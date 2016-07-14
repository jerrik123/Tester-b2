package org.mangocube.corenut.commons.io;

import java.io.*;
import java.net.MalformedURLException;

public class FileUtils {

    public static void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static String getFileUrlString(File f) {
        try {
            return f.toURI().toURL().toString();
        } catch (MalformedURLException e) {
            return "";
        }
    }
}
