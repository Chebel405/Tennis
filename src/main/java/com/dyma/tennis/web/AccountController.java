package com.dyma.tennis.web;

import com.dyma.tennis.model.UserAuthentication;
import com.dyma.tennis.model.UserCredentials;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Contrôleur gérant l'authentification des utilisateurs.
 */
@Tag(name = "Accounts API")
@RestController
@RequestMapping("/accounts")
public class AccountController {

    // URL du serveur d'authentification Keycloak (récupérée depuis application.properties)
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String tokenIssuerUrl;

    // ID du client utilisé pour l'authentification (défini dans Keycloak)
    @Value("${jwt.auth.client-id}")
    private String clientId;

    /**
     * Endpoint permettant d'obtenir un token d'accès en utilisant les identifiants de l'utilisateur.
     *
     * @param credentials Identifiants de connexion fournis par l'utilisateur.
     * @return Un objet UserAuthentication contenant le token d'accès.
     */
    @Operation(summary = "Gets an access token", description = "Gets an access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access token was provided.",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserAuthentication.class))}),
            @ApiResponse(responseCode = "400", description = "Login or password is not provided."),
            @ApiResponse(responseCode = "500", description = "An error occurred while asking for an access token.")
    })
    @PostMapping("/token") // Définit l'URL de l'endpoint : /accounts/token
    public ResponseEntity<UserAuthentication> getAccessToken(@RequestBody @Valid UserCredentials credentials) {
        // Construire l'URL de l'endpoint de Keycloak pour récupérer le token
        String url = tokenIssuerUrl + "/protocol/openid-connect/token";
        // Définition des en-têtes HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);// On envoie des données sous format formulaire

        // Création du corps de la requête sous forme de paramètres URL
        String requestBody = Map.of(
                        "username", credentials.login(),
                        "password", credentials.password(),
                        "grant_type", "password",
                        "client_id", clientId
                ).entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        String accessToken = (String) Objects.requireNonNull(response.getBody()).get("access_token");

        return ResponseEntity.ok(new UserAuthentication(credentials.login(), accessToken));
    }
}