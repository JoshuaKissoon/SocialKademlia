package socialkademlia.dht;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import kademlia.dht.GetParameter;
import kademlia.dht.KadContent;
import kademlia.exceptions.ContentExistException;
import kademlia.exceptions.ContentNotFoundException;
import kademlia.node.KademliaId;

/**
 * It would be infeasible to keep all content in memory to be send when requested
 * Instead we store content into files
 * We use this Class to keep track of all content stored
 *
 * @author Joshua Kissoon
 * @since 20140226
 */
class StoredContentManager
{

    private final Map<KademliaId, List<SocialKademliaStorageEntryMetadata>> entries;

    
    {
        entries = new HashMap<>();
    }

    /**
     * Add a new entry to our storage
     *
     * @param content The content to store a reference to
     */
    public SocialKademliaStorageEntryMetadata put(KadContent content) throws ContentExistException
    {
        return this.put(new JSocialKademliaStorageEntryMetadata(content));
    }

    /**
     * Add a new entry to our storage
     *
     * @param entry The StorageEntry to store
     */
    public SocialKademliaStorageEntryMetadata put(SocialKademliaStorageEntryMetadata entry) throws ContentExistException
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
     * @param param The parameters used to search for a content
     *
     * @return boolean
     */
    public synchronized boolean contains(GetParameter param)
    {
        if (this.entries.containsKey(param.getKey()))
        {
            /* Content with this key exist, check if any match the rest of the search criteria */
            for (SocialKademliaStorageEntryMetadata e : this.entries.get(param.getKey()))
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
        }
        return false;
    }

    /**
     * Check if a content exist in the DHT
     */
    public synchronized boolean contains(KadContent content)
    {
        return this.contains(new GetParameter(content));
    }

    /**
     * Check if a StorageEntry exist on this DHT
     */
    public synchronized boolean contains(SocialKademliaStorageEntryMetadata entry)
    {
        return this.contains(new GetParameter(entry));
    }

    /**
     * Checks if our DHT has a Content for the given criteria
     *
     * @param param The parameters used to search for a content
     *
     * @return List of content for the specific search parameters
     */
    public SocialKademliaStorageEntryMetadata get(GetParameter param) throws NoSuchElementException
    {
        if (this.entries.containsKey(param.getKey()))
        {
            /* Content with this key exist, check if any match the rest of the search criteria */
            for (SocialKademliaStorageEntryMetadata e : this.entries.get(param.getKey()))
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

    public SocialKademliaStorageEntryMetadata get(SocialKademliaStorageEntryMetadata md)
    {
        return this.get(new GetParameter(md));
    }

    /**
     * @return A list of all storage entries
     */
    public synchronized List<SocialKademliaStorageEntryMetadata> getAllEntries()
    {
        List<SocialKademliaStorageEntryMetadata> entriesRet = new ArrayList<>();

        for (List<SocialKademliaStorageEntryMetadata> entrySet : this.entries.values())
        {
            if (entrySet.size() > 0)
            {
                entriesRet.addAll(entrySet);
            }
        }

        return entriesRet;
    }

    /**
     * @return A list of all storage entries for cached content
     */
    public List<SocialKademliaStorageEntryMetadata> getAllCachedEntries()
    {
        List<SocialKademliaStorageEntryMetadata> entriesRet = new ArrayList<>();

        for (List<SocialKademliaStorageEntryMetadata> entrySet : this.entries.values())
        {
            if (entrySet.size() > 0)
            {
                for (SocialKademliaStorageEntryMetadata md : entriesRet)
                {
                    if (md.isCached())
                    {
                        entriesRet.add(md);
                    }
                }
            }
        }

        return entriesRet;
    }

    public void remove(KadContent content) throws ContentNotFoundException
    {
        this.remove(new JSocialKademliaStorageEntryMetadata(content));
    }

    public void remove(SocialKademliaStorageEntryMetadata entry) throws ContentNotFoundException
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
    public synchronized String toString()
    {
        StringBuilder sb = new StringBuilder("Stored Content: \n");
        int count = 0;
        for (List<SocialKademliaStorageEntryMetadata> es : this.entries.values())
        {
            if (entries.size() < 1)
            {
                continue;
            }

            for (SocialKademliaStorageEntryMetadata e : es)
            {
                sb.append(++count);
                sb.append(". ");
                sb.append(e);
                sb.append("\n");
            }
        }

        sb.append("\n");
        return sb.toString();
    }
}