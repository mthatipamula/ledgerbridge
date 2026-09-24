package com.ledgerbridge.settlement.blockchain.generated;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple6;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/LFDT-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.14.0.
 */
@SuppressWarnings("rawtypes")
public class SettlementLedger extends Contract {
    public static final String BINARY = "6080604052348015600e575f5ffd5b5061114e8061001c5f395ff3fe608060405234801561000f575f5ffd5b5060043610610034575f3560e01c80630d1126411461003857806398bb9c6314610054575b5f5ffd5b610052600480360381019061004d919061080f565b610089565b005b61006e60048036038101906100699190610905565b610470565b604051610080969594939291906109cf565b60405180910390f35b5f89899050116100ce576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016100c590610a94565b60405180910390fd5b5f8787905011610113576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161010a90610afc565b60405180910390fd5b5f8585905011610158576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161014f90610b64565b60405180910390fd5b5f831161019a576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161019190610bcc565b60405180910390fd5b5f82829050116101df576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016101d690610c34565b60405180910390fd5b5f5f8a8a6040516101f1929190610c8e565b90815260200160405180910390206005015414610243576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161023a90610cf0565b60405180910390fd5b6040518060c001604052808a8a8080601f0160208091040260200160405190810160405280939291908181526020018383808284375f81840152601f19601f82011690508083019250505050505050815260200188888080601f0160208091040260200160405190810160405280939291908181526020018383808284375f81840152601f19601f82011690508083019250505050505050815260200186868080601f0160208091040260200160405190810160405280939291908181526020018383808284375f81840152601f19601f82011690508083019250505050505050815260200184815260200183838080601f0160208091040260200160405190810160405280939291908181526020018383808284375f81840152601f19601f820116905080830192505050505050508152602001428152505f8a8a60405161038d929190610c8e565b90815260200160405180910390205f820151815f0190816103ae9190610f49565b5060208201518160010190816103c49190610f49565b5060408201518160020190816103da9190610f49565b506060820151816003015560808201518160040190816103fa9190610f49565b5060a082015181600501559050508888604051610418929190610c8e565b60405180910390207f3d189e06a3e1f460c3949dddd2f00f5e75cf6552b66f257acdedd9327836c6eb888888888888884260405161045d989796959493929190611044565b60405180910390a2505050505050505050565b60608060605f60605f5f5f898960405161048b929190610c8e565b90815260200160405180910390206040518060c00160405290815f820180546104b390610d68565b80601f01602080910402602001604051908101604052809291908181526020018280546104df90610d68565b801561052a5780601f106105015761010080835404028352916020019161052a565b820191905f5260205f20905b81548152906001019060200180831161050d57829003601f168201915b5050505050815260200160018201805461054390610d68565b80601f016020809104026020016040519081016040528092919081815260200182805461056f90610d68565b80156105ba5780601f10610591576101008083540402835291602001916105ba565b820191905f5260205f20905b81548152906001019060200180831161059d57829003601f168201915b505050505081526020016002820180546105d390610d68565b80601f01602080910402602001604051908101604052809291908181526020018280546105ff90610d68565b801561064a5780601f106106215761010080835404028352916020019161064a565b820191905f5260205f20905b81548152906001019060200180831161062d57829003601f168201915b505050505081526020016003820154815260200160048201805461066d90610d68565b80601f016020809104026020016040519081016040528092919081815260200182805461069990610d68565b80156106e45780601f106106bb576101008083540402835291602001916106e4565b820191905f5260205f20905b8154815290600101906020018083116106c757829003601f168201915b5050505050815260200160058201548152505090505f8160a001510361073f576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610736906110fa565b60405180910390fd5b805f015181602001518260400151836060015184608001518560a00151965096509650965096509650509295509295509295565b5f5ffd5b5f5ffd5b5f5ffd5b5f5ffd5b5f5ffd5b5f5f83601f84011261079c5761079b61077b565b5b8235905067ffffffffffffffff8111156107b9576107b861077f565b5b6020830191508360018202830111156107d5576107d4610783565b5b9250929050565b5f819050919050565b6107ee816107dc565b81146107f8575f5ffd5b50565b5f81359050610809816107e5565b92915050565b5f5f5f5f5f5f5f5f5f60a08a8c03121561082c5761082b610773565b5b5f8a013567ffffffffffffffff81111561084957610848610777565b5b6108558c828d01610787565b995099505060208a013567ffffffffffffffff81111561087857610877610777565b5b6108848c828d01610787565b975097505060408a013567ffffffffffffffff8111156108a7576108a6610777565b5b6108b38c828d01610787565b955095505060606108c68c828d016107fb565b93505060808a013567ffffffffffffffff8111156108e7576108e6610777565b5b6108f38c828d01610787565b92509250509295985092959850929598565b5f5f6020838503121561091b5761091a610773565b5b5f83013567ffffffffffffffff81111561093857610937610777565b5b61094485828601610787565b92509250509250929050565b5f81519050919050565b5f82825260208201905092915050565b8281835e5f83830152505050565b5f601f19601f8301169050919050565b5f61099282610950565b61099c818561095a565b93506109ac81856020860161096a565b6109b581610978565b840191505092915050565b6109c9816107dc565b82525050565b5f60c0820190508181035f8301526109e78189610988565b905081810360208301526109fb8188610988565b90508181036040830152610a0f8187610988565b9050610a1e60608301866109c0565b8181036080830152610a308185610988565b9050610a3f60a08301846109c0565b979650505050505050565b7f7472616e73616374696f6e4964207265717569726564000000000000000000005f82015250565b5f610a7e60168361095a565b9150610a8982610a4a565b602082019050919050565b5f6020820190508181035f830152610aab81610a72565b9050919050565b7f736f757263654163636f756e74207265717569726564000000000000000000005f82015250565b5f610ae660168361095a565b9150610af182610ab2565b602082019050919050565b5f6020820190508181035f830152610b1381610ada565b9050919050565b7f64657374696e6174696f6e4163636f756e7420726571756972656400000000005f82015250565b5f610b4e601b8361095a565b9150610b5982610b1a565b602082019050919050565b5f6020820190508181035f830152610b7b81610b42565b9050919050565b7f616d6f756e74206d7573742062652067726561746572207468616e207a65726f5f82015250565b5f610bb660208361095a565b9150610bc182610b82565b602082019050919050565b5f6020820190508181035f830152610be381610baa565b9050919050565b7f63757272656e63792072657175697265640000000000000000000000000000005f82015250565b5f610c1e60118361095a565b9150610c2982610bea565b602082019050919050565b5f6020820190508181035f830152610c4b81610c12565b9050919050565b5f81905092915050565b828183375f83830152505050565b5f610c758385610c52565b9350610c82838584610c5c565b82840190509392505050565b5f610c9a828486610c6a565b91508190509392505050565b7f736574746c656d656e7420616c726561647920657869737473000000000000005f82015250565b5f610cda60198361095a565b9150610ce582610ca6565b602082019050919050565b5f6020820190508181035f830152610d0781610cce565b9050919050565b7f4e487b71000000000000000000000000000000000000000000000000000000005f52604160045260245ffd5b7f4e487b71000000000000000000000000000000000000000000000000000000005f52602260045260245ffd5b5f6002820490506001821680610d7f57607f821691505b602082108103610d9257610d91610d3b565b5b50919050565b5f819050815f5260205f209050919050565b5f6020601f8301049050919050565b5f82821b905092915050565b5f60088302610df47fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff82610db9565b610dfe8683610db9565b95508019841693508086168417925050509392505050565b5f819050919050565b5f610e39610e34610e2f846107dc565b610e16565b6107dc565b9050919050565b5f819050919050565b610e5283610e1f565b610e66610e5e82610e40565b848454610dc5565b825550505050565b5f5f905090565b610e7d610e6e565b610e88818484610e49565b505050565b5f5b82811015610eae57610ea35f828401610e75565b600181019050610e8f565b505050565b601f821115610f015782821115610f0057610ecd81610d98565b610ed683610daa565b610edf85610daa565b6020861015610eec575f90505b808301610efb82840382610e8d565b505050505b5b505050565b5f82821c905092915050565b5f610f215f1984600802610f06565b1980831691505092915050565b5f610f398383610f12565b9150826002028217905092915050565b610f5282610950565b67ffffffffffffffff811115610f6b57610f6a610d0e565b5b610f758254610d68565b610f80828285610eb3565b5f60209050601f831160018114610fb1575f8415610f9f578287015190505b610fa98582610f2e565b865550611010565b601f198416610fbf86610d98565b5f5b82811015610fe657848901518255600182019150602085019450602081019050610fc1565b868310156110035784890151610fff601f891682610f12565b8355505b6001600288020188555050505b505050505050565b5f611023838561095a565b9350611030838584610c5c565b61103983610978565b840190509392505050565b5f60a0820190508181035f83015261105d818a8c611018565b9050818103602083015261107281888a611018565b905061108160408301876109c0565b8181036060830152611094818587611018565b90506110a360808301846109c0565b9998505050505050505050565b7f736574746c656d656e74206e6f7420666f756e640000000000000000000000005f82015250565b5f6110e460148361095a565b91506110ef826110b0565b602082019050919050565b5f6020820190508181035f830152611111816110d8565b905091905056fea264697066735822122057ad1623e5269a8dfa69ecb20ad6cb674c78c1a7de49d6253498d70270a2d66c64736f6c63430008250033";

