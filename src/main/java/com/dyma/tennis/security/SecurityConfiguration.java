package com.dyma.tennis.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private KeycloakTokenConverter keycloakTokenConverter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers ->
                        headers
                                .contentSecurityPolicy(csp ->
                                        csp.policyDirectives("default-src 'self' data:; style-src 'self' 'unsafe-inline';")
                                )
                                .frameOptions(frameOptionsConfig -> frameOptionsConfig.deny())
                                .permissionsPolicyHeader(permissionsPolicyConfig -> permissionsPolicyConfig.policy(
                                        "fullscreen=(self), geolocation=(), microphone=(), camera=()"
                                ))
                )
                .authorizeHttpRequests(authorizations ->
                        authorizations
                                .requestMatchers("/swagger-ui/**").permitAll()
                                .requestMatchers("/v3/api-docs/**").permitAll()
                                .requestMatchers("/accounts/token").permitAll()
                                .requestMatchers("/healthcheck").permitAll()
                                .requestMatchers("/actuator/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.GET, "/players/**").hasAuthority("ROLE_USER")
                                .requestMatchers(HttpMethod.POST, "/players/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/players/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/players/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.GET, "/tournaments/**").hasAuthority("ROLE_USER")
                                .requestMatchers(HttpMethod.POST, "/tournaments/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/tournaments/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/tournaments/**").hasAuthority("ROLE_ADMIN")
                                .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakTokenConverter))
                );
        return http.build();
    }
}