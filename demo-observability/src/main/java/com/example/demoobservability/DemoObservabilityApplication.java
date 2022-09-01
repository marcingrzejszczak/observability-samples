package com.example.demoobservability;

import brave.sampler.Sampler;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class DemoObservabilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoObservabilityApplication.class, args);
	}

	@Bean
	Sampler sampler() {
		return Sampler.ALWAYS_SAMPLE;
	}
}

@Component
class CommandLine implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(CommandLine.class);

	private final Tracer tracer;

	Span hello;

	CommandLine(Tracer tracer) {
		this.tracer = tracer;
	}

	@Override
	public void run(String... args) throws Exception {
		this.hello = this.tracer.nextSpan().name("HELLO");
		try (Tracer.SpanInScope scope = this.tracer.withSpan(hello.start())) {
			log.info("HELLO");
		} finally {
			this.hello.end();
		}
	}
}
