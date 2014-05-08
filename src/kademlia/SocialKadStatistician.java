package kademlia;

/**
 * Specification for class that keeps statistics for a Kademlia instance.
 *
 * These statistics are temporary and will be lost when Kad is shut down.
 *
 * @author Joshua Kissoon
 * @since 20140507
 */
public interface SocialKadStatistician extends KadStatistician
{

    /**
     * Add the timing for a new content lookup operation for updated content.
     *
     * @param time            The time the content lookup took in nanoseconds
     * @param routeLength     The length of the route it took to get the content
     * @param updateAvailable Whether an updated version of the content was available
     */
    public void addContentLookupFUC(long time, int routeLength, boolean updateAvailable);

    /**
     * @return The total number of content lookups performed for updated content.
     */
    public int numContentLookupsFUC();
}
