package com.darlan.camunda.okta.client;

import com.darlan.camunda.okta.model.OktaGroup;
import com.darlan.camunda.okta.model.OktaUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "OktaIdentityServiceClient",
        url = "${okta.issuer.url}/api/v1/",
        configuration = OktaFeignConfiguration.class)
public interface OktaIdentityServiceClient {

    @GetMapping("/users")
    List<OktaUser> getUsers();

    @GetMapping("/groups")
    List<OktaGroup> getGroups();

    @GetMapping("/users/{userId}")
    OktaUser getUserById(@PathVariable("userId") String userId);

    @GetMapping("/groups/{groupId}/users")
    List<OktaUser> getUsersByGroupId(@PathVariable("groupId") String groupId);

    @GetMapping("/users/{userId}/groups")
    List<OktaGroup> getUserGroups(@PathVariable("userId") String userId);
}
