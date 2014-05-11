package kademlia.util.serializer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import kademlia.routing.RoutingTable;
import java.lang.reflect.Type;
import java.util.List;
import kademlia.core.KadConfiguration;
import kademlia.routing.Contact;
import kademlia.routing.SocialKadRoutingTable;

/**
 * A KadSerializer that serializes routing tables to JSON format
 * The generic serializer is not working for routing tables
 *
 * Why a RoutingTable specific serializer?
 * The routing table structure:
 * - RoutingTable
 * -- Buckets[]
 * --- Map<NodeId, Node>
 * ---- NodeId:KeyBytes
 * ---- Node: NodeId, InetAddress, Port
 *
 * The above structure seems to be causing some problem for Gson,
 * especially at the Map part.
 *
 * Solution
 * - Make the Buckets[] transient
 * - Simply store all Nodes in the serialized object
 * - When reloading, re-add all nodes to the RoutingTable
 *
 * @author Joshua Kissoon
 *
 * @since 20140310
 */
public class JsonSocialKadRoutingTableSerializer implements KadSerializer<SocialKadRoutingTable>
{

    private final Gson gson;

    Type contactCollectionType = new TypeToken<List<Contact>>()
    {
    }.getType();

    private final KadConfiguration config;
    
    
    {
        gson = new Gson();
    }

    /**
     * Initialize the class
     *
     * @param config
     */
    public JsonSocialKadRoutingTableSerializer(KadConfiguration config)
    {
        this.config = config;
    }

    @Override
    public void write(SocialKadRoutingTable data, DataOutputStream out) throws IOException
    {
        try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(out)))
        {
            writer.beginArray();

            /* Write the basic RoutingTable */
            gson.toJson(data, RoutingTable.class, writer);

            /* Now Store the Contacts  */
            gson.toJson(data.getAllContacts(), contactCollectionType, writer);

            writer.endArray();
        }
    }

    @Override
    public SocialKadRoutingTable read(DataInputStream in) throws IOException, ClassNotFoundException
    {
        try (DataInputStream din = new DataInputStream(in);
                JsonReader reader = new JsonReader(new InputStreamReader(in)))
        {
            reader.beginArray();

            /* Read the basic RoutingTable */
            SocialKadRoutingTable tbl = gson.fromJson(reader, RoutingTable.class);
            tbl.setConfiguration(config);

            /* Now get the Contacts and add them back to the RoutingTable */
            List<Contact> contacts = gson.fromJson(reader, contactCollectionType);
            tbl.initialize();

            for (Contact c : contacts)
            {
                tbl.insert(c);
            }

            reader.endArray();
            /* Read and return the Content*/
            return tbl;
        }
    }
}
