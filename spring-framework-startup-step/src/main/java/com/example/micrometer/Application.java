package com.example.micrometer;

import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.metrics.observability.ObservabilityApplicationStartup;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

public class Application {

	private final MeterRegistry meterRegistry;

	public Application(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	public void run() {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.setApplicationStartup(new ObservabilityApplicationStartup(meterRegistry));
		applicationContext.register(MyConfig.class);
		applicationContext.refresh();
	}

	public static void main(String[] args) {

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
class MyService {

	private final MyRepository myRepository;

	public MyService(MyRepository myRepository) {
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

