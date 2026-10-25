package com.bms.bms_core.config;

import com.bms.bms_core.repositories.TheaterOwnerRepository;
import com.bms.bms_core.repositories.UserRepository;
import com.bms.bms_core.repositories.AdminRepository;
import com.bms.bms_core.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    /** Provides BCrypt password hashing and verification for authentication. */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Loads a user's stored password hash and assigns the account's security role. */
    @Bean
    UserDetailsService userDetailsService(UserRepository users,
                                         TheaterOwnerRepository owners,
                                         AdminRepository admins) {
        return email -> {
            String passwordHash = users.findPasswordByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));
            String role = admins.existsByEmail(email)
                    ? "ADMIN"
                    : owners.existsByEmail(email) ? "THEATER_OWNER" : "CUSTOMER";
            return User.withUsername(email)
                    .password(passwordHash)
                    .roles(role)
                    .build();
        };
    }

    /** Configures authentication to verify credentials with the user service and password encoder. */
    @Bean
    DaoAuthenticationProvider authenticationProvider(UserDetailsService users, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(users);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    /** Exposes Spring Security's configured authentication manager. */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /** Configures stateless JWT authentication and role-based access to API routes. */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            DaoAuthenticationProvider provider,
                                            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http.authenticationProvider(provider)
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/users/user/register", "/api/v1/users/login").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/theatre/create", "/api/v1/hall/register", "/api/v1/show/create").hasRole("THEATER_OWNER")
                        .requestMatchers("/api/v1/bookings/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/v1/payments/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/v1/theatre/get/all").hasRole("THEATER_OWNER")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                        (request, response, exception) -> response.sendError(401, "Unauthorized")))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
