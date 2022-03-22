package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class NestedTransactionService {

	private static final Logger log = LoggerFactory.getLogger(NestedTransactionService.class);

	private final CustomerRepository repository;

	public NestedTransactionService(CustomerRepository repository) {
		this.repository = repository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void newTransaction() {
		System.out.println("In new thread inside new nested transaction : " + TransactionSynchronizationManager.getCurrentTransactionName());
		repository.save(new Customer("Hello", "From Propagated Transaction"));
		repository.deleteById(10238L);
		log.info("");
	}
}
