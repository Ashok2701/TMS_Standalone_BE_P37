//package com.transport.tms.Config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class GlobalCorsConfig {
//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/**")
//                        .allowedOrigins(
//                                "http://localhost:3000",
//                                "https://localhost:3000",
//                                "http://tmssolutions.tema-systems.com:8081",
//                                "http://tmssolutions.tema-systems.com:8082",
//                                "https://id-preview--81d8c1e3-59ba-4d97-97af-217bbc48cd84.lovable.app",
//                                "https://preview--swiftroute-ui.lovable.app",
//                                "https://tmssolutions.tema-systems.com:8041"
//                        )
//                        .allowedMethods("*")
//                        .allowedHeaders("*")
//                        .allowCredentials(true);
//            }
//        };
//    }
//}
