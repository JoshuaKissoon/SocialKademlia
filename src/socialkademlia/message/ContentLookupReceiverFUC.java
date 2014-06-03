package socialkademlia.message;

import java.io.IOException;
import socialkademlia.JSocialKademliaNode;
import kademlia.KadConfiguration;
import kademlia.KadServer;
import kademlia.message.Message;
import kademlia.message.NodeLookupMessage;
import kademlia.message.Receiver;
import socialkademlia.dht.SocialKademliaDHT;
import socialkademlia.dht.SocialKademliaStorageEntry;

/**
 * Responds to a ContentLookupMessage for updated content by sending a ContentLookupMessageFUC containing the requested content information;
 * if the requested content is not found or the version stored locally is not newer than the one the sender has, an UpToDateContent message is sent.
 *
 * @author Joshua Kissoon
 * @since 20140419
 */
public class ContentLookupReceiverFUC implements Receiver
{
    
    private final KadServer server;
    private final JSocialKademliaNode localNode;
    private final SocialKademliaDHT dht;
    private final KadConfiguration config;
    
    public ContentLookupReceiverFUC(KadServer server, JSocialKademliaNode localNode, SocialKademliaDHT dht, KadConfiguration config)
    {
        this.server = server;
        this.localNode = localNode;
        this.dht = dht;
        this.config = config;
    }
    
    @Override
    public void receive(Message incoming, int comm) throws IOException
    {
        ContentLookupMessageFUC msg = (ContentLookupMessageFUC) incoming;
        this.localNode.getRoutingTable().insert(msg.getOrigin());

        /* Check if we can have this data */
        if (this.dht.contains(msg.getParameters()))
        {
            /* Return a ContentMessage with the required data if it's a newer version */
            SocialKademliaStorageEntry se = this.dht.get(msg.getParameters());

            /* Only if this node is one of the K-Closest or the owner, we return the content */
            if (se.getContentMetadata().isKNode() || se.getContentMetadata().getOwnerId().equals(localNode.getOwnerId()))
            {
                Message cMsg;
                
                if (se.getContentMetadata().getLastUpdatedTimestamp() > msg.getParameters().getLastUpdatedTimestamp())
                {
                    cMsg = new ContentMessage(localNode.getNode(), se);
                }
                else
                {
                    /* We don't have a newer version, send an UpToDateContentMsg */
                    cMsg = new UpToDateContentMessage(this.localNode.getNode());
                }
                server.reply(msg.getOrigin(), cMsg, comm);
                return;
            }
        }

        /**
         * Return a the K closest nodes to this content identifier
         * We create a NodeLookupReceiver and let this receiver handle this operation
         */
        NodeLookupMessage lkpMsg = new NodeLookupMessage(msg.getOrigin(), msg.getParameters().getKey());
        new NodeLookupReceiver(server, localNode, this.config).receive(lkpMsg, comm);
    }
    
    @Override
    public void timeout(int comm)
    {
        
    }
}
