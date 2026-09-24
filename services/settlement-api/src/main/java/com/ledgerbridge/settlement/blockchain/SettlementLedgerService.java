package com.ledgerbridge.settlement.blockchain;

import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple6;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;

@Service
public class SettlementLedgerService {

    private final Web3j web3j;
    private final BlockchainProperties blockchainProperties;

    public SettlementLedgerService(
            Web3j web3j,
            BlockchainProperties blockchainProperties) {

        this.web3j = web3j;
        this.blockchainProperties = blockchainProperties;
    }

    public TransactionReceipt recordSettlement(
            String transactionId,
            String sourceAccount,
            String destinationAccount,
            BigInteger amount,
            String currency) throws Exception {

        Credentials credentials = getCredentials();

       SettlementLedgerClient contract = SettlementLedgerClient.connect(
                blockchainProperties.getSettlementContractAddress(),
                web3j,
                credentials,
                new DefaultGasProvider()
        );

        return contract.recordSettlement(
                transactionId,
                sourceAccount,
                destinationAccount,
                amount,
                currency
        ).send();
    }

    public Tuple6<String, String, String, BigInteger, String, BigInteger>
    getSettlement(String transactionId) throws Exception {

        Credentials credentials = getCredentials();

        SettlementLedgerClient contract = SettlementLedgerClient.connect(
                        blockchainProperties.getSettlementContractAddress(),
                        web3j,
                        credentials,
                        new DefaultGasProvider()
        );

        return contract.getSettlement(transactionId).send();
    }

    private Credentials getCredentials() {

        String privateKey =
                System.getenv("LEDGERBRIDGE_DEPLOYER_PRIVATE_KEY");

        if (privateKey == null || privateKey.isBlank()) {
            throw new IllegalStateException(
                    "LEDGERBRIDGE_DEPLOYER_PRIVATE_KEY environment variable is not set");
        }

        return Credentials.create(privateKey);
    }
}