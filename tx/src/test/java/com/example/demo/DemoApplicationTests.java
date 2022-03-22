package com.example.demo;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.micrometer.contextpropagation.ContextContainer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.test.SampleTestRunner;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.AfterAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class DemoApplicationTests extends SampleTestRunner {

	@Autowired
	MeterRegistry meterRegistry;

	@Autowired
	NewTransactionService newTransactionService;

	static ExecutorService executorService = Executors.newSingleThreadExecutor();

	@Autowired
	TransactionTemplate transactionTemplate;

	@Autowired
	TransactionInterceptor transactionInterceptor;

	@Autowired
	PlatformTransactionManager platformTransactionManager;

	ObservationRegistry observationRegistry = ObservationRegistry.create();

//
//	@Autowired
//	AnnotationTransactionAspect annotationTransactionAspect;

	@PostConstruct
	void setup() {
		this.transactionTemplate.setObservationRegistry(getObservationRegistry());
		this.transactionInterceptor.setObservationRegistry(getObservationRegistry());
//		this.annotationTransactionAspect.setObservationRegistry(getObservationRegistry());
	}

	@Override
	public SampleTestRunnerConsumer yourCode() throws Exception {
		return (buildingBlocks, mr) -> {
			try {
				ContextContainer container = ContextContainer.create();

				System.out.println("Before : " + TransactionSynchronizationManager.getCurrentTransactionName());

				transactionTemplate.setName("test");

				String transactionName = transactionTemplate.execute(status -> {
					System.out.println("In transaction");

					Future<?> submit = executorService.submit(new ContextPropagatingRunnable(container, () -> {
						System.out.println("In new thread before new transaction : " + TransactionSynchronizationManager.getCurrentTransactionName());
						newTransactionService.newTransaction();
						System.out.println("In new thread after new transaction : " + TransactionSynchronizationManager.getCurrentTransactionName());
					}));

					try {
						submit.get();
					}
					catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}

					return TransactionSynchronizationManager.getCurrentTransactionName();
				});

				System.out.println("After execution : " + transactionName);

			}
			catch (Exception e) {
				System.out.println("Expected to throw an exception so that we see if rollback works");
			}
		};
	}

	@AfterAll
	static void cleanup() {
		executorService.shutdown();
	}

	@Override
	protected MeterRegistry getMeterRegistry() {
		return this.meterRegistry;
	}

	@Override
	protected ObservationRegistry getObservationRegistry() {
		return this.observationRegistry;
	}
}


class ContextPropagatingRunnable implements Runnable {

	private final ContextContainer container;

	private final Runnable delegate;

	ContextPropagatingRunnable(ContextContainer container, Runnable delegate) {
		this.container = container.captureThreadLocalValues();
		this.delegate = delegate;
	}

	@Override
	public void run() {
		this.container.tryScoped(this.delegate);
	}
}
