package com.intuit.fileapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("api/object_metadata/**", "/api/audit_log").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2Login((oauth2) -> oauth2 // Explicitly using OAuth2LoginConfigurer
                        .loginPage("/oauth2/authorization/google") // Customize this for your provider
                        .successHandler(redirectToFrontendSuccessHandler()) // Redirect after successful login
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler redirectToFrontendSuccessHandler() {
        return new SimpleUrlAuthenticationSuccessHandler("http://app.fileapp.click/metadata");
    }
}
