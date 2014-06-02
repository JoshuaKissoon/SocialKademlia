package socialkademlia.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import kademlia.message.Message;
import kademlia.node.Node;
import kademlia.util.serializer.JsonSerializer;
import socialkademlia.dht.SocialKademliaStorageEntry;

/**
 * A Message used to send content between nodes
 *
 * @author Joshua Kissoon
 * @since 20140226
 */
public class SocialKademliaContentMessage implements Message
{

    public static final byte CODE = 0x04;

    private SocialKademliaStorageEntry content;
    private Node origin;

    /**
     * @param origin  Where the message came from
     * @param content The content to be stored
     *
     */
    public SocialKademliaContentMessage(Node origin, SocialKademliaStorageEntry content)
    {
        this.content = content;
        this.origin = origin;
    }

    public SocialKademliaContentMessage(DataInputStream in) throws IOException
    {
        this.fromStream(in);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException
    {
        this.origin.toStream(out);

        /* Serialize the KadContent, then send it to the stream */
        new JsonSerializer<SocialKademliaStorageEntry>().write(content, out);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException
    {
        this.origin = new Node(in);

        try
        {
            this.content = new JsonSerializer<SocialKademliaStorageEntry>().read(in);
        }
        catch (ClassNotFoundException e)
        {
            System.err.println("ClassNotFoundException when reading StorageEntry; Message: " + e.getMessage());
        }
    }

    public Node getOrigin()
    {
        return this.origin;
    }

    public SocialKademliaStorageEntry getContent()
    {
        return this.content;
    }

    @Override
    public byte code()
    {
        return CODE;
    }

    @Override
    public String toString()
    {
        return "ContentMessage[origin=" + origin + ",content=" + content + "]";
    }
}
