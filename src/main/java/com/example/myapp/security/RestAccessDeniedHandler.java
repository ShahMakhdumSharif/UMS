package com.example.myapp.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        String json = String.format(
                "{\"timestamp\":\"%s\",\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access denied: You do not have permission to access this resource\",\"path\":\"%s\"}",
                LocalDateTime.now(),
                request.getRequestURI()
        );
        response.getWriter().write(json);
    }
}
