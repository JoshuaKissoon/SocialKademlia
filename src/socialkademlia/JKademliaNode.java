package socialkademlia;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import kademlia.DefaultConfiguration;
import kademlia.dht.GetParameter;
import kademlia.KadConfiguration;
import kademlia.KadServer;
import socialkademlia.dht.DHT;
import socialkademlia.dht.GetParameterFUC;
import kademlia.dht.KadContent;
import socialkademlia.dht.StorageEntry;
import socialkademlia.dht.util.StorageEntryCompressor;
import kademlia.exceptions.ContentNotFoundException;
import kademlia.exceptions.RoutingException;
import kademlia.message.KademliaMessageFactory;
import socialkademlia.exceptions.UpToDateContentException;
import socialkademlia.message.MessageFactory;
import kademlia.node.Node;
import kademlia.node.KademliaId;
import kademlia.operation.ConnectOperation;
import socialkademlia.operation.ContentLookupOperation;
import socialkademlia.operation.ContentLookupOperationFUC;
import kademlia.operation.Operation;
import kademlia.operation.KadRefreshOperation;
import kademlia.operation.StoreOperation;
import socialkademlia.routing.SocialKadRoutingTable;
import socialkademlia.routing.SocialKadRoutingTableImpl;
import kademlia.util.serializer.JsonDHTSerializer;
import kademlia.util.serializer.JsonSerializer;
import socialkademlia.util.serializer.JsonSocialKadRoutingTableSerializer;

/**
 * The main Kademlia Node on the network, this node manages everything for this local system.
 *
 * @author Joshua Kissoon
 * @since 20140215
 *
 * @todo When we receive a store message - if we have a newer version of the content, re-send this newer version to that node so as to update their version
 * @todo Handle IPv6 Addresses
 *
 */
public class JKademliaNode
{

    /* Kademlia Attributes */
    private final String ownerId;

    /* Objects to be used */
    private final transient Node localNode;
    private final transient KadServer server;
    private final transient DHT dht;
    private transient SocialKadRoutingTable routingTable;
    private final int udpPort;
    private transient KadConfiguration config;

    /* Timer used to execute refresh operations */
    private transient Timer refreshOperationTimer;
    private transient TimerTask refreshOperationTTask;

    /* Factories */
    private final transient KademliaMessageFactory messageFactory;

    /* Statistics */
    private final transient SocialKadStatistician statistician;

    
    {
        statistician = new Statistician();
    }

    /**
     * Creates a Kademlia DistributedMap using the specified name as filename base.
     * If the id cannot be read from disk the specified defaultId is used.
     * The instance is bootstraped to an existing network by specifying the
     * address of a bootstrap node in the network.
     *
     * @param ownerId      The Name of this node used for storage
     * @param localNode    The Local Node for this Kad instance
     * @param udpPort      The UDP port to use for routing messages
     * @param dht          The DHT for this instance
     * @param config
     * @param routingTable
     *
     * @throws IOException If an error occurred while reading id or local map
     *                     from disk <i>or</i> a network error occurred while
     *                     attempting to bootstrap to the network
     * */
    public JKademliaNode(String ownerId, Node localNode, int udpPort, DHT dht, SocialKadRoutingTable routingTable, KadConfiguration config) throws IOException
    {
        this.ownerId = ownerId;
        this.udpPort = udpPort;
        this.localNode = localNode;
        this.dht = dht;
        this.config = config;
        this.routingTable = routingTable;
        this.messageFactory = new MessageFactory(this, this.dht, this.config);
        this.server = new KadServer(udpPort, this.messageFactory, this.localNode, this.config, this.statistician);
        this.startRefreshOperation();
    }

    /**
     * Schedule the recurring refresh operation
     */
    public final void startRefreshOperation()
    {
        this.refreshOperationTimer = new Timer(true);
        refreshOperationTTask = new TimerTask()
        {
            @Override
            public void run()
            {
                try
                {
                    /* Runs a DHT RefreshOperation  */
                    JKademliaNode.this.refresh();
                }
                catch (IOException e)
                {
                    System.err.println("KademliaNode: Refresh Operation Failed; Message: " + e.getMessage());
                }
            }
        };
        refreshOperationTimer.schedule(refreshOperationTTask, this.config.restoreInterval(), this.config.restoreInterval());
    }

