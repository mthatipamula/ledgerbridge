package com.ledgerbridge.mcpclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class McpClientApplication {

    public static void main(String[] args) {

        SpringApplication application =
                new SpringApplication(McpClientApplication.class);

        application.setWebApplicationType(WebApplicationType.SERVLET);
        application.run(args);
    }
}