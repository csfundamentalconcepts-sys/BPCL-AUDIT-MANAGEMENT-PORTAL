package com.bpcl.audit_portal.auth.jwt;

import com.bpcl.audit_portal.auth.model.UserDetailsImplementation;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    public AuthTokenFilter(JwtUtils jwtUtils){
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = jwtUtils.getJwtFromHeader(request);

            if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                Claims claims = jwtUtils.getAllClaimsFromToken(jwt);

                String userName = claims.get("userName", String.class);
                Long userId = claims.get("userId", Long.class);

                String role = claims.get("roles", String.class);
                List<String> permissions = claims.get("permissions", List.class);

                List<GrantedAuthority> authorities = new ArrayList<>();

                if (permissions != null) {
                    authorities.addAll(
                            permissions.stream()
                                    .map(SimpleGrantedAuthority::new)
                                    .toList()
                    );
                }

                if (role != null) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }

                UserDetails userDetails =
                        new UserDetailsImplementation(
                                userId,
                                userName,
                                null,
                                authorities
                        );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (ExpiredJwtException e) {
            logger.warn("JWT expired: {}", request.getRequestURI());

        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT format: {}", request.getRequestURI());

        } catch (SecurityException e) {
            logger.error("JWT signature failed: {}", request.getRequestURI());

        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT: {}", request.getRequestURI());

        } catch (IllegalArgumentException e) {
            logger.error("Empty/invalid JWT: {}", request.getRequestURI());

        } catch (Exception e) {
            logger.error("JWT error: {}", request.getRequestURI(), e);
        }
        filterChain.doFilter(request, response);
    }
}