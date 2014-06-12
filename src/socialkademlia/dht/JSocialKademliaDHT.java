package socialkademlia.dht;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import kademlia.KadConfiguration;
import kademlia.dht.GetParameter;
import kademlia.dht.KadContent;
import kademlia.dht.KademliaStorageEntryMetadata;
import kademlia.exceptions.ContentExistException;
import kademlia.exceptions.ContentNotFoundException;
import kademlia.node.KademliaId;
import kademlia.util.serializer.JsonSerializer;
import kademlia.util.serializer.KadSerializer;

/**
 * The main Distributed Hash Table class that manages the entire JSocialKademliaDHT
 *
 * @author Joshua Kissoon
 * @since 20140226
 *
 * @todo Inherit the kademlia.dht.DHT class to remove the excess methods that are replicated
 */
public class JSocialKademliaDHT implements SocialKademliaDHT
{

    private transient StoredContentManager contentManager;
    private transient KadSerializer<JSocialKademliaStorageEntry> serializer = null;
    private transient KadConfiguration config;

    private final String ownerId;

    public JSocialKademliaDHT(String ownerId, KadConfiguration config)
    {
        this.ownerId = ownerId;
        this.config = config;
        this.initialize();
    }

    /**
     * Initialize this JSocialKademliaDHT to it's default state
     */
    @Override
    public final void initialize()
    {
        contentManager = new StoredContentManager();
    }

    /**
     * Set a new configuration. Mainly used when we restore the JSocialKademliaDHT state from a file
     *
     * @param con The new configuration file
     */
    @Override
    public void setConfiguration(KadConfiguration con)
    {
        this.config = con;
    }

    /**
     * Creates a new Serializer or returns an existing serializer
     *
     * @return The new ContentSerializer
     */
    @Override
    public KadSerializer<JSocialKademliaStorageEntry> getSerializer()
    {
        if (null == serializer)
        {
            serializer = new JsonSerializer<>();
        }

        return serializer;
    }

    /**
     * Handle storing content locally
     *
     * @param content The JSocialKademliaDHT content to store
     *
     * @return boolean true if we stored the content, false if the content already exists and is up to date
     *
     * @throws java.io.IOException
     */
    @Override
    public boolean store(JSocialKademliaStorageEntry content) throws IOException
    {
        boolean cached = content.getContentMetadata().isCached();   // Should we cache this content
        boolean isKNode = content.getContentMetadata().isKNode();   // Is this node one of the k-node

        /* Lets check if we have this content and it's the updated version */
        if (this.contentManager.contains(content.getContentMetadata()))
        {
            SocialKademliaStorageEntryMetadata current = this.contentManager.get(content.getContentMetadata());

            /* update the last republished time */
            current.updateLastRepublished();

            /* We have the current content, no need to update it! */
            if (current.getLastUpdatedTimestamp() >= content.getContentMetadata().getLastUpdatedTimestamp())
            {
                /* Cache it if required */
                if (cached)
                {
                    current.setCached();
                }

                /* Set this is a K-Node if required */
                if (isKNode)
                {
                    current.setKNode();
                }
                return false;
            }
            else
            {
                /* We got here means we don't have the current content, lets update it */
                /* If the current version is a cached version, remember to cache it back if we need to do an update */
                if (current.isCached())
                {
                    cached = true;
                }

                /* If this is a k-node for the current version, remember to set that back after an update */
                if (current.isKNode())
                {
                    isKNode = true;
                }

                /* Since we don't have the latest version, lets delete it so the new version will be added below */
                try
                {
                    this.absoluteRemove(current);
                }
                catch (ContentNotFoundException ex)
                {
                    /* This won't ever happen at this point since we only get here if the content is found, lets ignore it  */
                }
            }
        }

        /* We got here means we need to add the content or re-add it to update it */
        try
        {
            /* Store the content to a file and then keep track of this content in the entries manager */
            content.getContentMetadata().updateLastRepublished();
            content.getContentMetadata().setCached(cached);
            content.getContentMetadata().setKNode(isKNode);
            this.contentManager.put(content.getContentMetadata());
            this.putContentToFile(content, content.getContentMetadata());
            return true;
        }
        catch (ContentExistException e)
        {
            /**
             * Content already exist on the DHT
             * This won't happen because above takes care of removing the content if it's older and needs to be updated,
             * or returning if we already have the current content version.
             */
            return false;
        }
    }

