// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.msalobosample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtClaimsSetVerifier;
import org.springframework.security.oauth2.provider.token.store.jwk.JwkTokenStore;

@Slf4j
@Configuration
public class SecurityResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Value("${security.oauth2.resource.jwt.key-uri}")
    private String keySetUri;

    @Value("${security.oauth2.client.client-id}")
    private String applicationId;

    @Value("${security.oauth2.aad.aliases}")
    private String[] aadAliases;

    @Value("${security.oauth2.accepted.tenants}")
    private String[] acceptedTenants;

    @Value("${security.oauth2.scope.access-as-user}")
    private String accessAsUserScope;

    private final String AAD_SCOPE_CLAIM = "scp";

    @Override
    public void configure(HttpSecurity http) throws Exception {
        log.info("---------------- Security configure called ----------------");
            http
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .authorizeRequests()
                    .antMatchers("/test/*")
                    .access("#oauth2.hasScope('" + accessAsUserScope + "')"); // required scope to access /api URL
        }

    @Bean
    public TokenStore tokenStore() {
        JwkTokenStore jwkTokenStore = new JwkTokenStore(keySetUri, accessTokenConverter(), claimSetVerifier());
        //get the full token store here by passing the url, converter method and claims verifier
        return jwkTokenStore;
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        log.info("---------------- token converter called ----------------");
        JwtAccessTokenConverter jwtConverter = new JwtAccessTokenConverter();

        DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
        accessTokenConverter.setScopeAttribute(AAD_SCOPE_CLAIM);

        jwtConverter.setAccessTokenConverter(accessTokenConverter);
            // new converter is created and the scope is set as scp
        return jwtConverter;
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources){
        // we need to set resourceId to null so that spring doesn't try to verify this.
        // this is because the aud claim is variable in AAD (e.g. clientId or api://clientId ).
        // we then verify this in our custom verifier (AADClaimsVerifier)
        resources.resourceId(null);
    }

    @Bean
    public JwtClaimsSetVerifier claimSetVerifier() {
        return new AADClaimsVerifier(aadAliases, acceptedTenants, applicationId);
    }
}