package socialkademlia.dht;

import kademlia.dht.KademliaStorageEntry;

/**
 * Storage Entry interface for SocialKademlia.
 *
 * @author Joshua Kissoon
 * @since 20140602
 */
public interface SocialKademliaStorageEntry extends KademliaStorageEntry
{

    /**
     * Set the content value of this storage entry
     *
     * @param data
     */
    public void setContent(String data);

    /**
     * @return Whether this entry is compressed or not
     */
    public boolean isCompressed();

    /**
     * Specify that the storage entry is compressed
     */
    public void setCompressed();

    /**
     * Specify that the storage entry is decompressed
     */
    public void setDecompressed();

    /**
     * @return The content in string format
     */
    public String getContentString();

    @Override
    public SocialKademliaStorageEntryMetadata getContentMetadata();
}
