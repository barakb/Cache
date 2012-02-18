package utils.cache;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: Bar Orion Barak
 * Date: Feb 18, 2012
 * Time: 12:43:14 PM
 * Show how write a cache for files content.
 */
public class FileContentCache {
    private final Logger logger = Logger.getLogger(FileContentCache.class);

    public static void main(String[] args) throws Throwable {
        BasicConfigurator.configure();
//        create cache for the last 10 used file.
        Cache<String, byte[]> fileContentCache =
                new Cache<String, byte[]>(new Compute<String, byte[]>() {
                    @Override
                    public byte[] compute(String key) throws Exception {
                        return readFileContent(new File(key));
                    }
                }, 10);
        byte [] contentOfFoo = fileContentCache.get("foo.txt");
        // if you have an event from the file system that a file was deleted you should call
//        fileContentCache.remove(name); // where name is the name of the deleted file.
    }

    private static byte[] readFileContent(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        try {
            long length = file.length();

            if (length > Integer.MAX_VALUE) {
               throw new IllegalArgumentException("File it to large " + length);
            }

            byte[] bytes = new byte[(int) length];

            int offset = 0;
            int numRead;
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }
            return bytes;
        } finally {
            is.close();
        }
    }
}
