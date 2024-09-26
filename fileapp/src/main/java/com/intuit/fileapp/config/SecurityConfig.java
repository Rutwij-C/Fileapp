package com.intuit.fileapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.SecurityFilterChain;

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
                );

        return http.build();
    }
}
