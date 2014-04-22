package kademlia.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import kademlia.node.Node;
import kademlia.node.NodeId;

/**
 * A message sent to other nodes requesting the K-Closest nodes to a key sent in this message.
 *
 * @author Joshua Kissoon
 * @created 20140218
 */
public class NodeLookupMessage implements Message
{

    private Node origin;
    private NodeId lookupId;

    public static final byte CODE = 0x05;

    /**
     * A new NodeLookupMessage to find nodes
     *
     * @param origin The Node from which the message is coming from
     * @param lookup The key for which to lookup nodes for
     */
    public NodeLookupMessage(Node origin, NodeId lookup)
    {
        this.origin = origin;
        this.lookupId = lookup;
    }

    public NodeLookupMessage(DataInputStream in) throws IOException
    {
        this.fromStream(in);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException
    {
        this.origin = new Node(in);
        this.lookupId = new NodeId(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException
    {
        this.origin.toStream(out);
        this.lookupId.toStream(out);
    }

    public Node getOrigin()
    {
        return this.origin;
    }

    public NodeId getLookupId()
    {
        return this.lookupId;
    }

    @Override
    public byte code()
    {
        return CODE;
    }

    @Override
    public String toString()
    {
        return "NodeLookupMessage[origin=" + origin + ",lookup=" + lookupId + "]";
    }
}
