package io.chainmind.myriad.gateway;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {
    @Bean
    @Order(1)
    public SecurityWebFilterChain resourceServerWebFilterChain(ServerHttpSecurity http) {
        // @formatter:off
        http
            .authorizeExchange()
            	.pathMatchers("/actuator/**").permitAll()
                .anyExchange().authenticated()
                .and()
            .oauth2ResourceServer()
            	.jwt();
        return http.build();
        // @formatter:on
    }
    
    @Bean
    @Order(2)
    public SecurityWebFilterChain oauth2ClientWebFilterChain(ServerHttpSecurity http,
    	    ReactiveClientRegistrationRepository clientRegistrationRepository) {
        // @formatter:off
    	http.oauth2Login()
    			.and()
  			// Also logout at the OpenID Connect provider	
    		.logout(logout -> logout.logoutSuccessHandler(
    				new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository)))
    		// Authenticate through configured OpenID Provider
    		.authorizeExchange()
    			.anyExchange().authenticated()
    			.and()
    		// Allow showing /home within a frame
    		.headers().frameOptions().mode(Mode.SAMEORIGIN)
    			.and()
    	    // Disable CSRF in the gateway to prevent conflicts with proxied service CSRF
    		.csrf().disable();
        return http.build();
        // @formatter:on
    }

    @Bean
    CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(List.of("*"));
        corsConfig.setMaxAge(3600L);
        corsConfig.addAllowedMethod("*");
        corsConfig.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
    
}
