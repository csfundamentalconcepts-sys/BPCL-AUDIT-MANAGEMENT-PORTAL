package com.bpcl.audit_portal.auth.security;

import com.bpcl.audit_portal.auth.jwt.AuthEntryPointAccessDenied;
import com.bpcl.audit_portal.auth.jwt.AuthEntryPointJwt;
import com.bpcl.audit_portal.auth.jwt.AuthTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
SecurityFilterChain defaultSecurityFilterChain(
        HttpSecurity http,
        AuthTokenFilter authTokenFilter,
        AuthEntryPointJwt unauthorizedHandler,
        AuthEntryPointAccessDenied accessDeniedHandler) throws Exception {

    http
        .cors(Customizer.withDefaults())   // <-- Add this
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(requests ->
            requests
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/tickets/**").authenticated()
                    .requestMatchers("/api/applications/**").authenticated()
                    .requestMatchers("/api/ticket-history/**").authenticated()
                    .requestMatchers("/api/users/**").authenticated()
                    .requestMatchers("/api/vapt/**").authenticated()
                    .requestMatchers("/api/vapt-history/**").authenticated()
                .anyRequest().authenticated())
        .addFilterBefore(authTokenFilter,
                UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(exception -> exception
                .authenticationEntryPoint(unauthorizedHandler)
                .accessDeniedHandler(accessDeniedHandler));

    return http.build();
}
}