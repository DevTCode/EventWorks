package com.event.GestionEvents.Service;

import com.event.GestionEvents.Entity.AppUser;
import com.event.GestionEvents.Entity.Employe;
import com.event.GestionEvents.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
//userdetailsservice C'est une interface de Spring Security qui doit être mise en œuvre pour
// charger les informations de l'utilisateur basées sur un nom d'utilisateur.
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
//La méthode loadUserByUsername(String username) est la méthode principale à implémenter.
// Elle est invoquée par Spring Security lorsqu'il a besoin de récupérer les détails
// d'un utilisateur à partir de la base de données ou d'une autre source de données.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Attempting to load user: " + username);
        AppUser user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }

}
