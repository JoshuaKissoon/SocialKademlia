package kademlia.simulations;

import kademlia.simulations.DHTContentImpl;
import java.io.IOException;
import kademlia.KademliaNode;
import kademlia.dht.GetParameter;
import kademlia.dht.StorageEntry;
import kademlia.exceptions.ContentNotFoundException;
import kademlia.node.KademliaId;

/**
 * Test the performance of the GetUpdatedContent RPC in comparison to the getContentRPC
 *
 * @author Joshua Kissoon
 * @since 20140507
 */
public class GetContentPerformance
{

    public final static int NUM_RUNS = 1000;
    public final static int NUM_KADS = 10;

    DHTContentImpl c;
    KademliaNode[] kads;

    public long startTime, endTime, timeTaken;

    public GetContentPerformance()
    {
        try
        {
            /* Setting up Kad networks */
            kads = new KademliaNode[NUM_KADS];

            kads[0] = new KademliaNode("user0", new KademliaId("HRF456789SD584567460"), 1335);
            kads[1] = new KademliaNode("user1", new KademliaId("ASF456789475DS567461"), 1206);
            kads[2] = new KademliaNode("user2", new KademliaId("HRF456789SD584567463"), 4586);
            kads[3] = new KademliaNode("user3", new KademliaId("HRF456789SD584567464"), 8107);
            kads[4] = new KademliaNode("user4", new KademliaId("HRF456789SD584567465"), 8336);
            kads[5] = new KademliaNode("user5", new KademliaId("HRF456789SD584567466"), 13346);
            kads[6] = new KademliaNode("user6", new KademliaId("HRF456789SD584567468"), 12050);
            kads[7] = new KademliaNode("user7", new KademliaId("HRF456789SD584567433"), 14586);
            kads[8] = new KademliaNode("user8", new KademliaId("HRF456789SD58456746A"), 18105);
            kads[9] = new KademliaNode("user9", new KademliaId("HRF456789SD58456746B"), 18336);

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

            c = new DHTContentImpl(new KademliaId("HRF456789SD584567463"), "Joshua");
            c.setData(data);

            kads[0].put(c);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

        /* Lets do the get operations */
        GetParameter gp = new GetParameter(c);
        startTime = System.nanoTime();
        for (int i = 0; i < NUM_RUNS; i++)
        {
            try
            {
                StorageEntry cc = kads[1].get(gp);
            }
            catch (ContentNotFoundException | IOException ex)
            {

            }
        }
        endTime = System.nanoTime();
        timeTaken = (endTime - startTime) / 1000000L;  // milliseconds
        System.out.println("Get Operation time: " + timeTaken);
        System.out.println(kads[1].getStatistician());
    }

    public static void main(String[] args)
    {
        new GetContentPerformance();
    }
}
