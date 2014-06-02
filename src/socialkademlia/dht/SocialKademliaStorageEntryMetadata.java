package socialkademlia.dht;

import kademlia.dht.KademliaStorageEntryMetadata;

/**
 * Metadata class for SocialKademlia storage entries
 *
 * @author Joshua Kissoon
 * @since 20140602
 */
public interface SocialKademliaStorageEntryMetadata extends KademliaStorageEntryMetadata
{

    /**
     * Specify that this content should be cached.
     */
    public void setCached();

    /**
     * Specify whether this content should be cached.
     *
     * @param cached
     */
    public void setCached(boolean cached);

    /**
     * @return Whether this content is cached or not
     */
    public boolean isCached();

    /**
     * Specify that this node is one of the k-closest to the content.
     */
    public void setKNode();

    /**
     * Specify whether this node is a k-node of this content.
     *
     * @param cached
     */
    public void setKNode(boolean cached);

    /**
     * @return Whether this node is a K-Node for this content
     */
    public boolean isKNode();
}
