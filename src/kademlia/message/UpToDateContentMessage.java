package kademlia.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import kademlia.node.Node;

/**
 * A message used to when a ContentLookupMessageFUC is received where the node is looking for an updated version of a content,
 * If we don't have a newer version, we send this message to tell them they have the latest version.
 *
 * @author Joshua Kissoon
 * @created 20140419
 */
public class UpToDateContentMessage implements Message
{

    private Node origin;
    public static final byte CODE = 0x10;

    public UpToDateContentMessage(Node origin)
    {
        this.origin = origin;
    }

    public UpToDateContentMessage(DataInputStream in) throws IOException
    {
        this.fromStream(in);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException
    {
        this.origin = new Node(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException
    {
        origin.toStream(out);
    }

    public Node getOrigin()
    {
        return this.origin;
    }

    @Override
    public byte code()
    {
        return CODE;
    }

    @Override
    public String toString()
    {
        return "UpToDateContentMessage[origin=" + origin.getNodeId() + "]";
    }
}
