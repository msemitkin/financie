package com.github.msemitkin.financie.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TokenAuthFilter extends OncePerRequestFilter {
    private final String token;

    public TokenAuthFilter(@Value("${bot.telegram.webhook-secret-token}") String token) {
        this.token = token;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (!"/financie".equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String tokenValue = request.getHeader("X-Telegram-Bot-Api-Secret-Token");
        if (token.equals(tokenValue)) {
            logger.trace("Authorized request");
            filterChain.doFilter(request, response);
        } else {
            logger.error("Unauthorized request");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