    public final void stopRefreshOperation()
    {
        /* Close off the timer tasks */
        this.refreshOperationTTask.cancel();
        this.refreshOperationTimer.cancel();
        this.refreshOperationTimer.purge();
    }

    public JKademliaNode(String ownerId, Node node, int udpPort, SocialKadRoutingTable routingTable, KadConfiguration config) throws IOException
    {
        this(
                ownerId,
                node,
                udpPort,
                new DHT(ownerId, config),
                routingTable,
                config
        );
    }

    public JKademliaNode(String ownerId, Node node, int udpPort, KadConfiguration config) throws IOException
    {
        this(
                ownerId,
                node,
                udpPort,
                new SocialKadRoutingTableImpl(node, config),
                config
        );
    }

    public JKademliaNode(String ownerId, KademliaId defaultId, int udpPort) throws IOException
    {
        this(
                ownerId,
                new Node(defaultId, InetAddress.getLocalHost(), udpPort),
                udpPort,
                new DefaultConfiguration()
        );
    }

    /**
     * Load Stored state using default configuration
     *
     * @param ownerId The ID of the owner for the stored state
     *
     * @return A Kademlia instance loaded from a stored state in a file
     *
     * @throws java.io.FileNotFoundException
     * @throws java.lang.ClassNotFoundException
     */
    public static JKademliaNode loadFromFile(String ownerId) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        return JKademliaNode.loadFromFile(ownerId, new DefaultConfiguration());
    }

    /**
     * Load Stored state
     *
     * @param ownerId The ID of the owner for the stored state
     * @param iconfig Configuration information to work with
     *
     * @return A Kademlia instance loaded from a stored state in a file
     *
     * @throws java.io.FileNotFoundException
     * @throws java.lang.ClassNotFoundException
     */
    public static JKademliaNode loadFromFile(String ownerId, KadConfiguration iconfig) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        DataInputStream din;

        /**
         * @section Read Basic Kad data
         */
        din = new DataInputStream(new FileInputStream(getStateStorageFolderName(ownerId, iconfig) + File.separator + "kad.kns"));
        JKademliaNode ikad = new JsonSerializer<JKademliaNode>().read(din);

        /**
         * @section Read the routing table
         */
        din = new DataInputStream(new FileInputStream(getStateStorageFolderName(ownerId, iconfig) + File.separator + "routingtable.kns"));
        SocialKadRoutingTable irtbl = new JsonSocialKadRoutingTableSerializer(iconfig).read(din);

        /**
         * @section Read the node state
         */
        din = new DataInputStream(new FileInputStream(getStateStorageFolderName(ownerId, iconfig) + File.separator + "node.kns"));
        Node inode = new JsonSerializer<Node>().read(din);

        /**
         * @section Read the DHT
         */
        din = new DataInputStream(new FileInputStream(getStateStorageFolderName(ownerId, iconfig) + File.separator + "dht.kns"));
        DHT idht = new JsonDHTSerializer().read(din);
        idht.setConfiguration(iconfig);

        return new JKademliaNode(ownerId, inode, ikad.getPort(), idht, irtbl, iconfig);
    }

    /**
     * @return Node The local node for this system
     */
    public Node getNode()
    {
        return this.localNode;
    }

    /**
     * @return The KadServer used to send/receive messages
     */
    public KadServer getServer()
    {
        return this.server;
    }

    /**
     * @return The DHT for this kad instance
     */
    public DHT getDHT()
    {
        return this.dht;
    }

    /**
     * @return The current KadConfiguration object being used
     */
    public KadConfiguration getCurrentConfiguration()
    {
        return this.config;
    }

    /**
     * Connect to an existing peer-to-peer network.
     *
     * @param n The known node in the peer-to-peer network
     *
     * @throws RoutingException      If the bootstrap node could not be contacted
     * @throws IOException           If a network error occurred
     * @throws IllegalStateException If this object is closed
     * */
    public synchronized final void bootstrap(Node n) throws IOException, RoutingException
    {
        long startTime = System.nanoTime();
        Operation op = new ConnectOperation(this.server, this, n, this.config);
        op.execute();
        long endTime = System.nanoTime();
        this.statistician.setBootstrapTime(endTime - startTime);
    }

    /**
     * Stores the specified value under the given key
     * This value is stored on K nodes on the network, or all nodes if there are > K total nodes in the network
     *
     * @param content The content to put onto the DHT
     *
     * @return Integer How many nodes the content was stored on
     *
     * @throws java.io.IOException
     *
     */
    public int put(KadContent content) throws IOException
    {
        return this.put(new StorageEntry(content));
    }

    /**
     * Stores the specified value under the given key
     * This value is stored on K nodes on the network, or all nodes if there are > K total nodes in the network
     *
     * @param entry The StorageEntry with the content to put onto the DHT
     *
     * @return Integer How many nodes the content was stored on
     *
     * @throws java.io.IOException
     *
     */
    private int put(StorageEntry entry) throws IOException
    {
        StoreOperation sop = new StoreOperation(this.server, this, this.compressStorageEntry(entry), this.dht, this.config);
        sop.execute();

        /* Return how many nodes the content was stored on */
        return sop.numNodesStoredAt();
    }

    /**
     * Put the data on the network and also cache a copy locally
     *
     * @param content The content to store
     *
     * @return How many nodes the content has been stored at excluding the local node.
     *
     * @throws java.io.IOException
     */
    public int putAndCache(KadContent content) throws IOException
    {
        StorageEntry entry = new StorageEntry(content);

        this.cache(entry);
        return this.put(entry);
    }

    /**
     * Store a content on the local node's DHT
     *
     * @param content The content to put on the DHT
     *
     * @throws java.io.IOException
     */
    public void putLocally(KadContent content) throws IOException
    {
        this.dht.store(this.compressStorageEntry(new StorageEntry(content)));
    }

    /**
     * Stores the specified value under the given key locally;
     * This content is permanently stored locally and will not be deleted unless the cache is cleared.
     *
     * @param content The content to put onto the local DHT
     *
     * @throws java.io.IOException
     *
     */
    public void cache(KadContent content) throws IOException
    {
        this.cache(new StorageEntry(content));
    }

    private void cache(StorageEntry entry) throws IOException
    {
        this.dht.cache(this.compressStorageEntry(entry));
    }

    /**
     * Compress the storage entry
     */
    private StorageEntry compressStorageEntry(final StorageEntry entry)
    {
        try
        {
            return StorageEntryCompressor.compress(entry);
        }
        catch (IOException ex)
        {
            System.err.println("Error whiles compressing storage entry. Msg: " + ex.getMessage());
        }

        return entry;
    }

    /**
     * Decompress a given storage entry
     */
    private StorageEntry decompressStorageEntry(final StorageEntry entry)
    {
        try
        {
            return StorageEntryCompressor.decompress(entry);
        }
        catch (IOException ex)
        {
            System.err.println("Error whiles decompressing storage entry. Msg: " + ex.getMessage());
        }

        return entry;
    }

    /**
     * Get some content cached locally on the DHT.
     *
     * @param param The parameters used to search for the content
     *
     * @return DHTContent The content
     *
     * @throws java.io.IOException
     */
    public StorageEntry getCachedContent(GetParameter param) throws NoSuchElementException, IOException
    {
        return this.decompressStorageEntry(this.dht.get(param));
    }

    /**
     * Method called to do an updated of a content in the local storage; this method updates both cached and un-cached content.
     *
     * @param param The parameters of the content to update
     *
     * @return StorageEntry with the updated content
     *
     * @throws java.io.IOException
     * @throws kademlia.exceptions.UpToDateContentException
     */
    public StorageEntry updateContentLocally(GetParameterFUC param) throws IOException, UpToDateContentException, NoSuchElementException
    {
        if (this.dht.contains(param))
        {
            return this.getUpdated(param);
        }
        else
        {
            throw new NoSuchElementException("KademliaNode.updateContentLocally(): This content is not a part of the DHT. ");
        }
    }

    /**
     * Get some content stored on the DHT
     *
     * @param param The parameters used to search for the content
     *
     * @return DHTContent The content
     *
     * @throws java.io.IOException
     * @throws kademlia.exceptions.ContentNotFoundException
     */
    public StorageEntry get(GetParameter param) throws NoSuchElementException, IOException, ContentNotFoundException
    {
        if (this.dht.contains(param))
        {
            /* The content is on our DHT */
            StorageEntry e = this.dht.get(param);
            if (e.getContentMetadata().isKNode())
            {
                /* We're one of the k-nodes, lets just return the content */
                return this.decompressStorageEntry(e);
            }
            else if (e.getContentMetadata().isCached())
            {
                /**
                 * If it's cached, we check for an updated version
                 *
                 * @note Here we don't log the statistic because the getUpdated() will log for us
                 */
                GetParameterFUC gpf = new GetParameterFUC(e.getContentMetadata());
                try
                {
                    /* Get and return an updated version of the content */
                    return this.decompressStorageEntry(this.getUpdated(gpf));
                }
                catch (UpToDateContentException ex)
                {
                    /* well the version we have is the latest, lets just return that */
                    return this.decompressStorageEntry(e);
                }
            }
            else
            {
                /* If it's not cached, we just return it since our node is one of the K-Closest */
                return this.decompressStorageEntry(e);
            }
        }

        /* Seems like it doesn't exist in our DHT, get it from other Nodes */
        long startTime = System.nanoTime();
        ContentLookupOperation clo = new ContentLookupOperation(server, this, param, this.config);
        clo.execute();
        long endTime = System.nanoTime();
        this.statistician.addContentLookup(endTime - startTime, clo.routeLength(), clo.isContentFound());
        return this.decompressStorageEntry(clo.getContentFound());
    }

    /**
     * Get a content and cache it.
     *
     * @param gp
     *
     * @return The StorageEntry with the content
     *
     * @throws java.io.IOException
     * @throws kademlia.exceptions.ContentNotFoundException
     */
    public StorageEntry getAndCache(final GetParameter gp) throws IOException, ContentNotFoundException
    {
        StorageEntry e = this.get(gp);
        this.cache(e);

        /**
         * We have to decompress it again before returning it because the cache method would've compressed it
         *
         * @todo decide whether it's better to decompress twice or to copy the storageentry and use one copy for cache()
         */
        return this.decompressStorageEntry(e);
    }

    /**
     * Get some content stored on the DHT if there is a newer version than our current version.
     *
     * @param param The parameters used to search for the content
     *
     * @return StorageEntry The content
     *
     * @throws java.io.IOException
     * @throws kademlia.exceptions.UpToDateContentException
     */
    public StorageEntry getUpdated(GetParameterFUC param) throws IOException, UpToDateContentException
    {
        /* We assume the owner always have the latest content, so no need to contact any other node for updated content */
        if (param.getOwnerId().equals(this.getOwnerId()))
        {
            throw new UpToDateContentException("You are the owner of this content, no need to check other nodes!!!");
        }
        /* Seems like it doesn't exist in our DHT, get it from other Nodes */
        long startTime = System.nanoTime();
        ContentLookupOperationFUC clo = new ContentLookupOperationFUC(server, this, param, this.config);
        clo.execute();
        long endTime = System.nanoTime();
        this.statistician.addContentLookupFUC(endTime - startTime, clo.routeLength(), clo.newerContentExist(), clo.isContentFound());

        StorageEntry latest = clo.getContentFound();

        /* If we have this content locally, lets update it too */
        try
        {
            this.dht.update(latest);
        }
        catch (NoSuchElementException ex)
        {
            /* Any exception here will be if we don't have the content... just ignore it */
        }

        return this.decompressStorageEntry(latest);
    }

    /**
     * Allow the user of the System to call refresh even out of the normal Kad refresh timing
     *
     * @throws java.io.IOException
     */
    public void refresh() throws IOException
    {
        new KadRefreshOperation(this.server, this, this.dht, this.config).execute();
    }

    /**
     * @return String The ID of the owner of this local network
     */
    public String getOwnerId()
    {
        return this.ownerId;
    }

    /**
     * @return Integer The port on which this kad instance is running
     */
    public int getPort()
    {
        return this.udpPort;
    }

    /**
     * Here we handle properly shutting down the Kademlia instance
     *
     * @param saveState Whether to save the application state or not
     *
     * @throws java.io.FileNotFoundException
     */
    public void shutdown(final boolean saveState) throws IOException
    {
        /* Shut down the server */
        this.server.shutdown();

        this.stopRefreshOperation();

        /* Save this Kademlia instance's state if required */
        if (saveState)
        {
            /* Save the system state */
            this.saveKadState();
        }
    }

    /**
     * Saves the node state to a text file
     *
     * @throws java.io.FileNotFoundException
     */
    private void saveKadState() throws IOException
    {
        DataOutputStream dout;

        /**
         * @section Store Basic Kad data
         */
        dout = new DataOutputStream(new FileOutputStream(getStateStorageFolderName(this.ownerId, this.config) + File.separator + "kad.kns"));
        new JsonSerializer<JKademliaNode>().write(this, dout);

        /**
         * @section Save the node state
         */
        dout = new DataOutputStream(new FileOutputStream(getStateStorageFolderName(this.ownerId, this.config) + File.separator + "node.kns"));
        new JsonSerializer<Node>().write(this.localNode, dout);

        /**
         * @section Save the routing table
         * We need to save the routing table separate from the node since the routing table will contain the node and the node will contain the routing table
         * This will cause a serialization recursion, and in turn a Stack Overflow
         */
        dout = new DataOutputStream(new FileOutputStream(getStateStorageFolderName(this.ownerId, this.config) + File.separator + "routingtable.kns"));
        new JsonSocialKadRoutingTableSerializer(this.config).write(this.getRoutingTable(), dout);

        /**
         * @section Save the DHT
         */
        dout = new DataOutputStream(new FileOutputStream(getStateStorageFolderName(this.ownerId, this.config) + File.separator + "dht.kns"));
        new JsonDHTSerializer().write(this.dht, dout);

    }

    /**
     * Get the name of the folder for which a content should be stored
     *
     * @return String The name of the folder to store node states
     */
    private static String getStateStorageFolderName(String ownerId, KadConfiguration iconfig)
    {
        /* Setup the nodes storage folder if it doesn't exist */
        String path = iconfig.getNodeDataFolder(ownerId) + File.separator + "nodeState";
        File nodeStateFolder = new File(path);
        if (!nodeStateFolder.isDirectory())
        {
            nodeStateFolder.mkdir();
        }
        return nodeStateFolder.toString();
    }

    /**
     * @return The routing table for this node.
     */
    public SocialKadRoutingTable getRoutingTable()
    {
        return this.routingTable;
    }

    /**
     * @return The statistician that manages all statistics
     */
    public SocialKadStatistician getStatistician()
    {
        return this.statistician;
    }

    /**
     * Creates a string containing all data about this Kademlia instance
     *
     * @return The string representation of this Kad instance
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("\n\nPrinting Kad State for instance with owner: ");
        sb.append(this.ownerId);
        sb.append("\n\n");

        sb.append("\n");
        sb.append("Local Node");
        sb.append(this.localNode);
        sb.append("\n");

        sb.append("\n");
        sb.append("Routing Table: ");
        sb.append(this.getRoutingTable());
        sb.append("\n");

        sb.append("\n");
        sb.append("DHT: ");
        sb.append(this.dht);
        sb.append("\n");

        sb.append("\n\n\n");

        return sb.toString();
    }
}
