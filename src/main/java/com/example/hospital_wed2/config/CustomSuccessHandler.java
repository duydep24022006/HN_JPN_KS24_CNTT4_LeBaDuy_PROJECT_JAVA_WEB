package com.example.hospital_wed2.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String role = authentication.getAuthorities()
                .iterator().next().getAuthority();

        try {
            if (role.equals("ROLE_ADMIN")) {
                response.sendRedirect("/admin/dashboard");
            } else if (role.equals("ROLE_DOCTOR")) {
                response.sendRedirect("/doctor/dashboard");
            } else {
                response.sendRedirect("/patient/dashboard");
            }
        } catch (Exception e) {
            throw new RuntimeException("Redirect failed", e);
        }
    }
}