package com.ledgerbridge.settlement.blockchain;

import com.ledgerbridge.settlement.blockchain.generated.SettlementLedger;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple6;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;

public class SettlementLedgerDeployer {

    private static final String BESU_RPC_URL = "http://localhost:8545";

    private static final String CONTRACT_ADDRESS =
            "0xbca0fdc68d9b21b5bfb16d784389807017b2bbbc";

    public static void main(String[] args) throws Exception {

        String privateKey =
                System.getenv("LEDGERBRIDGE_DEPLOYER_PRIVATE_KEY");

        if (privateKey == null || privateKey.isBlank()) {
            throw new IllegalStateException(
                    "LEDGERBRIDGE_DEPLOYER_PRIVATE_KEY environment variable is not set");
        }

        Web3j web3j =
                Web3j.build(new HttpService(BESU_RPC_URL));

        try {
            Credentials credentials =
                    Credentials.create(privateKey);

            System.out.println("Connecting to SettlementLedger...");
            System.out.println(
                    "Deployer address: " + credentials.getAddress());
            System.out.println(
                    "Contract address: " + CONTRACT_ADDRESS);

            SettlementLedger contract =
                    SettlementLedger.load(
                            CONTRACT_ADDRESS,
                            web3j,
                            credentials,
                            new DefaultGasProvider()
                    );

            System.out.println("Recording settlement...");

            TransactionReceipt receipt =
                    contract.recordSettlement(
                            "demo-settlement-001",
                            "BANK-A-001",
                            "BANK-B-002",
                            BigInteger.valueOf(100000),
                            "USD"
                    ).send();

            System.out.println("Settlement recorded successfully.");
            System.out.println(
                    "Transaction hash: " + receipt.getTransactionHash());

            System.out.println("Reading settlement from blockchain...");

            Tuple6<String, String, String, BigInteger, String, BigInteger>
                    settlement =
                    contract.getSettlement(
                            "demo-settlement-001"
                    ).send();

            System.out.println("Settlement retrieved:");
            System.out.println("Transaction ID: " + settlement.component1());
            System.out.println("Source account: " + settlement.component2());
            System.out.println("Destination account: " + settlement.component3());
            System.out.println("Amount: " + settlement.component4());
            System.out.println("Currency: " + settlement.component5());
            System.out.println("Timestamp: " + settlement.component6());

        } finally {
            web3j.shutdown();
        }
    }
}