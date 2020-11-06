package com.darlan.camunda.okta.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OktaUser {

    private String id;
    private Profile profile;
    private List<OktaGroup> roles;

    public String getUsername() {
        return profile.getLogin();
    }

    public String getFirstName() {
        return profile.getFirstName();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Profile {
        private String firstName;
        private String lastName;
        private String mobilePhone;
        private String secondEmail;
        private String login;
        private String email;
    }

}
