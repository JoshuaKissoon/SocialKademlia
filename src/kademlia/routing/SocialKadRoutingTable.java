package kademlia.routing;

import java.util.List;

/**
 * Extensions on a RoutingTable to make it work for Social Kademlia.
 * A connection is directly related to the Distributed Online Social Network(DOSN);
 * Every Actor(user) on the DOSN have connections (friends);
 * this extensions provide mechanisms for the routing table to keep track of connections.
 *
 * @author Joshua Kissoon
 * @created 20140501
 */
public interface SocialKadRoutingTable extends KadRoutingTable
{

    /**
     * Adds a Connection to the routing table
     *
     * @param c the new connection
     */
    public void insertConnection(Connection c);

    /**
     * Checks if this routing table contain a connection.
     *
     * @param c The connection to check for
     *
     * @return boolean
     */
    public boolean containsConnection(Connection c);

    /**
     * Remove a connection from this routing table.
     *
     * @param c The connection to remove
     *
     * @return Boolean whether the removal was successful.
     */
    public boolean removeConnection(Connection c);

    /**
     * @return An Iterable structure with all connections in this routing table
     */
    public List<Connection> getConnections();
}