    @Override
    public boolean store(KadContent content) throws IOException
    {
        return this.store(new JSocialKademliaStorageEntry(content));
    }

    /**
     * Handle storing content locally to keep the content cached.
     *
     * We set that this content is a cached entry and that this node is not one of the k-nodes.
     *
     * @param content The JSocialKademliaDHT content to store
     *
     * @return boolean true if we stored the content, false if the content already exists and is up to date
     *
     * @throws java.io.IOException
     */
    @Override
    public boolean cache(JSocialKademliaStorageEntry content) throws IOException
    {
        content.getContentMetadata().setCached();
        content.getContentMetadata().setKNode(false);
        return this.store(content);
    }

    @Override
    public boolean cache(KadContent content) throws IOException
    {
        return this.cache(new JSocialKademliaStorageEntry(content));
    }

    /**
     * Write the given storage entry to it's file
     */
    private void putContentToFile(JSocialKademliaStorageEntry content, SocialKademliaStorageEntryMetadata entryMD) throws IOException
    {
        String contentStorageFolder = this.getContentStorageFolderName(content.getContentMetadata().getKey());

        try (FileOutputStream fout = new FileOutputStream(contentStorageFolder + File.separator + entryMD.hashCode() + ".kct");
                DataOutputStream dout = new DataOutputStream(fout))
        {
            this.getSerializer().write(content, dout);
        }
    }

    /**
     * Update a content; the operation is only done iff we already have a copy of the content here
     *
     * @param newContent The content to update.
     *
     * @throws java.io.IOException
     */
    @Override
    public void update(JSocialKademliaStorageEntry newContent) throws IOException
    {
        if (this.contentManager.contains(newContent.getContentMetadata()))
        {
            this.store(newContent);
        }
        else
        {
            throw new NoSuchElementException("This content is not on the DHT currently, cannot update it.");
        }
    }

