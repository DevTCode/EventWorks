package com.event.GestionEvents.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean

    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((request) -> request
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .requestMatchers("/client/**").hasRole("CLIENT")
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/logout").permitAll()
                        .requestMatchers("/edit/**").permitAll()
                        .requestMatchers("/editProfile/**").authenticated()
                        .requestMatchers("/reserve").hasRole("CLIENT")
                        .requestMatchers("/editForm/**").permitAll()
                        .requestMatchers("/confirm/**").permitAll()
                        .requestMatchers("/cancel/**").permitAll()
                        .requestMatchers("/reservations/**").permitAll()
                        .requestMatchers("/add").permitAll()
                        .requestMatchers("/notifications").permitAll()
                        .requestMatchers("/clearNotifications").permitAll()
                        .requestMatchers("/dash").permitAll()
                        .requestMatchers("/addEvent").permitAll()
                        .requestMatchers("/addForm").permitAll()
                        .requestMatchers("/showEvents").permitAll()
                        .requestMatchers("/list").permitAll()
                        .requestMatchers("/showReservations").permitAll()
                        .requestMatchers("/showAdminReservations").permitAll()
                        .requestMatchers("/dashboard").authenticated()
                        .requestMatchers("/register").permitAll()
                        .requestMatchers("/show").permitAll()
                        .requestMatchers("/static/**", "/style/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/", "/home").permitAll())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/dashboard",true)
                        .failureUrl("/login?error=true")
                )
                .logout(logoutConfigurer -> logoutConfigurer.logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true).permitAll())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
