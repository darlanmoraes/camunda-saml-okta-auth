package com.darlan.camunda.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Value("${okta.issuer.url}")
    private String issuerUrl;
    private static final String SIGNOUT_URL = "/login/signout";

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onLogoutSuccess(final HttpServletRequest request,
                                final HttpServletResponse response,
                                final Authentication authentication) throws IOException {
        final HttpSession session;
        if ((session = request.getSession(false)) != null) {
            session.invalidate();
        }
        redirectStrategy.sendRedirect(request, response, issuerUrl + SIGNOUT_URL);
    }
}
