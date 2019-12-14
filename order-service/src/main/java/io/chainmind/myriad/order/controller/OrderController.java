package io.chainmind.myriad.order.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.chainmind.myriad.order.client.AccountClient;
import io.chainmind.myriad.order.client.CustomerClient;
import io.chainmind.myriad.order.client.ProductClient;
import io.chainmind.myriad.order.model.Account;
import io.chainmind.myriad.order.model.Customer;
import io.chainmind.myriad.order.model.Order;
import io.chainmind.myriad.order.model.OrderStatus;
import io.chainmind.myriad.order.model.Product;
import io.chainmind.myriad.order.repository.OrderRepository;

@RestController
@RequestMapping("/orders")
public class OrderController {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Autowired
	OrderRepository repository;	
	@Autowired
	AccountClient accountClient;
	@Autowired
	CustomerClient customerClient;
	@Autowired
	ProductClient productClient;
	
	@PostMapping
	public Order prepare(@RequestBody Order order) throws JsonProcessingException {
		int price = 0;
		List<Product> products = productClient.findByIds(order.getProductIds());
		LOGGER.info("Products found: {}", mapper.writeValueAsString(products));
		Customer customer = customerClient.findByIdWithAccounts(order.getCustomerId());
		LOGGER.info("Customer found: {}", mapper.writeValueAsString(customer));
		for (Product product : products) 
			price += product.getPrice();
		final int priceDiscounted = priceDiscount(price, customer);
		LOGGER.info("Discounted price: {}", mapper.writeValueAsString(Collections.singletonMap("price", priceDiscounted)));
		Optional<Account> account = customer.getAccounts().stream().filter(a -> (a.getBalance() > priceDiscounted)).findFirst();
		if (account.isPresent()) {
			order.setAccountId(account.get().getId());
			order.setStatus(OrderStatus.ACCEPTED);
			order.setPrice(priceDiscounted);
			LOGGER.info("Account found: {}", mapper.writeValueAsString(account.get()));
		} else {
			order.setStatus(OrderStatus.REJECTED);
			LOGGER.info("Account not found: {}", mapper.writeValueAsString(customer.getAccounts()));
		}
		Map<String, String> m = MDC.getCopyOfContextMap();
		return repository.add(order);
	}
	
	@PutMapping("/{id}")
	public Order accept(@PathVariable Long id) throws JsonProcessingException {
		final Order order = repository.findById(id);
		LOGGER.info("Order found: {}", mapper.writeValueAsString(order));
		accountClient.withdraw(order.getAccountId(), order.getPrice());
		HashMap<String, Object> log = new HashMap<>();
		log.put("accountId", order.getAccountId());
		log.put("price", order.getPrice());
		LOGGER.info("Account modified: {}", mapper.writeValueAsString(log));
		order.setStatus(OrderStatus.DONE);
		LOGGER.info("Order status changed: {}", mapper.writeValueAsString(Collections.singletonMap("status", order.getStatus())));
		repository.update(order);
		return order;
	}
	
	private int priceDiscount(int price, Customer customer) {
		double discount = 0;
		switch (customer.getType()) {
		case REGULAR:
			discount += 0.05;
			break;
		case VIP:
			discount += 0.1;
			break;
			
		default:
			break;
		}
		int ordersNum = repository.countByCustomerId(customer.getId());
		discount += (ordersNum*0.01);
		return (int) (price - (price * discount));
	}
	
}
