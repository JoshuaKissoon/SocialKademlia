package kademlia.dht;

import kademlia.node.KademliaId;

/**
 * A GET request can get content based on Key, Owner, Type, etc
 *
 * This is a class containing the parameters to be passed in a GET request to find an Updated version of a content
 *
 * We use a class since the number of filtering parameters can change later
 *
 * This class will need the lastUpdatedTimestamp, since it is sent to request content of a newer version than the person has.
 *
 * @author Joshua Kissoon
 * @since 20140419
 */
public class GetParameterFUC extends GetParameter
{

    private final long lastUpdatedTS;

    /**
     * Construct a GetParameter to search for data by NodeId and owner
     *
     * @param key
     * @param type
     * @param updatedTs The last updated Timestamp
     */
    public GetParameterFUC(KademliaId key, String type, long updatedTs)
    {
        super(key, type);
        this.lastUpdatedTS = updatedTs;
    }

    /**
     * Construct a GetParameter to search for data by NodeId, owner, type
     *
     * @param key
     * @param type
     * @param updatedTS
     * @param owner
     */
    public GetParameterFUC(KademliaId key, String type, String owner, long updatedTS)
    {
        super(key, type, owner);
        this.lastUpdatedTS = updatedTS;
    }

    /**
     * Construct our get parameter from a Content
     *
     * @param c
     */
    public GetParameterFUC(KadContent c)
    {
        super(c);
        this.lastUpdatedTS = c.getLastUpdatedTimestamp();
    }

    /**
     * Construct our get parameter from a StorageEntryMeta data
     *
     * @param md
     */
    public GetParameterFUC(StorageEntryMetadata md)
    {
        super(md);
        this.lastUpdatedTS = md.getLastUpdatedTimestamp();
    }

    public long getLastUpdatedTimestamp()
    {
        return this.lastUpdatedTS;
    }

    @Override
    public String toString()
    {
        return "GetParameterFUC - [UpdatedTs: " + lastUpdatedTS + "] " + super.toString();
    }
}
