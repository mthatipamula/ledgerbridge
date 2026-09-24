package com.ledgerbridge.settlement.blockchain;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ledgerbridge.blockchain")
public class BlockchainProperties {

    private String rpcUrl;
    private String settlementContractAddress;

    public String getRpcUrl() {
        return rpcUrl;
    }

    public void setRpcUrl(String rpcUrl) {
        this.rpcUrl = rpcUrl;
    }

    public String getSettlementContractAddress() {
        return settlementContractAddress;
    }

    public void setSettlementContractAddress(String settlementContractAddress) {
        this.settlementContractAddress = settlementContractAddress;
    }
}