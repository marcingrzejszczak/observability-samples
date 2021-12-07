package com.example.micrometer;

import java.util.function.BiConsumer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.test.SampleTestRunner;

class ApplicationTests extends SampleTestRunner {

	ApplicationTests() {
		super(SamplerRunnerConfig
				.builder()
				.wavefrontApplicationName("observability-test")
				.wavefrontServiceName("startup-step")
				.wavefrontSource("marcin-pc")
				.wavefrontToken(System.getenv("WAVEFRONT_API_TOKEN"))
				.wavefrontUrl("https://demo.wavefront.com")
				.build());
	}

	@Override
	public BiConsumer<Tracer, MeterRegistry> yourCode() {
		return (tracer, meterRegistry) -> new Application(meterRegistry).run();
	}
}
