package com.ledgerbridge.settlement.blockchain;

import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Component
public class BesuClient {

    private static final String BESU_RPC_URL = "http://localhost:8545";

    private final Web3j web3j;

    public BesuClient() {
        this.web3j = Web3j.build(new HttpService(BESU_RPC_URL));
    }

    public Web3j getWeb3j() {
        return web3j;
    }
}