package kademlia.tests;

import java.util.Timer;
import java.util.TimerTask;
import kademlia.core.DefaultConfiguration;
import kademlia.Kademlia;
import kademlia.core.KadConfiguration;
import kademlia.node.NodeId;

/**
 * Testing the Kademlia Auto Content and Node table refresh operations
 *
 * @author Joshua Kissoon
 * @since 20140309
 */
public class AutoRefreshOperationTest2
{

    public AutoRefreshOperationTest2()
    {
        try
        {
            /* Setting up 2 Kad networks */
            final Kademlia kad1 = new Kademlia("JoshuaK", new NodeId("ASF456789djem4567463"), 12049);
            final Kademlia kad2 = new Kademlia("Crystal", new NodeId("AS84k678DJRW84567465"), 4585);
            final Kademlia kad3 = new Kademlia("Shameer", new NodeId("AS84k67894758456746A"), 8104);

            /* Connecting nodes */
            System.out.println("Connecting Nodes");
            kad2.bootstrap(kad1.getNode());
            kad3.bootstrap(kad2.getNode());

            DHTContentImpl c = new DHTContentImpl(new NodeId("AS84k678947584567465"), kad1.getOwnerId());
            c.setData("Setting the data");
            kad1.putLocally(c);

            System.out.println("\n Content ID: " + c.getKey());
            System.out.println(kad1.getNode() + " Distance from content: " + kad1.getNode().getNodeId().getDistance(c.getKey()));
            System.out.println(kad2.getNode() + " Distance from content: " + kad2.getNode().getNodeId().getDistance(c.getKey()));
            System.out.println(kad3.getNode() + " Distance from content: " + kad3.getNode().getNodeId().getDistance(c.getKey()));
            System.out.println("\nSTORING CONTENT 1 locally on " + kad1.getOwnerId() + "\n\n\n\n");

            System.out.println(kad1);
            System.out.println(kad2);
            System.out.println(kad3);

            /* Print the node states every few minutes */
            KadConfiguration config = new DefaultConfiguration();
            Timer timer = new Timer(true);
            timer.schedule(
                    new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            System.out.println(kad1);
                            System.out.println(kad2);
                            System.out.println(kad3);
                        }
                    },
                    // Delay                        // Interval
                    config.restoreInterval(), config.restoreInterval()
            );
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        new AutoRefreshOperationTest2();
    }
}
