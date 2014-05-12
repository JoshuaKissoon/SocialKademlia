package kademlia.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    public static byte[] compress(final byte[] input) throws IOException
    {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
                GZIPOutputStream gzipper = new GZIPOutputStream(bout))
        {
            gzipper.write(input, 0, input.length);
            gzipper.close();
            return bout.toByteArray();
        }
    }

    public static byte[] decompress(final byte[] input) throws IOException
    {
        try (ByteArrayInputStream bin = new ByteArrayInputStream(input);
                GZIPInputStream gzipper = new GZIPInputStream(bin);
                ByteArrayOutputStream out = new ByteArrayOutputStream();)
        {
            byte[] buffer = new byte[1024];
            int len;

            while ((len = gzipper.read(buffer)) > 0)
            {
                out.write(buffer, 0, len);
                buffer = new byte[1024];
            }

            gzipper.close();
            out.close();
            return out.toByteArray();
        }
    }
}
