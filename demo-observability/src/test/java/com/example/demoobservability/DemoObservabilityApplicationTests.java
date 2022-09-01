package com.example.demoobservability;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.BDDAssertions.then;

@SpringBootTest(properties = "spring.application.name=foo")
@ExtendWith(OutputCaptureExtension.class)
@AutoConfigureObservability
class DemoObservabilityApplicationTests {

	@Test
	void contextLoads(@Autowired CommandLine commandLine, CapturedOutput output) {
		then(commandLine.hello).isNotNull();

		String traceId = commandLine.hello.context().traceId();
		String spanId = commandLine.hello.context().spanId();

		then(output.toString()).contains("[foo," + traceId + "," + spanId + "]");
	}

}
