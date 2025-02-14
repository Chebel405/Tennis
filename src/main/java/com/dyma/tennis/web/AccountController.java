package com.dyma.tennis.web;

import com.dyma.tennis.model.UserAuthentication;
import com.dyma.tennis.model.UserCredentials;
import com.dyma.tennis.security.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

/**
 * Controller pour gérer les opérations liées aux comptes utilisateurs.
 */

@Tag(name="Accounts API")
@RestController
@RequestMapping("/accounts")
public class AccountController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    private final SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();

    /**
     * Authentifie un utilisateur avec ses informations d'identification et initialise le context de sécurité.
     *
     * @param credentials Informations d'authentification (login et password) transmises dans la requête
     * @param request L'objet HttpServletRequest de la requête HTTP.
     * @param response L'objet HttpServletResponse de la réponse HTTP.
     */
    @PostMapping("/login")
    public ResponseEntity<UserAuthentication> login(UserCredentials credentials, HttpServletRequest request, HttpServletResponse response){
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(credentials.login(), credentials.password());
        Authentication authentication =  authenticationManager.authenticate(authenticationToken);

        String jwt = jwtService.createToken(authentication);
        return new ResponseEntity<>(
                new UserAuthentication(authentication.getName(), jwt),
                HttpStatus.OK
        );

    }
}
