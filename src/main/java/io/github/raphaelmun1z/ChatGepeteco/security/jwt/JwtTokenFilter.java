package io.github.raphaelmun1z.ChatGepeteco.security.jwt;

import io.github.raphaelmun1z.ChatGepeteco.exceptions.models.InvalidJwtAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

public class JwtTokenFilter extends GenericFilterBean {
    private final JwtTokenProvider tokenProvider;
    private final HandlerExceptionResolver resolver;

    public JwtTokenFilter(JwtTokenProvider tokenProvider, HandlerExceptionResolver resolver) {
        this.tokenProvider = tokenProvider;
        this.resolver = resolver;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filter)
        throws IOException, ServletException {
        try {
            String token = tokenProvider.resolverToken((HttpServletRequest) request);
            if (StringUtils.isNotBlank(token) && tokenProvider.validarToken(token)) {
                Authentication authentication = tokenProvider.obterAutenticacao(token);

                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            filter.doFilter(request, response);
        } catch (InvalidJwtAuthenticationException e) {
            resolver.resolveException((HttpServletRequest) request, (HttpServletResponse) response, null, e);
        }
    }
}