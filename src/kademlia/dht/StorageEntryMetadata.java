package kademlia.dht;

import java.util.Objects;
import kademlia.node.KademliaId;

/**
 * Keeps track of data for a Content stored in the DHT
 * Used by the StorageEntryManager class
 *
 * @author Joshua Kissoon
 * @since 20140226
 */
public class StorageEntryMetadata
{

    private final KademliaId key;
    private final String ownerId;
    private final String type;
    private final int contentHash;
    private final long updatedTs;

    private boolean isCached = false;

    /* This value is the last time this content was last updated from the network */
    private long lastRepublished;

    public StorageEntryMetadata(KadContent content)
    {
        this.key = content.getKey();
        this.ownerId = content.getOwnerId();
        this.type = content.getType();
        this.contentHash = content.hashCode();
        this.updatedTs = content.getLastUpdatedTimestamp();

        this.lastRepublished = System.currentTimeMillis() / 1000L;
    }

    public KademliaId getKey()
    {
        return this.key;
    }

    public String getOwnerId()
    {
        return this.ownerId;
    }

    public String getType()
    {
        return this.type;
    }

    public int getContentHash()
    {
        return this.contentHash;
    }

    public long getLastUpdatedTimestamp()
    {
        return this.updatedTs;
    }

    /**
     * Specify that this content should be cached.
     */
    public void setCached()
    {
        this.isCached = true;
    }

    /**
     * Specify that this content should be cached.
     *
     * @param cached
     */
    public void setCached(boolean cached)
    {
        this.isCached = cached;
    }

    public boolean isCached()
    {
        return this.isCached;
    }

    /**
     * When a node is looking for content, he sends the search criteria in a GetParameter object
     * Here we take this GetParameter object and check if this StorageEntry satisfies the given parameters
     *
     * @param params
     *
     * @return boolean Whether this content satisfies the parameters
     */
    public boolean satisfiesParameters(GetParameter params)
    {
        /* Check that owner id matches */
        if ((params.getOwnerId() != null) && (!params.getOwnerId().equals(this.ownerId)))
        {
            return false;
        }

        /* Check that type matches */
        if ((params.getType() != null) && (!params.getType().equals(this.type)))
        {
            return false;
        }

        /* Check that key matches */
        if ((params.getKey() != null) && (!params.getKey().equals(this.key)))
        {
            return false;
        }

        return true;
    }

    public long lastRepublished()
    {
        return this.lastRepublished;
    }

    /**
     * Whenever we republish a content or get this content from the network, we update the last republished time
     */
    public void updateLastRepublished()
    {
        this.lastRepublished = System.currentTimeMillis() / 1000L;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof StorageEntryMetadata)
        {
            return this.hashCode() == o.hashCode();
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.key);
        hash = 23 * hash + Objects.hashCode(this.ownerId);
        hash = 23 * hash + Objects.hashCode(this.type);
        return hash;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("[StorageEntry: ");

        sb.append("{Key: ");
        sb.append(this.key);
        sb.append("} ");
        sb.append("{Owner: ");
        sb.append(this.ownerId);
        sb.append("} ");
        sb.append("{Type: ");
        sb.append(this.type);
        sb.append("} ");
        sb.append("{UpdatedTs: ");
        sb.append(this.updatedTs);
        sb.append("} ");
        sb.append("{cached?: ");
        sb.append(this.isCached());
        sb.append("} ");
        sb.append("]");

        return sb.toString();
    }
}
