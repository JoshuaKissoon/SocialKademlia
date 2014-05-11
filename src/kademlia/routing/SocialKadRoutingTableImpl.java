package kademlia.routing;

import java.util.ArrayList;
import java.util.List;
import kademlia.core.KadConfiguration;
import kademlia.node.Node;

/**
 * Implementation of SocialKadRoutingTable
 *
 * @author Joshua Kissoon
 * @since 20140501
 */
public class SocialKadRoutingTableImpl extends RoutingTable implements SocialKadRoutingTable
{

    /* Connections of the actor of this node in the DOSN */
    private final List<Connection> connections;

    
    {
        connections = new ArrayList<>();
    }

    public SocialKadRoutingTableImpl(Node localNode, KadConfiguration config)
    {
        super(localNode, config);
    }

    @Override
    public void insertConnection(Connection c)
    {
        if (this.containsConnection(c))
        {
            int index = this.connections.indexOf(c);
            this.connections.get(index).setSeenNow();
        }
        else
        {
            this.connections.add(c);
        }
    }

    @Override
    public boolean containsConnection(Connection c)
    {
        return this.connections.contains(c);
    }

    @Override
    public boolean removeConnection(Connection c)
    {
        if (this.containsConnection(c))
        {
            return this.connections.remove(c);
        }

        return false;
    }

    @Override
    public List<Connection> getConnections()
    {
        return this.connections;
    }
}
