package socialkademlia.dht;

import kademlia.dht.KadContent;

/**
 * A JSocialKademliaStorageEntry class that is used to store a content on the DHT
 *
 * @author Joshua Kissoon
 * @since 20140402
 */
public class JSocialKademliaStorageEntry implements SocialKademliaStorageEntry
{

    private String content;
    private final JSocialKademliaStorageEntryMetadata metadata;
    private boolean isCompressed;       // Is this entry in compressed form

    
    {
        this.isCompressed = false;
    }

    public JSocialKademliaStorageEntry(final KadContent content)
    {
        this(content, new JSocialKademliaStorageEntryMetadata(content));
    }

    public JSocialKademliaStorageEntry(final KadContent content, final JSocialKademliaStorageEntryMetadata metadata)
    {
        this.setContent(content.toSerializedForm());
        this.metadata = metadata;
    }

    @Override
    public final void setContent(final byte[] data)
    {
        this.content = new String(data);
    }

    @Override
    public final void setContent(String data)
    {
        this.content = data;
    }

    @Override
    public final byte[] getContent()
    {
        return this.content.getBytes();
    }

    @Override
    public final String getContentString()
    {
        return this.content;
    }

    @Override
    public final SocialKademliaStorageEntryMetadata getContentMetadata()
    {
        return this.metadata;
    }

    @Override
    public boolean isCompressed()
    {
        return this.isCompressed;
    }

    @Override
    public void setCompressed()
    {
        this.isCompressed = true;
    }

    @Override
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
