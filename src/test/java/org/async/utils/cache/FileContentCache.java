package org.async.utils.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @SuppressWarnings("unused")
    private final static Logger logger = LoggerFactory.getLogger(FileContentCache.class);


    public static void main(String[] args) throws Throwable {
//        create cache for the last 10 used file.
        Cache<String, byte[]> fileContentCache =
                new Cache<>(key -> readFileContent(new File(key)), 10);
        //noinspection unused
        byte[] contentOfFoo = fileContentCache.get("foo.txt");
        // if you have an event from the file system that a file was deleted you should call
//        fileContentCache.remove(name); // where name is the name of the deleted file.
    }

    private static byte[] readFileContent(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            long length = file.length();

            if (length > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("File is too large " + length);
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
        }
    }
}
