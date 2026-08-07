package com.transport.tms.Config;

import com.transport.tms.UserManagement.Service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Validates the Authorization: Bearer <token> header on every request (once
 * per request — hence OncePerRequestFilter) and, if valid, populates
 * Spring Security's per-request SecurityContext with the token's identity
 * and authorities. This is what makes "authorities.contains(DRIVER)" /
 * "which driver is this" meaningful downstream, and does so per-thread —
 * concurrent requests from different drivers never share or race on this
 * context, since each request gets its own thread and its own
 * ThreadLocal-backed SecurityContext.
 *
 * Deliberately tolerant of a missing/invalid token here — it just leaves
 * the request unauthenticated rather than rejecting it outright, so
 * SecurityConfig's per-path rules (permitAll vs authenticated) are what
 * actually decide whether that matters for a given endpoint. This keeps
 * existing, currently-permitAll parts of the app working exactly as
 * before; only paths SecurityConfig marks as requiring authentication
 * will actually be blocked by a missing/bad token.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                Claims claims = tokenService.decodeAccessToken(token);

                String username = claims.get("username", String.class);

                @SuppressWarnings("unchecked")
                List<String> rawAuthorities = claims.get("authorities", List.class);

                List<GrantedAuthority> authorities = (rawAuthorities == null ? List.<String>of() : rawAuthorities)
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                // Invalid/expired token — leave the request unauthenticated
                // rather than throwing here; SecurityConfig decides whether
                // that's actually a problem for this specific path.
                log.debug("Ignoring invalid/expired token: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
