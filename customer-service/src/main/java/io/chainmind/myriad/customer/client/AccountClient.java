package io.chainmind.myriad.customer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.chainmind.myriad.customer.model.Account;

import java.util.List;

@FeignClient(name = "account-service")
public interface AccountClient {

	@GetMapping("/accounts/customer/{customerId}")
	List<Account> findByCustomer(@PathVariable("customerId") Long customerId);
	
}
