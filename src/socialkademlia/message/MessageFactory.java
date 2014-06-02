package socialkademlia.message;

import java.io.DataInputStream;
import java.io.IOException;
import socialkademlia.JSocialKademliaNode;
import kademlia.KadConfiguration;
import kademlia.KadServer;
import kademlia.message.AcknowledgeMessage;
import kademlia.message.ConnectMessage;
import kademlia.message.ConnectReceiver;
import kademlia.message.ContentLookupMessage;
import kademlia.message.ContentMessage;
import kademlia.message.KademliaMessageFactory;
import kademlia.message.Message;
import kademlia.message.NodeLookupMessage;
import kademlia.message.NodeLookupReceiver;
import kademlia.message.NodeReplyMessage;
import kademlia.message.Receiver;
import kademlia.message.SimpleMessage;
import kademlia.message.SimpleReceiver;
import kademlia.message.StoreContentMessage;
import socialkademlia.dht.SocialKademliaDHT;

/**
 * Handles creating messages and receivers
 *
 * @author Joshua Kissoon
 * @since 20140202
 */
public class MessageFactory implements KademliaMessageFactory
{

    private final JSocialKademliaNode localNode;
    private final SocialKademliaDHT dht;
    private final KadConfiguration config;

    public MessageFactory(JSocialKademliaNode local, SocialKademliaDHT dht, KadConfiguration config)
    {
        this.localNode = local;
        this.dht = dht;
        this.config = config;
    }

    @Override
    public Message createMessage(byte code, DataInputStream in) throws IOException
    {
        switch (code)
        {
            case AcknowledgeMessage.CODE:
                return new AcknowledgeMessage(in);
            case ConnectMessage.CODE:
                return new ConnectMessage(in);
            case ContentMessage.CODE:
                return new ContentMessage(in);
            case ContentLookupMessage.CODE:
                return new ContentLookupMessage(in);
            case NodeLookupMessage.CODE:
                return new NodeLookupMessage(in);
            case NodeReplyMessage.CODE:
                return new NodeReplyMessage(in);
            case SimpleMessage.CODE:
                return new SimpleMessage(in);
            case StoreContentMessage.CODE:
                return new StoreContentMessage(in);
            case ContentLookupMessageFUC.CODE:
                return new ContentLookupMessageFUC(in);
            case UpToDateContentMessage.CODE:
                return new UpToDateContentMessage(in);
            default:
                //System.out.println(this.localNode + " - No Message handler found for message. Code: " + code);
                return new SimpleMessage(in);

        }
    }

    @Override
    public Receiver createReceiver(byte code, KadServer server)
    {
        switch (code)
        {
            case ConnectMessage.CODE:
                return new ConnectReceiver(server, this.localNode);
            case ContentLookupMessage.CODE:
                return new ContentLookupReceiver(server, this.localNode, this.dht, this.config);
            case NodeLookupMessage.CODE:
                return new NodeLookupReceiver(server, this.localNode, this.config);
            case StoreContentMessage.CODE:
                return new StoreContentReceiver(server, this.localNode, this.dht);
            case ContentLookupMessageFUC.CODE:
                return new ContentLookupReceiverFUC(server, this.localNode, this.dht, this.config);
            default:
                //System.out.println("No receiver found for message. Code: " + code);
                return new SimpleReceiver();
        }
    }
}
