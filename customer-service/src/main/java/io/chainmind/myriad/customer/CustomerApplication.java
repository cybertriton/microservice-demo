package io.chainmind.myriad.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import io.chainmind.myriad.customer.model.Customer;
import io.chainmind.myriad.customer.model.CustomerType;
import io.chainmind.myriad.customer.repository.CustomerRepository;

@SpringBootApplication
@EnableFeignClients
public class CustomerApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(CustomerApplication.class, args);
	}

	@Bean
	public CommonsRequestLoggingFilter requestLoggingFilter() {
	    CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
	    loggingFilter.setIncludePayload(true);
	    loggingFilter.setIncludeHeaders(true);
	    loggingFilter.setMaxPayloadLength(1000);
	    loggingFilter.setAfterMessagePrefix("REQ:");
	    return loggingFilter;
	}
	
	@Bean
	CustomerRepository repository() {
		CustomerRepository repository = new CustomerRepository();
		repository.add(new Customer("John Scott", CustomerType.NEW));
		repository.add(new Customer("Adam Smith", CustomerType.REGULAR));
		repository.add(new Customer("Jacob Ryan", CustomerType.VIP));
		return repository;
	}

	
}
