package com.swedbank.common.application.filter;

import com.swedbank.auth.application.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class TokenAuthFilter extends OncePerRequestFilter {

    @Value("${security.jwt.header}")
    private String headerValue;

    @Value("${security.jwt.prefix}")
    private String prefix;

    @Value("${security.jwt.secret}")
    private String secret;

    private final RequestAttributeSecurityContextRepository requestAttributeSecurityContextRepository;

    public TokenAuthFilter(RequestAttributeSecurityContextRepository requestAttributeSecurityContextRepository) {
        this.requestAttributeSecurityContextRepository = requestAttributeSecurityContextRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(headerValue);
        if (null != header && header.startsWith(prefix)) {
            String token = header.replace(prefix, "").trim();
            try {
                Claims claims = JwtUtil.getAllClaimsFromToken(token, secret);
                String username = claims.getSubject();
                var isTokenValid = JwtUtil.validateToken(token, username, secret);
                if (username != null && isTokenValid) {
                    User principal = new User(username, "", List.of());
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            principal.getAuthorities()
                    );
                    SecurityContext context = new SecurityContextImpl(auth);

                    SecurityContextHolder.setContext(context);

                    requestAttributeSecurityContextRepository.saveContext(context, request, response);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
