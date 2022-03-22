package com.example.demo;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

@Service
public class ContinuedTransactionService {

	private static final Logger log = LoggerFactory.getLogger(ContinuedTransactionService.class);

	private final com.example.demo.CustomerRepository repository;

	private final com.example.demo.NestedTransactionService nestedTransactionService;

	public ContinuedTransactionService(CustomerRepository repository, NestedTransactionService nestedTransactionService) {
		this.repository = repository;
		this.nestedTransactionService = nestedTransactionService;
	}

	@Transactional
	public void continuedTransaction() {

		// fetch an individual customer by ID
		com.example.demo.Customer customer = repository.findById(1L);
		log.info("Customer found with findById(1L):");
		log.info("--------------------------------");
		log.info(customer.toString());
		log.info("");

		// fetch customers by last name
		log.info("Customer found with findByLastName('Bauer'):");
		log.info("--------------------------------------------");
		repository.findByLastName("Bauer").forEach(bauer -> {
			log.info(bauer.toString());
		});
		nestedTransactionService.newTransaction();
	}
}
