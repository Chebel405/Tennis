package com.dyma.tennis.web;

import com.dyma.tennis.model.UserCredentials;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    private final SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();

    /**
     * Authentifie un utilisateur avec ses informations d'identification et initialise le context de sécurité.
     *
     * @param credentials Informations d'authentification (login et password) transmises dans la requête
     * @param request L'objet HttpServletRequest de la requête HTTP.
     * @param response L'objet HttpServletResponse de la réponse HTTP.
     */
    @PostMapping("/login")
    public void login(UserCredentials credentials, HttpServletRequest request, HttpServletResponse response){
        //Création d'un token d'authentification à partir des informations d'identification de l'utilisateur
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(credentials.login(), credentials.password());

        // Authentifie l'utilisateur avec le token d'authentification
        Authentication authentication =  authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        //Obtient le contexte de sécurité actuel
        SecurityContext securityContext =  SecurityContextHolder.getContext();
        //Définit l'authentification dans le contexte de sécurité
        securityContext.setAuthentication(authentication);

        //Sauvegarde le contexte de sécurité dans la session HTTP
        securityContextRepository.saveContext(securityContext, request, response);
    }
    @GetMapping("/logout")
    public void logout(@RequestBody @Valid Authentication authentication, HttpServletRequest request, HttpServletResponse response){
        securityContextLogoutHandler.logout(request, response, authentication);

    }




}
