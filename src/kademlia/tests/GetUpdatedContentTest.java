package kademlia.tests;

import java.io.IOException;
import java.util.List;
import kademlia.Kademlia;
import kademlia.dht.GetParameterFUC;
import kademlia.dht.StorageEntry;
import kademlia.exceptions.UpToDateContentException;
import kademlia.node.NodeId;

/**
 * We put the content on the network, and then check for an up to date version.
 *
 * @author Joshua Kissoon
 * @since 20140224
 */
public class GetUpdatedContentTest
{

    public static void main(String[] args)
    {

        try
        {
            /* Setting up 2 Kad networks */
            Kademlia kad1 = new Kademlia("JoshuaK", new NodeId("ASF45678947584567467"), 7574);
            Kademlia kad2 = new Kademlia("Crystal", new NodeId("ASERTKJDHGVHERJHGFLK"), 7572);

            /* Create 2 content */
            DHTContentImpl c1 = new DHTContentImpl(kad2.getOwnerId(), "Joshua Book 1st Edition.");
            DHTContentImpl c2 = new DHTContentImpl(kad2.getOwnerId(), "Joshua Bio 1st Edition.");
            /* Connect the 2 nodes */
            kad2.bootstrap(kad1.getNode());

            /* Lets share the contents */
            kad1.put(c1);
            kad1.put(c2);

            for (long i = 0; i < 9999990000L; i++)
            {

            }

            /* Update c2 and store it on kad1 only */
            System.out.println("\n\n ************** Updating C2");
            long c2OldTs = c2.getLastUpdatedTimestamp();
            c2.setData("Joshua Bio 2nd Edition");
            kad1.putLocally(c2);

            /* Get updated version of c1 */
            try
            {
                /* Lets see if there is an updated version of the content */
                System.out.println("\n\nRetrieving Content C1");
                GetParameterFUC gp = new GetParameterFUC(c1.getKey(), DHTContentImpl.TYPE, c1.getOwnerId(), c1.getLastUpdatedTimestamp());
                List<StorageEntry> conte = kad2.getUpdated(gp, 4);
                for (StorageEntry cc : conte)
                {
                    System.out.println("Content Found: " + new DHTContentImpl().fromBytes(cc.getContent().getBytes()));
                    System.out.println("Content Metadata: " + cc.getContentMetadata());
                }
            }
            catch (IOException | UpToDateContentException ex)
            {
                System.err.println(ex.getMessage());
            }

            /* Get updated version of C2 */
            try
            {
                /* Lets retrieve the content c2 if it's updated */
                System.out.println("\n\n\n****************Retrieving Content c2 Again");
                
                /* We use c2 old TS, since that's the TS of the version kad2 has */
                GetParameterFUC gp = new GetParameterFUC(c2.getKey(), DHTContentImpl.TYPE, c2.getOwnerId(), c2OldTs);
                List<StorageEntry> conte = kad2.getUpdated(gp, 4);
                for (StorageEntry cc : conte)
                {
                    System.out.println("Updated Content Found: " + new DHTContentImpl().fromBytes(cc.getContent().getBytes()));
                    System.out.println("Updated Content Metadata: " + cc.getContentMetadata());
                }
            }
            catch (IOException | UpToDateContentException ex)
            {
                System.err.println(ex.getMessage());
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