    private static String librariesLinkedBinary;

    public static final String FUNC_GETSETTLEMENT = "getSettlement";

    public static final String FUNC_RECORDSETTLEMENT = "recordSettlement";

    public static final Event SETTLEMENTRECORDED_EVENT = new Event("SettlementRecorded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>(true) {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected SettlementLedger(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected SettlementLedger(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected SettlementLedger(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected SettlementLedger(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<SettlementRecordedEventResponse> getSettlementRecordedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(SETTLEMENTRECORDED_EVENT, transactionReceipt);
        ArrayList<SettlementRecordedEventResponse> responses = new ArrayList<SettlementRecordedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            SettlementRecordedEventResponse typedResponse = new SettlementRecordedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.transactionId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.sourceAccount = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.destinationAccount = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.currency = (String) eventValues.getNonIndexedValues().get(3).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static SettlementRecordedEventResponse getSettlementRecordedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(SETTLEMENTRECORDED_EVENT, log);
        SettlementRecordedEventResponse typedResponse = new SettlementRecordedEventResponse();
        typedResponse.log = log;
        typedResponse.transactionId = (byte[]) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.sourceAccount = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.destinationAccount = (String) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        typedResponse.currency = (String) eventValues.getNonIndexedValues().get(3).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
        return typedResponse;
    }

