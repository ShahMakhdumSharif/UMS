package com.example.myapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          RestAuthenticationEntryPoint authenticationEntryPoint,
                          RestAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(authorize -> authorize
                        // Static web resources and navigation routes
                        .requestMatchers("/", "/*.html", "/js/**", "/favicon.ico", "/error", "/ums/**").permitAll()
                        // Public auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        // H2 Database Console
                        .requestMatchers("/h2-console/**").permitAll()
                        // View access: all authenticated roles can see Admins, Teachers, Students
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/admins/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/teachers/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/students/**").authenticated()
                        // Modification access: limited as per role
                        .requestMatchers("/api/admins/**").hasRole("ADMIN")
                        .requestMatchers("/api/teachers/**").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers("/api/students/**").hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
