package socialkademlia.dht.util;

import java.io.IOException;
import socialkademlia.dht.SocialKademliaStorageEntry;
import socialkademlia.util.StringCompressor;

/**
 * Class that handles compression of storage entries.
 *
 * In the storage entry, it doesn't make sense to constantly compress all of the data and decompress it,
 * so we only compress the content part.
 *
 * @author Joshua Kissoon
 * @since 20140511
 */
public class StorageEntryCompressor
{

    /**
     * Compress a given storage entry.
     *
     * We only compress the content part of the storage entry.
     *
     * @param entry The entry whose content is to be compressed
     *
     * @return The entry with compressed content
     *
     * @throws java.io.IOException
     */
    public static SocialKademliaStorageEntry compress(final SocialKademliaStorageEntry entry) throws IOException
    {
        if (entry.isCompressed())
        {
            return entry;
        }

        try
        {
            entry.setContent(StringCompressor.compress(entry.getContent()));
            entry.setCompressed();
        }
        catch (IOException ex)
        {
            System.err.println("Error whiles decompressing entry.");
            System.err.println("Entry: " + entry);
            System.err.println("Error: " + ex.getMessage());
            throw new IOException(ex.getMessage());
        }
        return entry;
    }

    /**
     * Decompress a given storage entry.
     *
     * We only decompress the content part of the storage entry.
     *
     * @param entry
     *
     * @return
     *
     * @throws java.io.IOException
     */
    public static SocialKademliaStorageEntry decompress(final SocialKademliaStorageEntry entry) throws IOException
    {
        if (!entry.isCompressed())
        {
            return entry;
        }
        try
        {
            entry.setContent(StringCompressor.decompress(entry.getContentString()));
            entry.setDecompressed();
        }
        catch (IOException ex)
        {
            System.err.println("Error whiles decompressing entry.");
            System.err.println("Entry: " + entry);
            System.err.println("Error: " + ex.getMessage());
            throw new IOException(ex.getMessage());
        }
        return entry;
    }
}
