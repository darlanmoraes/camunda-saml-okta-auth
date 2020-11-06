package com.darlan.camunda;

import org.camunda.bpm.engine.impl.plugin.AdministratorAuthorizationPlugin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

@SpringBootApplication
@EnableFeignClients
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Value("${camunda.bpm.admin.username}")
    private String administratorUsername;

    @Bean
    @Primary
    @Order(Integer.MAX_VALUE - 1)
    public AdministratorAuthorizationPlugin administratorAuthorizationPlugin() {
        final AdministratorAuthorizationPlugin plugin = new AdministratorAuthorizationPlugin();
        plugin.setAdministratorUserName(administratorUsername);
        return plugin;
    }

}
