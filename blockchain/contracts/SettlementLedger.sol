// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

contract SettlementLedger {

    struct Settlement {
        string transactionId;
        string sourceAccount;
        string destinationAccount;
        uint256 amount;
        string currency;
        uint256 timestamp;
    }

    mapping(string => Settlement) private settlements;

    event SettlementRecorded(
        string indexed transactionId,
        string sourceAccount,
        string destinationAccount,
        uint256 amount,
        string currency,
        uint256 timestamp
    );

    function recordSettlement(
        string calldata transactionId,
        string calldata sourceAccount,
        string calldata destinationAccount,
        uint256 amount,
        string calldata currency
    ) external {

        require(bytes(transactionId).length > 0, "transactionId required");
        require(bytes(sourceAccount).length > 0, "sourceAccount required");
        require(bytes(destinationAccount).length > 0, "destinationAccount required");
        require(amount > 0, "amount must be greater than zero");
        require(bytes(currency).length > 0, "currency required");

        require(
            settlements[transactionId].timestamp == 0,
            "settlement already exists"
        );

        settlements[transactionId] = Settlement({
            transactionId: transactionId,
            sourceAccount: sourceAccount,
            destinationAccount: destinationAccount,
            amount: amount,
            currency: currency,
            timestamp: block.timestamp
        });

        emit SettlementRecorded(
            transactionId,
            sourceAccount,
            destinationAccount,
            amount,
            currency,
            block.timestamp
        );
    }

    function getSettlement(
        string calldata transactionId
    ) external view returns (
        string memory,
        string memory,
        string memory,
        uint256,
        string memory,
        uint256
    ) {
        Settlement memory settlement = settlements[transactionId];

        require(
            settlement.timestamp != 0,
            "settlement not found"
        );

        return (
            settlement.transactionId,
            settlement.sourceAccount,
            settlement.destinationAccount,
            settlement.amount,
            settlement.currency,
            settlement.timestamp
        );
    }
}