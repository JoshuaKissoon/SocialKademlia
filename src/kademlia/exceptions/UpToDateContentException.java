package kademlia.exceptions;

/**
 * An exception used to indicate that a content on this node's DHT is up to date.
 *
 * @author Joshua Kissoon
 * @created 20140422
 */
public class UpToDateContentException extends Exception
{

    public UpToDateContentException()
    {
        super();
    }

    public UpToDateContentException(String message)
    {
        super(message);
    }
}
