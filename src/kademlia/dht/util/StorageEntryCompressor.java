package kademlia.dht.util;

import java.io.IOException;
import kademlia.dht.StorageEntry;
import kademlia.util.StringCompressor;

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
    public static StorageEntry compress(final StorageEntry entry) throws IOException
    {
        entry.setContent(StringCompressor.compress(entry.getContent()));
        
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
    public static StorageEntry decompress(final StorageEntry entry) throws IOException
    {
        entry.setContent(StringCompressor.decompress(entry.getContent()));

        return entry;
    }
}
