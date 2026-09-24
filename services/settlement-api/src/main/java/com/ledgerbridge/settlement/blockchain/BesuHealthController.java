package com.ledgerbridge.settlement.blockchain;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.protocol.core.methods.response.EthBlockNumber;

import java.io.IOException;
import java.math.BigInteger;

@RestController
public class BesuHealthController {

    private final BesuClient besuClient;

    public BesuHealthController(BesuClient besuClient) {
        this.besuClient = besuClient;
    }

    @GetMapping("/api/v1/blockchain/status")
    public BlockchainStatus getStatus() throws IOException {

        EthBlockNumber response =
                besuClient.getWeb3j()
                        .ethBlockNumber()
                        .send();

        BigInteger blockNumber = response.getBlockNumber();

        return new BlockchainStatus(
                "CONNECTED",
                blockNumber.longValue()
        );
    }

    public record BlockchainStatus(
            String status,
            long blockNumber
    ) {}
}