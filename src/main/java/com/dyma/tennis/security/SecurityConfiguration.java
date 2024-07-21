package com.dyma.tennis.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de la sécurité pour l'application
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Autowired
    private DymaUserDetailsService dymaUserDetailsService;

    /**
     * Définit un bean PasswordEncoder qui utilise BCrypt pour hacher les mots de passe.
     *
     * @return un PasswordEncoder qui utilise BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /**
     * Configure le AuthentificationManager avec un DaoAuthenticationProvider.
     *
     * @param userDetailsService le service pour récupérer les informations de l'utilisateur.
     * @param passwordEncoder l'encodeur de mots de passe.
     * @return un AuthenticationManager configuré
     */
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(dymaUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(authenticationProvider);
    }

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
                                .permissionsPolicy(permissionsPolicyConfig -> permissionsPolicyConfig.policy(
                                        "fullscreen=(self), geolocation=(), microphone=(), camera=()" // Définit les politiques de permissions
                                ))
                )
                .authorizeHttpRequests(authorization ->
                        authorization
                                .requestMatchers("/healthcheck/**").permitAll()
                                .requestMatchers("/swagger-ui/**").permitAll()
                                .requestMatchers("/v3/api-docs/**").permitAll()
                                .requestMatchers("/accounts/login").permitAll()
                                .requestMatchers(HttpMethod.GET,"/players/**").hasAuthority("ROLE_USER")
                                .requestMatchers(HttpMethod.POST,"/players/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.PUT,"/players/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.DELETE,"/players/**").hasAuthority("ROLE_ADMIN")
                                .anyRequest().authenticated());
        return http.build();
    }
}
