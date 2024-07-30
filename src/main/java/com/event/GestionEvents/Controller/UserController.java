package com.event.GestionEvents.Controller;

import com.event.GestionEvents.Entity.AppUser;
import com.event.GestionEvents.Repository.UserRepository;
import com.event.GestionEvents.Service.EmployeeService;
import com.event.GestionEvents.dto.RegisterDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Controller
public class UserController {
    //Le rôle de `@Autowired` en Spring Boot est d'injecter automatiquement les dépendances
    //nécessaires dans un composant, tel qu'un service ou un contrôleur, permettant ainsi
    //de gérer les objets de manière transparente et efficace.
    @Autowired
    private UserRepository repo;
    @Autowired
    public UserController(UserRepository repo) {
        this.repo = repo;

    }
    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }


    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                return "redirect:/dash";
            } else if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_CLIENT"))) {
                return "redirect:/client";
            }
        }
        return "redirect:/login?error";
    }
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login?logout";
    }
    @GetMapping("/register")
    public String register(Model model) {
        RegisterDto registerDto = new RegisterDto();
        model.addAttribute(registerDto);
        model.addAttribute("success",false);
        return "register";
    }

    @PostMapping("/register")
    public String register(
            Model model,
            @Valid @ModelAttribute RegisterDto registerDto,
            BindingResult result
    ) {
        AppUser appUser = repo.findByUsername(registerDto.getUsername());
        if (appUser != null) {
            result.addError(
                    new FieldError("registerDto", "email",
                            "Email address is already used")
            );
        }
        if (result.hasErrors()) {
            return "register";
        }
        try {
            var bCryptEncoder = new BCryptPasswordEncoder();
            AppUser newUser = new AppUser();
            newUser.setUsername (registerDto.getUsername());
            newUser.setRole("ADMIN");
            newUser.setPassword(bCryptEncoder.encode(registerDto.getPassword()));
            repo.save(newUser);
            model.addAttribute ("registerDto", new RegisterDto () );
            model.addAttribute("success", true);
        }
        catch (Exception ex) {
            result.addError (
                    new FieldError ("registerDto", "username",
            ex.getMessage())
            );
        }

        return "register";
    }
}
