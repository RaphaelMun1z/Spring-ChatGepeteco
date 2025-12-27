package io.github.raphaelmun1z.ChatGepeteco.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.raphaelmun1z.ChatGepeteco.exceptions.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                LocalDateTime.now().toString(),
                List.of("Acesso negado: você não tem permissão para acessar este recurso."),
                request.getRequestURI()
        );

        response.getWriter().write(new ObjectMapper().writeValueAsString(exceptionResponse));
    }
}
