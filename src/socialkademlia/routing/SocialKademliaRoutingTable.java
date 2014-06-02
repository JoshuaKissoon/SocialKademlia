package socialkademlia.routing;

import java.util.List;
import kademlia.node.Node;
import kademlia.routing.KademliaRoutingTable;

/**
 * Extensions on a RoutingTable to make it work for Social Kademlia.
 * A connection is directly related to the Distributed Online Social Network(DOSN);
 * Every Actor(user) on the DOSN have connections (friends);
 * this extensions provide mechanisms for the routing table to keep track of connections.
 *
 * @author Joshua Kissoon
 * @created 20140501
 */
public interface SocialKademliaRoutingTable extends KademliaRoutingTable
{

    /**
     * Adds a Connection to the routing table
     *
     * @param actorId The actor Id of this connection
     * @param node    The node related to this connection
     */
    public void insertConnection(String actorId, Node node);

    /**
     * Checks if this routing table contain a connection.
     *
     * @param actorId The id of the actor
     *
     * @return boolean
     */
    public boolean containsConnection(String actorId);

    /**
     * Remove a connection from this routing table.
     *
     * @param actorId
     *
     * @return Boolean whether the removal was successful.
     */
    public boolean removeConnection(String actorId);

    /**
     * @param actorId
     *
     * @return The node of a given connection
     */
    public Node getConnectionNode(String actorId);

    /**
     * @return An Iterable structure with all connections in this routing table
     */
    public List<Connection> getConnections();
}
