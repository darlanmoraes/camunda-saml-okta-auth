package com.darlan.camunda.custom.service;

import com.darlan.camunda.okta.client.OktaIdentityServiceClient;
import com.darlan.camunda.custom.entity.CustomUser;
import com.darlan.camunda.okta.model.OktaUser;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final OktaIdentityServiceClient oktaIdentityServiceClient;

    public UserService(OktaIdentityServiceClient oktaIdentityServiceClient) {
        this.oktaIdentityServiceClient = oktaIdentityServiceClient;
    }

    private CustomUser fromOktaUser(final OktaUser oktaUser){
        return CustomUser.builder()
                .id(oktaUser.getUsername())
                .email(oktaUser.getUsername())
                .firstName(oktaUser.getFirstName())
                .lastName("")
            .build();
    }

    public CustomUser findById(String id) {
        return this.fromOktaUser(this.oktaIdentityServiceClient.getUserById(id));
    }

    public Collection<CustomUser> findAll() {
        final List<OktaUser> oktaUsers = this.oktaIdentityServiceClient.getUsers();
        return oktaUsers.stream()
                .map(this::fromOktaUser)
            .collect(Collectors.toList());
    }

    public Collection<CustomUser> findByGroupId(String groupId){
        return this.oktaIdentityServiceClient
            .getUsersByGroupId(groupId).stream()
                .map(this::fromOktaUser)
            .collect(Collectors.toList());
    }

}
