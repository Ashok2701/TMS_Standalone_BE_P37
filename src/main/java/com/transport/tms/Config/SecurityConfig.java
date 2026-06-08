package com.transport.tms.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors -> cors.configurationSource(request -> {

                    CorsConfiguration config =
                            new CorsConfiguration();

                    // NO STAR HERE

                    config.setAllowedOrigins(
                            List.of(
                                    "http://localhost:3000",
                                    "https://localhost:3000",
                                    "http://tmssolutions.tema-systems.com:8081",
                                    "http://tmssolutions.tema-systems.com:8082",
                                    "https://id-preview--81d8c1e3-59ba-4d97-97af-217bbc48cd84.lovable.app",
                                    "https://preview--swiftroute-ui.lovable.app",
                                    "https://tmssolutions.tema-systems.com:8041"
                            ));

                    config.setAllowedMethods(
                            List.of("*"));

                    config.setAllowedHeaders(
                            List.of("*"));

                    config.setAllowCredentials(true);

                    return config;
                }))

                .formLogin(AbstractHttpConfigurer::disable)

                .httpBasic(AbstractHttpConfigurer::disable)

                .logout(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**")
                        .permitAll()

                        .requestMatchers(
                                "/api/v1/user/login")
                        .permitAll()

                        .requestMatchers(
                                "/api/v1/auth/login")
                        .permitAll()

                        .anyRequest()
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}