package kademlia.message;

import java.io.IOException;
import kademlia.core.KadConfiguration;
import kademlia.core.KadServer;
import kademlia.dht.DHT;
import kademlia.dht.StorageEntry;
import kademlia.node.Node;

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
    private final Node localNode;
    private final DHT dht;
    private final KadConfiguration config;

    public ContentLookupReceiverFUC(KadServer server, Node localNode, DHT dht, KadConfiguration config)
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
            StorageEntry se = this.dht.get(msg.getParameters());
            Message cMsg;

            if (se.getContentMetadata().getLastUpdatedTimestamp() > msg.getParameters().getLastUpdatedTimestamp())
            {
                cMsg = new ContentMessage(localNode, se);
            }
            else
            {
                /* We don't have a newer version, send an UpToDateContentMsg */
                cMsg = new UpToDateContentMessage(this.localNode);
            }
            server.reply(msg.getOrigin(), cMsg, comm);
        }
        else
        {
            /**
             * Return a the K closest nodes to this content identifier
             * We create a NodeLookupReceiver and let this receiver handle this operation
             */
            NodeLookupMessage lkpMsg = new NodeLookupMessage(msg.getOrigin(), msg.getParameters().getKey());
            new NodeLookupReceiver(server, localNode, this.config).receive(lkpMsg, comm);
        }
    }

    @Override
    public void timeout(int comm)
    {

    }
}
