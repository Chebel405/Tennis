package com.dyma.tennis.security;

import com.dyma.tennis.data.RoleEntity;
import com.dyma.tennis.data.UserEntity;
import com.dyma.tennis.data.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *  Service qui permet de retrouver l'utilisateur par son login pour l'authentification.
 */
@Component
public class DymaUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    /**
     * Charge les détails de l'utilisateur par son nom d'utilisateur (login).
     *
     * @param login Le nom d'utilisateur
     * @return Les détails de l'utilisateur, incluant les rêles et les autorisations
     * @throws UsernameNotFoundException Si l'utilisateur n'est pas trouvé
     */
    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        userRepository.findOneWithRolesByLoginIgnoreCase(login)
                .map(this::createSecurityUser)
                .orElseThrow(() -> new UsernameNotFoundException("User with login " + login + " could not be found"));


        return null;
    }

    /**
     * Crée un objet User (implémentation de UserDetails) à partir d'un userEntity.
     *
     * @param userEntity L'entité utilisateur récupérée depuis le base de données.
     * @return Un objet User inclant le login, le password haché et les rôles de l'utilisateur.
     */
    private User createSecurityUser(UserEntity userEntity) {
        // Transforme les rôles de l'utilisateur en SimpleGrantedAuthority pour Spring Security
        List<SimpleGrantedAuthority> grantedRoles = userEntity
                .getRoles()
                .stream()
                .map(RoleEntity::getName)
                .map(SimpleGrantedAuthority::new)
                .toList();

        // Retourne un objet User avec les informations de l'utilisateur
        return new User(userEntity.getLogin(), userEntity.getPassword(), grantedRoles);
    }
}
