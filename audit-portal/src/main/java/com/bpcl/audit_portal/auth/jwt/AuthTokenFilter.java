package com.bpcl.audit_portal.auth.jwt;

import com.bpcl.audit_portal.auth.model.UserDetailsImplementation;
import com.bpcl.audit_portal.common.model.User;
import com.bpcl.audit_portal.common.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
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
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    public AuthTokenFilter(JwtUtils jwtUtils, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = jwtUtils.getJwtFromHeader(request);

            if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                Claims claims = jwtUtils.getAllClaimsFromToken(jwt);

                String username = claims.get("userName", String.class);
                Long userId = claims.get("userId", Long.class);
                String role = claims.get("role", String.class);
                List<Integer> applicationIds = claims.get("applicationIds", List.class);

                User user = userRepository.findById(userId).orElse(null);

                if (user == null) {
                    logger.warn("User not found for id: {}", userId);
                    filterChain.doFilter(request, response);
                    return;
                }

                if (Boolean.FALSE.equals(user.getIsActive())) {
                    logger.warn("User is inactive: {}", username);
                    filterChain.doFilter(request, response);
                    return;
                }

                List<GrantedAuthority> authorities = new ArrayList<>();

                if (applicationIds != null) {
                    authorities.addAll(applicationIds.stream()
                            .map(id -> new SimpleGrantedAuthority("APP_" + id))
                            .toList());
                }

                if (role != null) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }

                UserDetails userDetails = new UserDetailsImplementation(
                        userId,
                        username,
                        null,
                        authorities
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("User authenticated successfully: {}", username);
            }

        } catch (ExpiredJwtException e) {
            logger.warn("JWT expired for path: {}", request.getRequestURI());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT format for path: {}", request.getRequestURI());
        } catch (JwtException e) {
            logger.error("JWT processing error for path: {}", request.getRequestURI(), e);
        } catch (Exception e) {
            logger.error("Unexpected error in AuthTokenFilter for path: {}", request.getRequestURI(), e);
        }

        filterChain.doFilter(request, response);
    }
}