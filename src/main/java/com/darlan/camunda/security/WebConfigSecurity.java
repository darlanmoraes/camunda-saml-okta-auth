package com.darlan.camunda.security;

import org.camunda.bpm.webapp.impl.security.auth.ContainerBasedAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml2.credentials.Saml2X509Credential;
import org.springframework.security.saml2.credentials.Saml2X509Credential.Saml2X509CredentialType;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.Filter;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;

@EnableWebSecurity
public class WebConfigSecurity extends WebSecurityConfigurerAdapter {

    private static final String CERT_PATH = "saml-cert.pem";
    private static final String CERT_PROTOCOL = "X.509";

    @Value("${okta.sso.url}")
    private String ssoUrl;
    @Value("${okta.idp.entity.id}")
    private String entityId;
    @Value("${okta.idp.entity.local.id}")
    private String localEntityId;
    @Value("${okta.acs.url}")
    private String acsUrl;
    @Value("${okta.registration.id}")
    private String registrationId;

    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;

    public WebConfigSecurity(final CustomLogoutSuccessHandler customLogoutSuccessHandler) {
        this.customLogoutSuccessHandler = customLogoutSuccessHandler;
    }

    @Bean
    public RelyingPartyRegistration oktaRegistration() throws CertificateException {
        final InputStream is = this.getClass().getClassLoader().getResourceAsStream(CERT_PATH);
        final CertificateFactory factory = CertificateFactory.getInstance(CERT_PROTOCOL);
        final X509Certificate certificate = (X509Certificate) factory.generateCertificate(is);
        final Saml2X509Credential credential = new Saml2X509Credential(certificate,
                Saml2X509CredentialType.VERIFICATION, Saml2X509CredentialType.ENCRYPTION);

        return RelyingPartyRegistration.withRegistrationId(registrationId)
                .providerDetails(config -> config.entityId(entityId))
                .providerDetails(config -> config.webSsoUrl(ssoUrl))
                .providerDetails(config -> config.signAuthNRequest(false))
                .credentials(credentials -> credentials.add(credential))
                .localEntityIdTemplate(localEntityId)
                .assertionConsumerServiceUrlTemplate(acsUrl)
            .build();
    }

    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository(final RelyingPartyRegistration oktaRegistration) {
        return new InMemoryRelyingPartyRegistrationRepository(oktaRegistration);
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
            .csrf().ignoringAntMatchers("/camunda/api/**")
        .and()
            .antMatcher("/**")
            .authorizeRequests()
            .antMatchers("/camunda/app/**")
            .authenticated()
            .anyRequest()
            .permitAll()
        .and()
            .saml2Login()
            .defaultSuccessUrl("/camunda/app/welcome/default/#!/welcome")
        .and()
            .logout()
            .logoutRequestMatcher(new AntPathRequestMatcher("/**/logout"))
            .logoutSuccessHandler(customLogoutSuccessHandler);
    }

    @Bean
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public FilterRegistrationBean containerBasedAuthenticationFilter(){
        final FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setName("camunda-container-auth");
        registration.setFilter(new ContainerBasedAuthenticationFilter());
        registration.setInitParameters(Collections.singletonMap("authentication-provider",
                "com.darlan.camunda.okta.authentication.OktaAuthenticationProvider"));
        registration.setOrder(101);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public FilterRegistrationBean processEngineAuthenticationFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setName("camunda-engine-auth");
        registration.setFilter(this.getProcessEngineAuthenticationFilter());
        registration.addInitParameter("authentication-provider",
                "com.darlan.camunda.config.ProcessEngineAuthenticationFilter");
        registration.setOrder(101);
        registration.addUrlPatterns("/engine-rest/*");
        return registration;
    }

    @Bean
    public Filter getProcessEngineAuthenticationFilter() {
        return new ProcessEngineAuthenticationFilter();
    }

}
