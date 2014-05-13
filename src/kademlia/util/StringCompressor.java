package kademlia.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
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

    public static String compress(final byte[] input) throws IOException
    {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
                GZIPOutputStream gzipper = new GZIPOutputStream(bout))
        {
            gzipper.write(input, 0, input.length);
            gzipper.close();
            return Base64.getEncoder().encodeToString(bout.toByteArray());
        }
    }

    public static String decompress(final String input) throws IOException
    {
        byte[] inputBytes = Base64.getDecoder().decode(input);

        try (GZIPInputStream gzipper = new GZIPInputStream(new ByteArrayInputStream(inputBytes));
                BufferedReader bf = new BufferedReader(new InputStreamReader(gzipper)))
        {
            StringBuilder data = new StringBuilder();
            String line;
            while ((line = bf.readLine()) != null)
            {
                data.append(line);
            }
            return data.toString();
        }
    }
}
