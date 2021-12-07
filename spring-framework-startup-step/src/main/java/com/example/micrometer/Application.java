package com.example.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.metrics.micrometer.MicrometerApplicationStartup;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

public record Application(MeterRegistry meterRegistry) {

	public void run() {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		MicrometerApplicationStartup startup = new MicrometerApplicationStartup(this.meterRegistry);
		try {
			applicationContext.setApplicationStartup(startup);
			applicationContext.register(MyConfig.class);
			applicationContext.refresh();
		}
		finally {
			startup.stopRootSample();
		}
	}

	public static void main(String[] args) {
		new Application(new SimpleMeterRegistry()).run();
	}
}

@Configuration
class MyConfig {

	@Bean
	MyRepository myRepository() {
		return new MyRepository();
	}

	@Bean
	MyService myService(MyRepository myRepository) {
		return new MyService(myRepository);
	}

	@Bean
	static MyBeanPostProcessor myBeanPostProcessor() {
		return new MyBeanPostProcessor();
	}
}

@Service
record MyService(MyRepository myRepository) {

	MyService(MyRepository myRepository) {
		this.myRepository = myRepository;
		try {
			Thread.sleep(200L);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

@Repository
class MyRepository {

	public MyRepository() {
		try {
			Thread.sleep(250L);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

class MyBeanPostProcessor implements BeanPostProcessor {
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		try {
			Thread.sleep(100L);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
	}
}


