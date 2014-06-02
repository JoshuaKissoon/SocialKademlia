package socialkademlia.simulations;

import socialkademlia.simulations.RoutingTableSimulation;
import socialkademlia.JSocialKademliaNode;
import kademlia.node.KademliaId;
import socialkademlia.routing.SocialKadRoutingTable;

/**
 * Testing how the routing table works and checking if everything works properly
 *
 * @author Joshua Kissoon
 * @since 20140426
 */
public class RoutingTableSimulation
{

    public RoutingTableSimulation()
    {
        try
        {
            /* Setting up 2 Kad networks */
            JSocialKademliaNode kad1 = new JSocialKademliaNode("JoshuaK", new KademliaId("ASF45678947584567463"), 12049);
            JSocialKademliaNode kad2 = new JSocialKademliaNode("Crystal", new KademliaId("ASF45678947584567464"), 4585);
            JSocialKademliaNode kad3 = new JSocialKademliaNode("Shameer", new KademliaId("ASF45678947584567465"), 8104);
            JSocialKademliaNode kad4 = new JSocialKademliaNode("Lokesh", new KademliaId("ASF45678947584567466"), 8335);
            JSocialKademliaNode kad5 = new JSocialKademliaNode("Chandu", new KademliaId("ASF45678947584567467"), 13345);

            SocialKadRoutingTable rt = kad1.getRoutingTable();
            
            rt.insert(kad2.getNode());
            rt.insert(kad3.getNode());
            rt.insert(kad4.getNode());
            System.out.println(rt);
            
            rt.insert(kad5.getNode());            
            System.out.println(rt);
            
            rt.insert(kad3.getNode());            
            System.out.println(rt);
            
            
            /* Lets shut down a node and then try putting a content on the network. We'll then see how the un-responsive contacts work */
        }
        catch (IllegalStateException e)
        {

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        new RoutingTableSimulation();
    }
}
