package socialkademlia.dht;

import kademlia.dht.KadContent;

/**
 * A StorageEntry class that is used to store a content on the DHT
 *
 * @author Joshua Kissoon
 * @since 20140402
 */
public class StorageEntry implements SocialKademliaStorageEntry
{

    private String content;
    private final SocialKademliaStorageEntryMetadata metadata;
    private boolean isCompressed;       // Is this entry in compressed form

    
    {
        this.isCompressed = false;
    }

    public StorageEntry(final KadContent content)
    {
        this(content, new JSocialKademliaStorageEntryMetadata(content));
    }

    public StorageEntry(final KadContent content, final SocialKademliaStorageEntryMetadata metadata)
    {
        this.setContent(content.toSerializedForm());
        this.metadata = metadata;
    }

    @Override
    public final void setContent(final byte[] data)
    {
        this.content = new String(data);
    }

    public final void setContent(String data)
    {
        this.content = data;
    }

    @Override
    public final byte[] getContent()
    {
        return this.content.getBytes();
    }

    public final String getContentString()
    {
        return this.content;
    }

    public final SocialKademliaStorageEntryMetadata getContentMetadata()
    {
        return this.metadata;
    }

    public boolean isCompressed()
    {
        return this.isCompressed;
    }

    public void setCompressed()
    {
        this.isCompressed = true;
    }

    public void setDecompressed()
    {
        this.isCompressed = false;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("[StorageEntry: ");

        sb.append("[Content: ");
        sb.append(this.getContent());
        sb.append("]");

        sb.append(this.getContentMetadata());

        sb.append("]");

        return sb.toString();
    }
}
