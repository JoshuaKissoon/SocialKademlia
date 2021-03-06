package socialkademlia.simulations;

import java.io.IOException;
import socialkademlia.JSocialKademliaNode;
import socialkademlia.dht.GetParameterFUC;
import socialkademlia.exceptions.UpToDateContentException;
import kademlia.node.KademliaId;
import kademlia.simulations.DHTContentImpl;
import socialkademlia.dht.SocialKademliaStorageEntry;

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
    JSocialKademliaNode[] kads;

    public long startTime, endTime, timeTaken;

    public GetUpdatedContentPerformance()
    {
        try
        {
            /* Setting up Kad networks */
            kads = new JSocialKademliaNode[NUM_KADS];

            kads[0] = new JSocialKademliaNode("user0", new KademliaId("HRF456789SD584567460"), 1335);
            kads[1] = new JSocialKademliaNode("user1", new KademliaId("ASF456789475DS567461"), 1206);
            kads[2] = new JSocialKademliaNode("user2", new KademliaId("HRF456789SD584567463"), 4586);
            kads[3] = new JSocialKademliaNode("user3", new KademliaId("HRF456789SD584567464"), 8107);
            kads[4] = new JSocialKademliaNode("user4", new KademliaId("HRF456789SD584567465"), 8336);
            kads[5] = new JSocialKademliaNode("user5", new KademliaId("HRF456789SD584567466"), 13346);
            kads[6] = new JSocialKademliaNode("user6", new KademliaId("HRF456789SD584567468"), 12050);
            kads[7] = new JSocialKademliaNode("user7", new KademliaId("HRF456789SD584567433"), 14586);
            kads[8] = new JSocialKademliaNode("user8", new KademliaId("HRF456789SD58456746A"), 18105);
            kads[9] = new JSocialKademliaNode("user9", new KademliaId("HRF456789SD58456746B"), 18336);

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
        GetParameterFUC gp = new GetParameterFUC(c);
        startTime = System.nanoTime();
        for (int i = 0; i < NUM_RUNS; i++)
        {
            try
            {
                SocialKademliaStorageEntry cc = kads[1].getUpdated(gp);
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
