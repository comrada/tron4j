package com.github.comrada.tron4j.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.comrada.tron4j.abi.FunctionEncoder;
import com.github.comrada.tron4j.abi.TypeReference;
import com.github.comrada.tron4j.abi.datatypes.Address;
import com.github.comrada.tron4j.abi.datatypes.Bool;
import com.github.comrada.tron4j.abi.datatypes.Function;
import com.github.comrada.tron4j.abi.datatypes.generated.Uint256;
import com.github.comrada.tron4j.api.GrpcAPI.EmptyMessage;
import com.github.comrada.tron4j.proto.Chain.Transaction;
import com.github.comrada.tron4j.proto.Contract.TriggerSmartContract;
import com.github.comrada.tron4j.proto.Response.BlockExtention;
import com.github.comrada.tron4j.proto.Response.TransactionExtention;
import com.github.comrada.tron4j.proto.Response.TransactionReturn;

import java.math.BigInteger;
import java.util.Arrays;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;

public class ClientTest {
    @Test
    public void testGetNowBlockQuery() {
        TronClient client = TronClient.ofShasta("3333333333333333333333333333333333333333333333333333333333333333");
        BlockExtention block = client.blockingStub.getNowBlock2(EmptyMessage.newBuilder().build());

        System.out.println(block.getBlockHeader());
        assertTrue(block.getBlockHeader().getRawDataOrBuilder().getNumber() > 0);
    }

    @Test
    public void testSendTrc20Transaction() {
        TronClient client = TronClient.ofNile("3333333333333333333333333333333333333333333333333333333333333333");

        // transfer(address,uint256) returns (bool)
        Function trc20Transfer = new Function("transfer",
            Arrays.asList(new Address("TVjsyZ7fYF3qLF6BQgPmTEZy1xrNNyVAAA"),
                new Uint256(BigInteger.valueOf(10).multiply(BigInteger.valueOf(10).pow(18)))),
            Arrays.asList(new TypeReference<Bool>() {}));

        String encodedHex = FunctionEncoder.encode(trc20Transfer);

        TriggerSmartContract trigger =
            TriggerSmartContract.newBuilder()
                .setOwnerAddress(TronClient.parseAddress("TJRabPrwbZy45sbavfcjinPJC18kjpRTv8"))
                .setContractAddress(TronClient.parseAddress("TF17BgPaZYbz8oxbjhriubPDsA7ArKoLX3"))
                .setData(TronClient.parseHex(encodedHex))
                .build();

        System.out.println("trigger:\n" + trigger);

        TransactionExtention txnExt = client.blockingStub.triggerContract(trigger);
        System.out.println("txn id => " + Hex.toHexString(txnExt.getTxid().toByteArray()));

        Transaction signedTxn = client.signTransaction(txnExt);

        System.out.println(signedTxn.toString());
        TransactionReturn ret = client.blockingStub.broadcastTransaction(signedTxn);
        System.out.println("======== Result ========\n" + ret.toString());
    }

    @Test
    public void testGenerateAddress() {
        TronClient.generateAddress();
    }
}
