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
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Controller pour gérer les opérations liées aux comptes utilisateurs.
 */

@Tag(name="Accounts API")
@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String tokenIssuerUrl;

    @Value("${jwt.auth.client-id}")
    private String clientId;

    @Operation(summary = "Gets an access token", description = "Gets an access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access token was provided.",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserAuthentication.class))}),
            @ApiResponse(responseCode = "400", description = "Login or password is not provided."),
            @ApiResponse(responseCode = "500", description = "An error occurred while asking for an access token.")
    })
    @PostMapping("/token")
    public ResponseEntity<UserAuthentication> getAccessToken(@RequestBody @Valid UserCredentials credentials) {
        String url = tokenIssuerUrl + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

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
