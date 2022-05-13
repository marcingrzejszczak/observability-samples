package com.example.sleuthsamples;

import brave.handler.SpanHandler;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpConnectionPoolMetrics;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpMetricsEventListener;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import io.micrometer.tracing.handler.HttpClientTracingObservationHandler;
import io.micrometer.tracing.http.HttpClientHandler;
import jakarta.annotation.PostConstruct;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@SpringBootApplication
public class OkHttp3Application implements CommandLineRunner {

	public static void main(String... args) {
		new SpringApplicationBuilder(OkHttp3Application.class).web(WebApplicationType.NONE).run(args);
	}

	@Autowired
	MeterRegistry meterRegistry;

	@Autowired
	ObservationRegistry observationRegistry;

	@Autowired
	HttpClientTracingObservationHandler handler;

	@Override
	public void run(String... args) throws Exception {
		var client = new OkHttpClient.Builder().eventListener(OkHttpMetricsEventListener
				.builder(this.meterRegistry, "okhttp.requests").tags(Tags.of("foo", "bar"))
				.observationRegistry(this.observationRegistry)
				.build()).build();
		Request request = new Request.Builder().url("https://httpbin.org").build();

		client.newCall(request).execute().close();

		Thread.sleep(5000);
	}

	@PostConstruct
	void foo() {
		this.observationRegistry.observationConfig().observationHandler(this.handler);
	}
}
