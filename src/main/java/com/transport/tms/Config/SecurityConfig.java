package com.transport.tms.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

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

                        .requestMatchers(
                                "/api/pod/auth/login")
                        .permitAll()

                        // Real enforcement, scoped deliberately narrow for
                        // now: everything ELSE under /api/pod/** requires a
                        // valid token (checked by JwtAuthenticationFilter
                        // below). No other /api/pod/** endpoint exists yet
                        // — this is forward-looking for the next POD
                        // service (trips/stops/POD submission), so it's
                        // enforced correctly from the moment it's built
                        // rather than retrofitted later. Every OTHER
                        // existing endpoint in the app is untouched
                        // (anyRequest().permitAll() below, same as before)
                        // to avoid breaking anything that isn't part of
                        // this POD work.
                        .requestMatchers(
                                "/api/pod/**")
                        .authenticated()

                        .anyRequest()
                        .permitAll()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}