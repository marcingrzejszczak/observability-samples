package com.example.sleuthsamples;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mongodb.client.SynchronousContextProvider;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

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

	@Bean MyRunner myRunner(BasicUserRepository basicUserRepository) {
		return new MyRunner(basicUserRepository);
	}

	@Bean MongoClientSettingsBuilderCustomizer micrometerMongoClientSettingsBuilderCustomizer(MeterRegistry meterRegistry) {
		return clientSettingsBuilder -> clientSettingsBuilder
				.contextProvider(contextProvider(meterRegistry))
				.addCommandListener(new MicrometerMongoCommandListener(meterRegistry));
	}

	static SynchronousContextProvider contextProvider(MeterRegistry meterRegistry) {
		return () -> new SynchronousObservabilityRequestContext(meterRegistry);
	}

	static class SynchronousObservabilityRequestContext extends ObservabilityRequestContext {

		SynchronousObservabilityRequestContext(MeterRegistry meterRegistry) {
			super(context(meterRegistry));
		}

		private static Map<Object, Object> context(MeterRegistry meterRegistry) {
			Map<Object, Object> map = new ConcurrentHashMap<>();
			// simulating that there previously had been a sample in the flow
			Timer.Sample sample = Timer.start(meterRegistry);
			map.put(Timer.Sample.class, sample);
			return map;
		}

	}
}

class MyRunner {

	private final BasicUserRepository basicUserRepository;

	MyRunner(BasicUserRepository basicUserRepository) {
		this.basicUserRepository = basicUserRepository;
	}

	void run() {
		User save = basicUserRepository.save(new User("foo" + System.currentTimeMillis(), "bar", "baz", null));
		basicUserRepository.findUserByUsername(save.getUsername());
	}
}
