package kademlia.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Class that handles compression of Strings
 *
 * @author Joshua
 * @since
 */
public class StringCompressor
{

    public static byte[] compress(final String input) throws IOException
    {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
                GZIPOutputStream gzipper = new GZIPOutputStream(bout))
        {
            gzipper.write(input.getBytes(), 0, input.length());
            gzipper.close();

            return bout.toByteArray();
        }
    }

    public static byte[] decompress(final byte[] input) throws IOException
    {
        try (GZIPInputStream gzipper = new GZIPInputStream(new ByteArrayInputStream(input));
                BufferedReader bf = new BufferedReader(new InputStreamReader(gzipper)))
        {
            StringBuilder data = new StringBuilder();
            String line;
            while ((line = bf.readLine()) != null)
            {
                data.append(line);
            }
            return data.toString().getBytes();
        }
    }
}
