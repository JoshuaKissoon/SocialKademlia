package socialkademlia.routing;

import kademlia.node.Node;

/**
 * Keeps information about connections of an actor (user) on the DOSN; Contacts are stored in the Buckets in the Routing Table.
 * Very similar to the contact class.
 *
 * @author Joshua Kissoon
 * @since 20140501
 */
public class Connection
{

    private Node n;
    private long lastSeen;
    private final String actorId;

    /**
     * Create a contact object
     *
     * @param n       The node associated with this contact
     * @param actorId
     */
    public Connection(String actorId, Node n)
    {
        this.n = n;
        this.actorId = actorId;
        this.lastSeen = System.currentTimeMillis() / 1000L;
    }

    public Node getNode()
    {
        return this.n;
    }

    public void setNode(Node n)
    {
        this.n = n;
    }

    public String getConnectionId()
    {
        return this.actorId;
    }

    /**
     * When a Node sees a contact a gain, the Node will want to update that it's seen recently,
     * this method updates the last seen timestamp for this contact.
     */
    public void setSeenNow()
    {
        this.lastSeen = System.currentTimeMillis() / 1000L;
    }

    /**
     * When last was this contact seen?
     *
     * @return long The last time this contact was seen.
     */
    public long lastSeen()
    {
        return this.lastSeen;
    }

    public boolean equals(Connection c)
    {
        return c.getNode().equals(this.getNode());
    }
}
