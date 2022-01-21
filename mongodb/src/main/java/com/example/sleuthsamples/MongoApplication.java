package com.example.sleuthsamples;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mongodb.client.SynchronousContextProvider;
import io.micrometer.api.instrument.MeterRegistry;
import io.micrometer.api.instrument.Timer;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.observability.MicrometerMongoCommandListener;

@SpringBootApplication
public class MongoApplication {

	public static void main(String... args) {
		new SpringApplicationBuilder(MongoApplication.class).web(WebApplicationType.NONE).run(args);
	}

	@Bean
	MyRunner myRunner(BasicUserRepository basicUserRepository, MeterRegistry meterRegistry) {
		return new MyRunner(basicUserRepository, meterRegistry);
	}

	@Bean
	MongoClientSettingsBuilderCustomizer micrometerMongoClientSettingsBuilderCustomizer(MeterRegistry meterRegistry) {
		return clientSettingsBuilder -> clientSettingsBuilder
				.contextProvider(contextProvider(meterRegistry))
				.addCommandListener(new MicrometerMongoCommandListener(meterRegistry)); // [5]
	}

	static SynchronousContextProvider contextProvider(MeterRegistry meterRegistry) {
		return () -> {
			return new SynchronousObservabilityRequestContext(meterRegistry);
		};
	}

	static class SynchronousObservabilityRequestContext extends ObservabilityRequestContext {

		SynchronousObservabilityRequestContext(MeterRegistry meterRegistry) {
			super(context(meterRegistry));
		}

		private static Map<Object, Object> context(MeterRegistry meterRegistry) {
			Map<Object, Object> map = new ConcurrentHashMap<>();
			map.put(Timer.Sample.class, meterRegistry.getCurrentSample());
			return map;
		}

	}
}

class MyRunner {

	private final BasicUserRepository basicUserRepository;

	private final MeterRegistry meterRegistry;

	MyRunner(BasicUserRepository basicUserRepository, MeterRegistry meterRegistry) {
		this.basicUserRepository = basicUserRepository;
		this.meterRegistry = meterRegistry;
	}

	void run() {
		Timer.Sample foo = Timer.start(meterRegistry);
		try (Timer.Scope scope = foo.makeCurrent()) {
			User save = basicUserRepository.save(new User("foo" + System.currentTimeMillis(), "bar", "baz", null));
			basicUserRepository.findUserByUsername(save.getUsername());
		}
		finally {
			foo.stop(Timer.builder("my.runner"));
		}
	}
}
