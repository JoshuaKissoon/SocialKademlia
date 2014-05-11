package kademlia.dht;

/**
 * A StorageEntry class that is used to store a content on the DHT
 *
 * @author Joshua Kissoon
 * @since 20140402
 */
public class StorageEntry
{

    private byte[] content;
    private final StorageEntryMetadata metadata;

    public StorageEntry(KadContent content)
    {
        this(content, new StorageEntryMetadata(content));
    }

    public StorageEntry(KadContent content, StorageEntryMetadata metadata)
    {
        this.content = content.toBytes();
        this.metadata = metadata;
    }

    public byte[] getContent()
    {
        return this.content;
    }

    public void setContent(byte[] data)
    {
        this.content = data;
    }
    
    public void setContent(String data)
    {
        
    }

    public String getContentString()
    {
        return new String(this.content);
    }

    public StorageEntryMetadata getContentMetadata()
    {
        return this.metadata;
    }
}
