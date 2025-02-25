package com.dyma.tennis.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;


/**
 * Configuration de la sécurité pour l'application
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private KeycloakTokenConverter keycloakTokenConverter;

    /**
     * Configure la chaîne de filtres de sécurité pour l'application.
     *
     * @param http l'objet HttpSecurity à configurer.
     * @return la chaîne de filtres de sécurité configurée.
     * @throws Exception si une erreur de configuration survient.
     */
    @Bean
    //Authentification obligatoire pour l'execution des requêtes
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Désactive la protection CSRF
                .headers(headers ->
                        headers
                                .contentSecurityPolicy(csp ->
                                        csp.policyDirectives("default-src 'self' data:; style-src 'self' 'unsafe-inline';") // Définit la politique de sécurité du contenu
                                )
                                .frameOptions(frameOptionsConfig -> frameOptionsConfig.deny()) // Empêche le site d'être affiché dans un iframe
                                .permissionsPolicyHeader(permissionsPolicyConfig -> permissionsPolicyConfig.policy(
                                        "fullscreen=(self), geolocation=(), microphone=(), camera=()" // Définit les politiques de permissions
                                ))
                )
                .authorizeHttpRequests(authorization ->
                        authorization
                                .requestMatchers("/healthcheck/**").permitAll()
                                .requestMatchers("/actuator/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/swagger-ui/**").permitAll()
                                .requestMatchers("/v3/api-docs/**").permitAll()
                                .requestMatchers("/accounts/token").permitAll()
                                .requestMatchers(HttpMethod.GET,"/players/**").hasAuthority("ROLE_USER")
                                .requestMatchers(HttpMethod.POST,"/players/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.PUT,"/players/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.DELETE,"/players/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.GET,"/tournaments/**").hasAuthority("ROLE_USER")
                                .requestMatchers(HttpMethod.POST,"/tournaments/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.PUT,"/tournaments/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.DELETE,"/tournaments/**").hasAuthority("ROLE_ADMIN")
                                .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(keycloakTokenConverter))
                );
        return http.build();
    }
}
