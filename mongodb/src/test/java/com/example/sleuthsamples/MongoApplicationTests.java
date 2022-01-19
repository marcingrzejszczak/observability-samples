package com.example.sleuthsamples;

import java.util.Deque;
import java.util.function.BiConsumer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.TimerRecordingHandler;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.test.SampleTestRunner;
import io.micrometer.tracing.test.reporter.BuildingBlocks;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.observability.MongoTracingRecordingHandler;

@SpringBootTest
class MongoApplicationTests extends SampleTestRunner {

	@Autowired MeterRegistry meterRegistry;

	@Autowired MyRunner myRunner;

	@Override protected MeterRegistry getMeterRegistry() {
		return this.meterRegistry;
	}

	@Override protected SampleRunnerConfig getSampleRunnerConfig() {
		return SampleRunnerConfig.builder().build();
	}

	@Override public BiConsumer<Tracer, MeterRegistry> yourCode() {
		return (tracer, meterRegistry) -> myRunner.run();
	}

	@Override public BiConsumer<BuildingBlocks, Deque<TimerRecordingHandler>> customizeTimerRecordingHandlers() {
		return (buildingBlocks, timerRecordingHandlers) -> {
			MongoTracingRecordingHandler handler = new MongoTracingRecordingHandler(
					buildingBlocks.getTracer());
			handler.setSetRemoteIpAndPortEnabled(true);
			timerRecordingHandlers.addFirst(handler);
		};
	}
}
