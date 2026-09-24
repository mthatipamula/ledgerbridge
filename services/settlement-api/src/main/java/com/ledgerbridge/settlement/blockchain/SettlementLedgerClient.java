package com.ledgerbridge.settlement.blockchain;

import com.ledgerbridge.settlement.blockchain.generated.SettlementLedger;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.ContractGasProvider;

public class SettlementLedgerClient extends SettlementLedger {

    protected SettlementLedgerClient(
            String contractAddress,
            Web3j web3j,
            Credentials credentials,
            ContractGasProvider gasProvider) {

        super(
                contractAddress,
                web3j,
                credentials,
                gasProvider
        );
    }

    public static SettlementLedgerClient connect(
            String contractAddress,
            Web3j web3j,
            Credentials credentials,
            ContractGasProvider gasProvider) {

        return new SettlementLedgerClient(
                contractAddress,
                web3j,
                credentials,
                gasProvider
        );
    }
}