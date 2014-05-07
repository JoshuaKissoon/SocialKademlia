package kademlia.tests;

import java.io.IOException;
import kademlia.KademliaNode;
import kademlia.dht.GetParameterFUC;
import kademlia.dht.StorageEntry;
import kademlia.exceptions.UpToDateContentException;
import kademlia.node.NodeId;

/**
 * Test the performance of the GetUpdatedContent RPC in comparison to the getContentRPC
 *
 * @author Joshua Kissoon
 * @since 20140507
 */
public class GetUpdatedContentPerformance
{

    public final static int NUM_RUNS = 1000;
    public final static int NUM_KADS = 10;

    DHTContentImpl c;
    KademliaNode[] kads;

    public long startTime, endTime, timeTaken;

    public GetUpdatedContentPerformance()
    {
        try
        {
            /* Setting up Kad networks */
            kads = new KademliaNode[NUM_KADS];

            kads[0] = new KademliaNode("user0", new NodeId("HRF456789SD584567460"), 1334);
            kads[1] = new KademliaNode("user1", new NodeId("ASF456789475DS567461"), 1209);
            kads[2] = new KademliaNode("user2", new NodeId("AFG45678947584567462"), 4585);
            kads[3] = new KademliaNode("user3", new NodeId("FSF45J38947584567463"), 8104);
            kads[4] = new KademliaNode("user4", new NodeId("ASF45678947584567464"), 8335);
            kads[5] = new KademliaNode("user5", new NodeId("GHF4567894DR84567465"), 13345);
            kads[6] = new KademliaNode("user6", new NodeId("ASF45678947584567466"), 12049);
            kads[7] = new KademliaNode("user7", new NodeId("AE345678947584567467"), 14585);
            kads[8] = new KademliaNode("user8", new NodeId("ASAA5678947584567468"), 18104);
            kads[9] = new KademliaNode("user9", new NodeId("ASF456789475845674U9"), 18335);

            for (int i = 1; i < NUM_KADS; i++)
            {
                kads[i].bootstrap(kads[0].getNode());
            }

            String originalData = "some data ";
            String data = "";
            for (int i = 0; i < 50; i++)
            {
                data += originalData;
            }

            c = new DHTContentImpl("Joshua", data);

            kads[1].put(c);
        }
        catch (IOException ex)
        {

        }

        /* Lets do the get updated content operations */
        startTime = System.nanoTime();
        for (int i = 0; i < NUM_RUNS; i++)
        {
            try
            {
                StorageEntry cc = kads[1].getUpdated(new GetParameterFUC(c));
            }
            catch (UpToDateContentException ex)
            {

            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        endTime = System.nanoTime();
        timeTaken = (endTime - startTime) / 1000000L;  // milliseconds
        System.out.println("GUC Operation time: " + timeTaken);
        System.out.println(kads[1].getStatistician());

    }

    public static void main(String[] args)
    {
        new GetUpdatedContentPerformance();
    }
}
