package io.chainmind.myriad.customer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import feign.RequestInterceptor;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfiguration.class);
	
	@Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") 
	private String jwkSetUri;
		
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http
			.authorizeRequests(authorizeRequests ->
				authorizeRequests
					.antMatchers("/actuator/**").permitAll()
				.anyRequest().authenticated()
			)
			.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
		// @formatter:on
	}
	
	@Bean
	JwtDecoder jwtDecoder() {
		return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
	}
	
	@Bean
	public RequestInterceptor requestInterceptor() {
		return (template) -> {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null) {
				Object principal = auth.getPrincipal();
				if (principal instanceof Jwt) {
					Jwt jwt = (Jwt) principal;
					LOGGER.info("JWT token is: %s", jwt.getTokenValue());
					template.header(HttpHeaders.AUTHORIZATION,  "Bearer " + jwt.getTokenValue());
				} else {
					LOGGER.warn("********* An OAuth2 JWT access token required **********");
				}
			}
		};
	}
	
}
