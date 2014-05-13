package kademlia.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import kademlia.dht.GetParameterFUC;
import kademlia.node.Node;
import kademlia.util.serializer.JsonSerializer;

/**
 * Messages used to send to another node requesting a newer version of a content that the node already has.
 *
 * @author Joshua Kissoon
 * @since 20140419
 */
public class ContentLookupMessageFUC implements Message
{

    public static final byte CODE = 0x29;

    private Node origin;
    private GetParameterFUC params;

    /**
     * @param origin The node where this lookup came from
     * @param params The parameters used to find the content
     */
    public ContentLookupMessageFUC(Node origin, GetParameterFUC params)
    {
        this.origin = origin;
        this.params = params;
    }

    public ContentLookupMessageFUC(DataInputStream in) throws IOException
    {
        this.fromStream(in);
    }

    public GetParameterFUC getParameters()
    {
        return this.params;
    }

    public Node getOrigin()
    {
        return this.origin;
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException
    {
        this.origin.toStream(out);

        /* Write the params to the stream */
        new JsonSerializer<GetParameterFUC>().write(this.params, out);
    }

    @Override
    public final void fromStream(DataInputStream in) throws IOException
    {
        this.origin = new Node(in);

        /* Read the params from the stream */
        try
        {
            this.params = new JsonSerializer<GetParameterFUC>().read(in);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public byte code()
    {
        return CODE;
    }

}
