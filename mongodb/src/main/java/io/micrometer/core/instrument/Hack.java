package io.micrometer.core.instrument;

public class Hack {

	public static Timer.Sample currentSample(MeterRegistry meterRegistry) {
		return meterRegistry.getCurrentSample();
	}
}
