package com.dyma.tennis.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
     * Configure le AuthentificationManager avec un DeoAuthenticationProvider.
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
                .authorizeHttpRequests(
                        authorization -> authorization.anyRequest().authenticated());
        return http.build();
    }
}
