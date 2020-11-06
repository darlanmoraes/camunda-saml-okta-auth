package com.darlan.camunda.okta.authentication;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.rest.security.auth.AuthenticationResult;
import org.camunda.bpm.engine.rest.security.auth.impl.ContainerBasedAuthenticationProvider;
import org.camunda.bpm.engine.rest.util.EngineUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

public class OktaAuthenticationProvider extends ContainerBasedAuthenticationProvider {

    @Override
    public AuthenticationResult extractAuthenticatedUser(final HttpServletRequest request, final ProcessEngine engine) {
        Authentication auth = null;

        final SecurityContext context = SecurityContextHolder.getContext();
        if (context != null && context.getAuthentication() != null) {
            auth = context.getAuthentication();
        }

        if (auth instanceof Saml2Authentication) {
            final Saml2Authentication authentication = (Saml2Authentication) SecurityContextHolder.getContext().getAuthentication();
            return contextAuthentication(authentication);
        } else {
            return AuthenticationResult.unsuccessful();
        }

    }

    private AuthenticationResult contextAuthentication(Saml2Authentication authentication) {
        final String userId = authentication.getName();

        if (StringUtils.isEmpty(userId)) {
            return AuthenticationResult.unsuccessful();
        }

        final AuthenticationResult result = new AuthenticationResult(userId, true);
        result.setGroups(this.getUserGroups(userId, EngineUtil.lookupProcessEngine("default")));
        return result;
    }

    private List<String> getUserGroups(final String userId, final ProcessEngine engine) {
        return engine.getIdentityService()
                .createGroupQuery()
                .groupMember(userId)
                .list()
                .stream()
                .map(Group::getId)
            .collect(Collectors.toList());
    }

}
