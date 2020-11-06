package com.darlan.camunda.custom.service;

import com.darlan.camunda.okta.client.OktaIdentityServiceClient;
import com.darlan.camunda.okta.model.OktaGroup;
import com.darlan.camunda.custom.entity.CustomGroup;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final OktaIdentityServiceClient oktaIdentityServiceClient;

    public GroupService(final OktaIdentityServiceClient oktaIdentityServiceClient) {
        this.oktaIdentityServiceClient = oktaIdentityServiceClient;
    }

    public CustomGroup findById(final String id) {
        return CustomGroup.builder()
                .id(id)
                .name(id)
                .type("")
            .build();
    }

    private CustomGroup fromOktaGroup(final OktaGroup oktaGroup){
        return CustomGroup.builder()
                .id(oktaGroup.getRoleName())
                .name(oktaGroup.getRoleName())
                .type("")
            .build();
    }

    public Collection<CustomGroup> findAll() {
        return this.oktaIdentityServiceClient
                .getGroups()
                .stream()
                .map(this::fromOktaGroup)
            .collect(Collectors.toList());
    }



    public List<CustomGroup> getGroupsForUser(final String userId){
        final List<OktaGroup> oktaGroups = this.oktaIdentityServiceClient.getUserGroups(userId);
        return oktaGroups.stream()
                .map(this::fromOktaGroup)
            .collect(Collectors.toList());
    }
}
