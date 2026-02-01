package com.danielkim.order.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Order Processing API.
 *
 * <p>This configuration provides: - HTTP Basic Authentication for simplicity (suitable for
 * demo/testing) - In-memory user store with predefined users - Method-level security
 * with @PreAuthorize annotations - Public access to H2 console for development - CSRF disabled for
 * REST API usage
 *
 * <p>For production environments, this should be replaced with: - JWT-based authentication -
 * Database-backed user store - OAuth2/OIDC integration - Proper CSRF protection for web
 * applications
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  /**
   * Configures the security filter chain.
   *
   * <p>Security rules: - H2 console is publicly accessible (development only) - API endpoints
   * require authentication - HTTP Basic authentication is enabled - CSRF is disabled for REST API -
   * Frame options disabled for H2 console
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            authz ->
                authz
                    .requestMatchers("/h2-console/**")
                    .permitAll() // Allow H2 console access
                    .requestMatchers("/actuator/health")
                    .permitAll() // Health check endpoint
                    .anyRequest()
                    .authenticated())
        .httpBasic(basic -> {}) // Enable HTTP Basic authentication
        .csrf(
            csrf ->
                csrf.ignoringRequestMatchers(
                    "/h2-console/**", // Disable CSRF for H2 console
                    "/api/**" // Disable CSRF for REST API endpoints
                    ))
        .headers(
            headers -> headers.frameOptions().sameOrigin() // Allow frames for H2 console
            );

    return http.build();
  }

  /**
   * In-memory user details service with predefined users.
   *
   * <p>Demo users: - Username: user, Password: password, Role: USER - Username: admin, Password:
   * admin123, Role: USER, ADMIN
   *
   * <p>For production, replace with database-backed UserDetailsService.
   */
  @Bean
  public UserDetailsService userDetailsService() {
    UserDetails user =
        User.builder()
            .username("user")
            .password(passwordEncoder().encode("password"))
            .roles("USER")
            .build();

    UserDetails admin =
        User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin123"))
            .roles("USER", "ADMIN")
            .build();

    return new InMemoryUserDetailsManager(user, admin);
  }

  /** Password encoder using BCrypt for secure password hashing. */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
