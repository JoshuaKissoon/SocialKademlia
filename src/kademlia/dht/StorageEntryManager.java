package kademlia.dht;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import kademlia.exceptions.ContentExistException;
import kademlia.exceptions.ContentNotFoundException;
import kademlia.node.NodeId;

/**
 * It would be infeasible to keep all content in memory to be send when requested
 * Instead we store content into files
 * We use this Class to keep track of all content stored
 *
 * @author Joshua Kissoon
 * @since 20140226
 */
class StorageEntryManager
{

    private final Map<NodeId, List<StorageEntryMetadata>> entries;

    
    {
        entries = new HashMap<>();
    }

    /**
     * Add a new entry to our storage
     *
     * @param content The content to store a reference to
     */
    public StorageEntryMetadata put(KadContent content) throws ContentExistException
    {
        return this.put(new StorageEntryMetadata(content));
    }

    /**
     * Add a new entry to our storage
     *
     * @param entry The StorageEntry to store
     */
    public StorageEntryMetadata put(StorageEntryMetadata entry) throws ContentExistException
    {
        if (!this.entries.containsKey(entry.getKey()))
        {
            this.entries.put(entry.getKey(), new ArrayList<>());
        }

        /* If this entry doesn't already exist, then we add it */
        if (!this.contains(entry))
        {
            this.entries.get(entry.getKey()).add(entry);

            return entry;
        }
        else
        {
            throw new ContentExistException("Content already exists on this DHT");
        }
    }

    /**
     * Checks if our DHT has a Content for the given criteria
     *
     * @todo Add searching for content by type and ownerID
     *
     * @param param The parameters used to search for a content
     *
     * @return boolean
     */
    public boolean contains(GetParameter param)
    {
        if (this.entries.containsKey(param.getKey()))
        {
            /* Content with this key exist, check if any match the rest of the search criteria */
            for (StorageEntryMetadata e : this.entries.get(param.getKey()))
            {
                /* If any entry satisfies the given parameters, return true */
                if (e.satisfiesParameters(param))
                {
                    return true;
                }
            }
        }
        else
        {
            System.out.println(this);
        }
        return false;
    }

    /**
     * Check if a content exist in the DHT
     */
    public boolean contains(KadContent content)
    {
        return this.contains(new GetParameter(content));
    }

    /**
     * Check if a StorageEntry exist on this DHT
     */
    public boolean contains(StorageEntryMetadata entry)
    {
        return this.contains(new GetParameter(entry));
    }

    /**
     * Checks if our DHT has a Content for the given criteria
     *
     * @param param The parameters used to search for a content
     *
     * @todo Add finding for content by type and ownerID
     *
     * @return List of content for the specific search parameters
     */
    public StorageEntryMetadata get(GetParameter param) throws NoSuchElementException
    {
        if (this.entries.containsKey(param.getKey()))
        {
            /* Content with this key exist, check if any match the rest of the search criteria */
            for (StorageEntryMetadata e : this.entries.get(param.getKey()))
            {
                /* If any entry satisfies the given parameters, return true */
                if (e.satisfiesParameters(param))
                {
                    return e;
                }
            }

            /* If we got here, means we didn't find any entry */
            throw new NoSuchElementException();
        }
        else
        {
            throw new NoSuchElementException("No content exist for the given parameters");
        }
    }

    public StorageEntryMetadata get(StorageEntryMetadata md)
    {
        return this.get(new GetParameter(md));
    }

    /**
     * @return A list of all storage entries
     */
    public List<StorageEntryMetadata> getAllEntries()
    {
        List<StorageEntryMetadata> entriesRet = new ArrayList<>();

        for (List<StorageEntryMetadata> entrySet : this.entries.values())
        {
            if (entrySet.size() > 0)
            {
                entriesRet.addAll(entrySet);
            }
        }

        return entriesRet;
    }

    public void remove(KadContent content) throws ContentNotFoundException
    {
        this.remove(new StorageEntryMetadata(content));
    }

    public void remove(StorageEntryMetadata entry) throws ContentNotFoundException
    {
        if (contains(entry))
        {
            this.entries.get(entry.getKey()).remove(entry);
        }
        else
        {
            throw new ContentNotFoundException("This content does not exist in the Storage Entries");
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("Stored Content: \n");
        for (List<StorageEntryMetadata> es : this.entries.values())
        {
            if (entries.size() < 1)
            {
                continue;
            }

            for (StorageEntryMetadata e : es)
            {
                sb.append(e);
                sb.append("\n");
            }
        }

        sb.append("\n");
        return sb.toString();
    }
}
