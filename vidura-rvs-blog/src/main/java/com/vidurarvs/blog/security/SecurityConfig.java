package com.vidurarvs.blog.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Access rules for ViduraRvs:
 * - Everyone (no login) can read the public blog, search, and view author profiles.
 * - /admin/admins/** (managing other admin accounts) is SUPER_ADMIN only.
 * - Everything else under /admin/** requires ADMIN or SUPER_ADMIN.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/post/**", "/category/**", "/search", "/about",
                                "/author/**", "/css/**", "/js/**", "/img/**", "/uploads/**", "/login").permitAll()
                        .requestMatchers("/admin/admins/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/admin/dashboard", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                // Classic (non-BREACH-masked) CSRF handler: works correctly with
                // server-rendered Thymeleaf forms where the hidden _csrf field is
                // rendered once and posted back as-is.
                .csrf(csrf -> csrf.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .exceptionHandling(handling -> handling.accessDeniedPage("/access-denied"));

        return http.build();
    }
}