    /**
     * Retrieves a Content from local storage
     *
     * @param key      The Key of the content to retrieve
     * @param hashCode The hash code of the content to retrieve
     *
     * @return A KadContent object
     *
     * @throws java.io.IOException
     */
    @Override
    public JSocialKademliaStorageEntry retrieve(KademliaId key, int hashCode) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        String folder = this.getContentStorageFolderName(key);
        DataInputStream din = new DataInputStream(new FileInputStream(folder + File.separator + hashCode + ".kct"));
        return this.getSerializer().read(din);
    }

    /**
     * Check if any content for the given criteria exists in this JSocialKademliaDHT
     *
     * @param param The content search criteria
     *
     * @return boolean Whether any content exist that satisfy the criteria
     */
    @Override
    public boolean contains(GetParameter param)
    {
        return this.contentManager.contains(param);
    }

    /**
     * Retrieve and create a KadContent object given the JSocialKademliaStorageEntry object
     *
     * @param entry The JSocialKademliaStorageEntry used to retrieve this content
     *
     * @return KadContent The content object
     *
     * @throws java.io.IOException
     */
    @Override
    public JSocialKademliaStorageEntry get(SocialKademliaStorageEntryMetadata entry) throws IOException, NoSuchElementException
    {
        try
        {
            return this.retrieve(entry.getKey(), entry.hashCode());
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error while loading file for content. Message: " + e.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            System.err.println("The class for some content was not found. Message: " + e.getMessage());
        }

        /* If we got here, means we got no entries */
        throw new NoSuchElementException();
    }

    /**
     * Get the JSocialKademliaStorageEntry for the content if any exist,
     * retrieve the KadContent from the storage system and return it
     *
     * @param param The parameters used to filter the content needed
     *
     * @return KadContent A KadContent found on the JSocialKademliaDHT satisfying the given criteria
     *
     * @throws java.io.IOException
     */
    @Override
    public JSocialKademliaStorageEntry get(GetParameter param) throws NoSuchElementException, IOException
    {
        /* Load a KadContent if any exist for the given criteria */
        try
        {
            SocialKademliaStorageEntryMetadata e = this.contentManager.get(param);
            return this.retrieve(e.getKey(), e.hashCode());
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error while loading file for content. Message: " + e.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            System.err.println("The class for some content was not found. Message: " + e.getMessage());
        }

        /* If we got here, means we got no entries */
        throw new NoSuchElementException();
    }

    /**
     * Delete a content from local storage
     *
     * @param content The Content to Remove
     *
     *
     * @throws kademlia.exceptions.ContentNotFoundException
     */
    @Override
    public void remove(KadContent content) throws ContentNotFoundException
    {
        this.remove(new JSocialKademliaStorageEntryMetadata(content));
    }

    /**
     * Similar to the remove method, however, in this case, we remove the content even if it's cached
     */
    private void absoluteRemove(SocialKademliaStorageEntryMetadata entry) throws ContentNotFoundException
    {
        contentManager.remove(entry);

        String folder = this.getContentStorageFolderName(entry.getKey());
        File file = new File(folder + File.separator + entry.hashCode() + ".kct");
        if (file.exists())
        {
            file.delete();
        }
        else
        {
            throw new ContentNotFoundException();
        }
    }

    @Override
    public void remove(SocialKademliaStorageEntryMetadata entry) throws ContentNotFoundException
    {
        /* If it's cached data, we don't remove it, just set that we are no longer one of the k-closest */
        if (this.contentManager.get(entry).isCached())
        {
            this.contentManager.get(entry).setKNode(false);
            return;
        }

        this.absoluteRemove(entry);
    }

    /**
     * Get the name of the folder for which a content should be stored
     *
     * @param key The key of the content
     *
     * @return String The name of the folder
     */
    private String getContentStorageFolderName(KademliaId key)
    {
        /**
         * Each content is stored in a folder named after the first 2 characters of the NodeId
         *
         * The name of the file containing the content is the hash of this content
         */
        String folderName = key.hexRepresentation().substring(0, 2);
        File contentStorageFolder = new File(this.config.getNodeDataFolder(ownerId) + File.separator + folderName);

        /* Create the content folder if it doesn't exist */
        if (!contentStorageFolder.isDirectory())
        {
            contentStorageFolder.mkdir();
        }

        return contentStorageFolder.toString();
    }

    /**
     * @return A List of all StorageEntries for this node
     */
    @Override
    public List<SocialKademliaStorageEntryMetadata> getStorageEntries()
    {
        return contentManager.getAllEntries();
    }

    /**
     * @return A List of all StorageEntries of cached content for this node
     */
    public List<SocialKademliaStorageEntryMetadata> getCachedStorageEntries()
    {
        return contentManager.getAllCachedEntries();
    }

    /**
     * Used to add a list of storage entries for existing content to the JSocialKademliaDHT.
     * Mainly used when retrieving StorageEntries from a saved state file.
     *
     * @param ientries The entries to add
     */
    @Override
    public void putStorageEntries(List<SocialKademliaStorageEntryMetadata> ientries)
    {
        for (KademliaStorageEntryMetadata e : ientries)
        {
            SocialKademliaStorageEntryMetadata se = (SocialKademliaStorageEntryMetadata) e;
            try
            {
                this.contentManager.put(se);
            }
            catch (ContentExistException ex)
            {
                /* Entry already exist, no need to store it again */
            }
        }
    }

    @Override
    public synchronized String toString()
    {
        return this.contentManager.toString();
    }
}
