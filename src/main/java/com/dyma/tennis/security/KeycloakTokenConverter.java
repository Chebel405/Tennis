package com.dyma.tennis.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Convertit un JWT Keycloak en un objet JwtAuthenticationToken exploitable par Spring Security.
 */
@Component
public class KeycloakTokenConverter implements Converter<Jwt, JwtAuthenticationToken> {

    // Récupère l'ID du client à partir du fichier de configuration application.properties ou application.yml
    @Value("${jwt.auth.client-id}")
    private String clientId;

    // Récupère l'attribut principal (nom d'utilisateur) du token
    @Value("${jwt.auth.principal-attribute}")
    private String principalAttribute;

    // Convertisseur par défaut des autorités JWT de Spring Security
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    /**
     * Convertit un JWT en un JwtAuthenticationToken contenant les rôles et les autorités.
     * @param jwt Le token JWT à convertir.
     * @return Un objet JwtAuthenticationToken contenant l'utilisateur authentifié et ses rôles.
     */
    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        Collection<String> roles = Optional.ofNullable(jwt.getClaimAsMap("resource_access"))
                .map(map -> map.get(clientId))
                .map(resource -> (Collection<String>) ((Map<String, Object>) resource).get("roles"))
                .orElse(Collections.emptyList());

        Set<GrantedAuthority> authorities = Stream.concat(
                jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                roles.stream().map(SimpleGrantedAuthority::new)
        ).collect(Collectors.toSet());

        return new JwtAuthenticationToken(jwt, authorities, principalAttribute);
    }
}