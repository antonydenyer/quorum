package org.web3j.quorum;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;


import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Ignore;
import org.junit.Test;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.http.HttpService;
import org.web3j.qourum.tx.QuorumTransactionManager;
import org.web3j.quorum.generated.Greeter;
import org.web3j.quorum.tx.ClientTransactionManager;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.web3j.tx.Contract.GAS_LIMIT;
import static org.web3j.tx.ManagedTransaction.GAS_PRICE;

/**
 * Useful integration tests for verifying Quorum deployments and transaction privacy.
 *
 * <p>To use, simply run:
 *
 * <code>git clone https://github.com/blk-io/crux.git</code>
 * <code>docker-compose -f docker/quorum-crux/docker-compose.yaml up</code>
 *
 * <p>Then you're good to go!
 */

@Ignore
public class RawTransactionManagerTest {

    // Node details are those of blk.io's Sample Quorum with Crux network, available at:
    // https://github.com/blk-io/crux/tree/master/docker/quorum-crux/
    private static final Node quorum1 = new Node(
            "0xed9d02e382b34818e88b88a309c7fe71e65f419d",
            Arrays.asList(
                    "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo="),
            "http://localhost:22001");

    private static final Node quorum2 = new Node(
            "0xca843569e3427144cead5e4d5999a3d0ccf92b8e",
            Arrays.asList(
                    "QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc="),
            "http://localhost:22002");

    private static final Node quorum3 = new Node(
            "0x0fbdc686b912d7722dc86510934589e0aaf3b55a",
            Arrays.asList(
                    "1iTZde/ndBHvzhcl7V68x44Vx7pl8nwx9LqnM/AfJUg="),
            "http://localhost:22003");

    private static final Node quorum4 = new Node(
            "0x9186eb3d20cbd1f5f992a950d808c4495153abd5",
            Arrays.asList(
                    "oNspPPgszVUFw0qmGFfWwh1uxVUXgvBxleXORHj07g8="),
            "http://localhost:22004");

    private static final List<Node> nodes = Arrays.asList(
            quorum1, quorum2, quorum3, quorum4);


    @Test
    public void testNodes() throws Exception {

        // Send transactions between all sets of nodes
        for (int count = 0; count < 1; count++) {
            for (int i = 0; i < nodes.size(); i++) {
                Node sourceNode = nodes.get(i);
                Node destNode = nodes.get((i + 1) % nodes.size());
                String requestId = Integer.toString(i);
                testRawTransactionsWithGreeterContract(sourceNode, destNode,requestId);

            }
        }
    }

    public void testRawTransactionsWithGreeterContract (Node sourceNode,
                                                        Node destNode,
                                                        String requestId) throws Exception {

        Quorum quorum = Quorum.build(new HttpService(sourceNode.getUrl()));
        String account = quorum.ethAccounts().send().getAccounts().get(0);
        Credentials credentials = Credentials.create(account);

        QuorumTransactionManager transactionManager = new QuorumTransactionManager(
                quorum,
                credentials,
                sourceNode.getPublicKeys().get(0),
                destNode.getPublicKeys(),
                5000,
                5);

        String greeting = "Hello Quorum world! [" + requestId + "]";

        Greeter contract = Greeter.deploy(
                quorum,
                transactionManager,
                BigInteger.ZERO,
                Contract.GAS_LIMIT,
                greeting).send();

        Thread.sleep(10000);

        System.out.println(contract.getContractAddress());
        String test = contract.greet().send();


        assertThat(test , is(greeting));
    }

    private void runPrivateGreeterTest(
            Node sourceNode, Node destNode, String requestId) throws Exception {
        Quorum quorum = Quorum.build(new HttpService(sourceNode.getUrl()));

        ClientTransactionManager transactionManager =
                new ClientTransactionManager(
                        quorum,
                        sourceNode.getAddress(),
                        sourceNode.getPublicKeys().get(0),
                        destNode.getPublicKeys());

        String greeting = "Hello Quorum world! [" + requestId + "]";
        Greeter contract = Greeter.deploy(
                quorum, transactionManager,
                GAS_PRICE, GAS_LIMIT,
                greeting).send();

        MatcherAssert.assertThat(contract.greet().send(), Is.is(greeting));
    }
    public void testRawTransaction(Node sourceNode, Node destNode, String requestId) throws Exception {

    }
}