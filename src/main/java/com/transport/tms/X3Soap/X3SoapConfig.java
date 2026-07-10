package com.transport.tms.X3Soap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class X3SoapConfig {

    @Value("${x3.soap.url}")
    private String soapUrl;

    @Value("${x3.soap.username}")
    private String username;

    @Value("${x3.soap.password}")
    private String password;

    @Bean
    public X3SoapService x3SoapService() {
        return new X3SoapService(soapUrl, username, password);
    }
}
