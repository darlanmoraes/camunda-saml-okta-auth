package com.darlan.camunda.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.identity.Group;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessEngineAuthenticationFilter implements Filter {

    @Override
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        final ProcessEngine engine = BpmPlatform.getDefaultProcessEngine() != null
                ? BpmPlatform.getDefaultProcessEngine()
                : ProcessEngines.getDefaultProcessEngine(false);

        if (engine == null) {
            final String msg = "Default Process engine not available";
            final ObjectMapper objectMapper = new ObjectMapper();
            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
            response.setContentType(MediaType.APPLICATION_JSON);
            objectMapper.writer().writeValue(response.getWriter(), msg);
            response.getWriter().flush();
            return;
        }
        try {
            if (request.getHeader("username") != null) {
                final String userId = request.getHeader("username");
                this.setAuthenticatedUser(engine, userId, this.getGroupIdsForUser(engine, userId), new ArrayList<>());
                chain.doFilter(req, res);
            } else {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            }
        } finally {
            this.clearAuthentication(engine);
        }
    }

    protected void setAuthenticatedUser(final ProcessEngine engine, final String userId,
                                        final List<String> groupIds, final List<String> tenantIds) {
        engine.getIdentityService().setAuthentication(userId, groupIds, tenantIds);
    }

    protected List<String> getGroupIdsForUser(final ProcessEngine engine, final String userId){
        final List<Group> groups = engine.getIdentityService().createGroupQuery()
                .groupMember(userId)
            .list();
        return groups.stream()
                .map(Group::getId)
            .collect(Collectors.toList());
    }

    protected void clearAuthentication(final ProcessEngine engine) {
        engine.getIdentityService().clearAuthentication();
    }
}

