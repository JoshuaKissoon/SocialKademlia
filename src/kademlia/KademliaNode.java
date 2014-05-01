package kademlia;

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
import kademlia.core.DefaultConfiguration;
import kademlia.dht.GetParameter;
import kademlia.core.KadConfiguration;
import kademlia.core.KadServer;
import kademlia.dht.DHT;
import kademlia.dht.GetParameterFUC;
import kademlia.dht.KadContent;
import kademlia.dht.StorageEntry;
import kademlia.exceptions.ContentNotFoundException;
import kademlia.exceptions.RoutingException;
import kademlia.exceptions.UpToDateContentException;
import kademlia.message.MessageFactory;
import kademlia.node.Node;
import kademlia.node.NodeId;
import kademlia.operation.ConnectOperation;
import kademlia.operation.ContentLookupOperation;
import kademlia.operation.ContentLookupOperationFUC;
import kademlia.operation.Operation;
import kademlia.operation.KadRefreshOperation;
import kademlia.operation.StoreOperation;
import kademlia.routing.RoutingTable;
import kademlia.util.serializer.JsonDHTSerializer;
import kademlia.util.serializer.JsonRoutingTableSerializer;
import kademlia.util.serializer.JsonSerializer;

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
public class KademliaNode
{

    /* Kademlia Attributes */
    private final String ownerId;

    /* Objects to be used */
    private final transient Node localNode;
    private final transient KadServer server;
    private final transient DHT dht;
    private transient RoutingTable routingTable;
    private final int udpPort;
    private transient KadConfiguration config;

    /* Timer used to execute refresh operations */
    private final transient Timer refreshOperationTimer;
    private final transient TimerTask refreshOperationTTask;

    /* Whether this node is up and running */
    private boolean isRunning = false;

    /* Factories */
    private final transient MessageFactory messageFactory;

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
    public KademliaNode(String ownerId, Node localNode, int udpPort, DHT dht, RoutingTable routingTable, KadConfiguration config) throws IOException
    {
        this.ownerId = ownerId;
        this.udpPort = udpPort;
        this.localNode = localNode;
        this.dht = dht;
        this.config = config;
        this.routingTable = routingTable;
        this.messageFactory = new MessageFactory(this, this.dht, this.config);
        this.server = new KadServer(udpPort, this.messageFactory, this.localNode, this.config);
        this.refreshOperationTimer = new Timer(true);

        /* Schedule Recurring RestoreOperation */
        refreshOperationTTask = new TimerTask()
        {
            @Override
            public void run()
            {
                try
                {
                    /* Runs a DHT RefreshOperation  */
                    KademliaNode.this.refresh();
                }
                catch (IOException e)
                {
                    System.err.println("Refresh Operation Failed; Message: " + e.getMessage());
                }
            }
        };
        refreshOperationTimer.schedule(refreshOperationTTask, this.config.restoreInterval(), this.config.restoreInterval());

        this.isRunning = true;
    }

    public KademliaNode(String ownerId, Node node, int udpPort, RoutingTable routingTable, KadConfiguration config) throws IOException
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

    public KademliaNode(String ownerId, Node node, int udpPort, KadConfiguration config) throws IOException
    {
        this(
                ownerId,
                node,
                udpPort,
                new RoutingTable(node, config),
                config
        );
    }

    public KademliaNode(String ownerId, NodeId defaultId, int udpPort) throws IOException
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
    public static KademliaNode loadFromFile(String ownerId) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        return KademliaNode.loadFromFile(ownerId, new DefaultConfiguration());
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
    public static KademliaNode loadFromFile(String ownerId, KadConfiguration iconfig) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        DataInputStream din;

        /**
         * @section Read Basic Kad data
         */
        din = new DataInputStream(new FileInputStream(getStateStorageFolderName(ownerId, iconfig) + File.separator + "kad.kns"));
        KademliaNode ikad = new JsonSerializer<KademliaNode>().read(din);

        /**
         * @section Read the routing table
         */
        din = new DataInputStream(new FileInputStream(getStateStorageFolderName(ownerId, iconfig) + File.separator + "routingtable.kns"));
        RoutingTable irtbl = new JsonRoutingTableSerializer().read(din);

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

        return new KademliaNode(ownerId, inode, ikad.getPort(), idht, irtbl, iconfig);
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
        Operation op = new ConnectOperation(this.server, this, n, this.config);
        op.execute();
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
    public synchronized int put(KadContent content) throws IOException
    {
        StoreOperation sop = new StoreOperation(this.server, this, content, this.dht, this.config);
        sop.execute();

        /* Return how many nodes the content was stored on */
        return sop.numNodesStoredAt();
    }

    /**
     * Store a content on the local node's DHT
     *
     * @param content The content to put on the DHT
     *
     * @throws java.io.IOException
     */
    public synchronized void putLocally(KadContent content) throws IOException
    {
        this.dht.store(content);
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
    public synchronized void cache(KadContent content) throws IOException
    {
        this.dht.cache(content);
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
        return this.dht.get(param);
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
            return this.getUpdated(param, this.config.k());
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
            /**
             * If the content exist in our own DHT, then return it if it's not a cached entry.
             * If e is cached means this node is not one of the k-closest, so there may be a more updated version.
             */
            StorageEntry e = this.dht.get(param);
            if (!e.getContentMetadata().isCached())
            {
                return e;
            }
        }

        /* Seems like it doesn't exist in our DHT, get it from other Nodes */
        ContentLookupOperation clo = new ContentLookupOperation(server, this, param, this.config);
        clo.execute();
        return clo.getContentFound();
    }

    /**
     * Get some content stored on the DHT if there is a newer version than our current version.
     *
     * @param param           The parameters used to search for the content
     * @param numNodesToQuery How many nodes should we query to get this content. We return all content on these nodes.
     *
     * @return StorageEntry The content
     *
     * @throws java.io.IOException
     * @throws kademlia.exceptions.UpToDateContentException
     */
    public StorageEntry getUpdated(GetParameterFUC param, int numNodesToQuery) throws IOException, UpToDateContentException
    {
        /* Seems like it doesn't exist in our DHT, get it from other Nodes */
        ContentLookupOperationFUC clo = new ContentLookupOperationFUC(server, this, param, numNodesToQuery, this.config);
        clo.execute();

        StorageEntry latest = clo.getLatestContentFound();

        /* If we have this content locally, lets update it too */
        try
        {
            this.dht.update(latest);
        }
        catch (NoSuchElementException ex)
        {
            /* Any exception here will be if we don't have the content... just ignore it */
        }

        return latest;
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

        /* Close off the timer tasks */
        this.refreshOperationTTask.cancel();
        this.refreshOperationTimer.cancel();
        this.refreshOperationTimer.purge();

        this.isRunning = false;

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
        new JsonSerializer<KademliaNode>().write(this, dout);

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
        new JsonRoutingTableSerializer().write(this.getRoutingTable(), dout);

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

    public RoutingTable getRoutingTable()
    {
        return this.routingTable;
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
