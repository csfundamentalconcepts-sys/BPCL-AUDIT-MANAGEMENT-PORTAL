package com.bpcl.audit_portal.auth.security;

import com.bpcl.audit_portal.auth.jwt.AuthEntryPointAccessDenied;
import com.bpcl.audit_portal.auth.jwt.AuthEntryPointJwt;
import com.bpcl.audit_portal.auth.jwt.AuthTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
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
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, AuthTokenFilter authTokenFilter, AuthEntryPointJwt unauthorizedHandler, AuthEntryPointAccessDenied accessDeniedHandler) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((requests) ->
                        requests.requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                .requestMatchers("/api/head/**").hasRole("HEAD")
                                .requestMatchers("/api/spoc/**").hasRole("SPOC")
                                .requestMatchers("/api/developer/**").hasRole("DEVELOPER")
                                .requestMatchers("/api/scrum-master/**").hasRole("SCRUM_MASTER")
                                .requestMatchers("/api/tickets/**").authenticated()
                                .requestMatchers("/api/applications/**").authenticated()
                                .anyRequest().authenticated())

                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(accessDeniedHandler)
                );
        return http.build();
    }
}