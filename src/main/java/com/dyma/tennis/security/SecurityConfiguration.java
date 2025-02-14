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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.util.stream.Collectors;

import static org.apache.commons.lang3.exception.ExceptionUtils.getMessage;


/**
 * Configuration de la sécurité pour l'application
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);
    @Value("${jwt.base64-secret}")
    private String jwtSecret;

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
    @Bean
    public JwtEncoder jwtEncoder(){
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

    @Bean
    public JwtDecoder jwtDecoder(){
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                .withSecretKey(getSecretKey())
                .macAlgorithm(SecurityUtils.JWT_ALGORITHM)
                .build();
        return token -> {
            try {
                return jwtDecoder.decode(token);
            } catch (Exception e){
                log.error("Conldn't decode JWT : {}", e.getMessage());
                throw e;
            }
        };
    }
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(){
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            return jwt.getClaimAsStringList(SecurityUtils.AUTHORITIES_CLAIM_KEY)
                    .stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        });
        return jwtAuthenticationConverter;
    }

    private SecretKey getSecretKey(){
        byte[] keyBytes = Base64.from(jwtSecret).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, SecurityUtils.JWT_ALGORITHM.getName());
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
                                .requestMatchers("/accounts/login").permitAll()
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
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }
}
