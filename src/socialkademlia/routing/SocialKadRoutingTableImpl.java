package socialkademlia.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import kademlia.KadConfiguration;
import kademlia.node.Node;
import kademlia.routing.JKademliaRoutingTable;

/**
 * Implementation of SocialKadRoutingTable
 *
 * @author Joshua Kissoon
 * @since 20140501
 */
public class SocialKadRoutingTableImpl extends JKademliaRoutingTable implements SocialKadRoutingTable
{

    /* Connections of the actor of this node in the DOSN */
    private final HashMap<String, Connection> connections;

    
    {
        connections = new HashMap<>();
    }

    public SocialKadRoutingTableImpl(Node localNode, KadConfiguration config)
    {
        super(localNode, config);
    }

    @Override
    public synchronized void insertConnection(String actorId, Node node)
    {
        if (this.containsConnection(actorId))
        {
            Connection c = this.connections.get(actorId);
            c.setNode(node);
            c.setSeenNow();
        }
        else
        {
            this.connections.put(actorId, new Connection(actorId, node));
        }
    }

    @Override
    public synchronized boolean containsConnection(String actorId)
    {
        return this.connections.containsKey(actorId);
    }

    @Override
    public synchronized boolean removeConnection(String actorId)
    {
        if (this.containsConnection(actorId))
        {
            this.connections.remove(actorId);
            return true;
        }
        return false;
    }

    @Override
    public synchronized List<Connection> getConnections()
    {
        return this.connections.isEmpty() ? new ArrayList<>() : new ArrayList<>(this.connections.values());
    }

    @Override
    public synchronized Node getConnectionNode(String actorId)
    {
        if (this.containsConnection(actorId))
        {
            return this.connections.get(actorId).getNode();
        }
        else
        {
            throw new NoSuchElementException("This connection does not exist.");
        }
    }
}