    public Flowable<SettlementRecordedEventResponse> settlementRecordedEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getSettlementRecordedEventFromLog(log));
    }

    public Flowable<SettlementRecordedEventResponse> settlementRecordedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(SETTLEMENTRECORDED_EVENT));
        return settlementRecordedEventFlowable(filter);
    }

    public RemoteFunctionCall<Tuple6<String, String, String, BigInteger, String, BigInteger>> getSettlement(
            String transactionId) {
        final Function function = new Function(FUNC_GETSETTLEMENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(transactionId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple6<String, String, String, BigInteger, String, BigInteger>>(function,
                new Callable<Tuple6<String, String, String, BigInteger, String, BigInteger>>() {
                    @Override
                    public Tuple6<String, String, String, BigInteger, String, BigInteger> call()
                            throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple6<String, String, String, BigInteger, String, BigInteger>(
                                (String) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (String) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue(), 
                                (String) results.get(4).getValue(), 
                                (BigInteger) results.get(5).getValue());
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> recordSettlement(String transactionId,
            String sourceAccount, String destinationAccount, BigInteger amount, String currency) {
        final Function function = new Function(
                FUNC_RECORDSETTLEMENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(transactionId), 
                new org.web3j.abi.datatypes.Utf8String(sourceAccount), 
                new org.web3j.abi.datatypes.Utf8String(destinationAccount), 
                new org.web3j.abi.datatypes.generated.Uint256(amount), 
                new org.web3j.abi.datatypes.Utf8String(currency)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static SettlementLedger load(String contractAddress, Web3j web3j,
            Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new SettlementLedger(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static SettlementLedger load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new SettlementLedger(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static SettlementLedger load(String contractAddress, Web3j web3j,
            Credentials credentials, ContractGasProvider contractGasProvider) {
        return new SettlementLedger(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static SettlementLedger load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new SettlementLedger(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<SettlementLedger> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return deployRemoteCall(SettlementLedger.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<SettlementLedger> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(SettlementLedger.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    public static RemoteCall<SettlementLedger> deploy(Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(SettlementLedger.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<SettlementLedger> deploy(Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(SettlementLedger.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    public static void linkLibraries(List<Contract.LinkReference> references) {
        librariesLinkedBinary = linkBinaryWithReferences(BINARY, references);
    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class SettlementRecordedEventResponse extends BaseEventResponse {
        public byte[] transactionId;

        public String sourceAccount;

        public String destinationAccount;

        public BigInteger amount;

        public String currency;

        public BigInteger timestamp;
    }
}
