package com.ledgerbridge.settlement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.ledgerbridge.settlement.blockchain.BlockchainProperties;

@SpringBootApplication
@EnableConfigurationProperties(BlockchainProperties.class)
public class SettlementApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SettlementApiApplication.class, args);
    }
}