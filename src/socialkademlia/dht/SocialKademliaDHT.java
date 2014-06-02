package socialkademlia.dht;

import java.io.IOException;
import java.util.NoSuchElementException;
import kademlia.dht.GetParameter;
import kademlia.dht.KadContent;
import kademlia.dht.KademliaDHT;

/**
 * Interface that specifies the methods of SocialKademlia's DHT
 *
 * @author Joshua Kissoon
 * @since 20140602
 */
public interface SocialKademliaDHT extends KademliaDHT
{

    /**
     * Handle storing content locally to keep the content cached.
     *
     * We set that this content is a cached entry and that this node is not one of the k-nodes.
     *
     * @param content The DHT content to store
     *
     * @return boolean true if we stored the content, false if the content already exists and is up to date
     *
     * @throws java.io.IOException
     */
    public boolean cache(SocialKademliaStorageEntry content) throws IOException;

    public boolean cache(KadContent content) throws IOException;

    /**
     * Update a content; the operation is only done iff we already have a copy of the content here
     *
     * @param newContent The content to update.
     *
     * @throws java.io.IOException
     */
    public void update(SocialKademliaStorageEntry newContent) throws IOException;

    @Override
    public SocialKademliaStorageEntry get(GetParameter param) throws NoSuchElementException, IOException;
}
