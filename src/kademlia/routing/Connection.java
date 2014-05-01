package kademlia.routing;

import kademlia.node.Node;

/**
 * Keeps information about connections of an actor (user) on the DOSN; Contacts are stored in the Buckets in the Routing Table.
 * Very similar to the contact class.
 *
 * @author Joshua Kissoon
 * @since 20140501
 */
public class Connection implements Comparable<Connection>
{

    private final Node n;
    private long lastSeen;

    /**
     * Create a contact object
     *
     * @param n The node associated with this contact
     */
    public Connection(Node n)
    {
        this.n = n;
        this.lastSeen = System.currentTimeMillis() / 1000L;
    }

    public Node getNode()
    {
        return this.n;
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

    @Override
    public int compareTo(Connection o)
    {
        if (this.getNode().equals(o.getNode()))
        {
            return 0;
        }

        return (this.lastSeen() > o.lastSeen()) ? 1 : -1;
    }

}
