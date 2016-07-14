package org.mangocube.corenut.commons.io;

import java.io.Writer;
import java.io.PrintStream;
import java.io.IOException;

/**
 * Setup the System.out or System.err direct to the new repository.
 *
 * @since 1.0
 */
public class SystemConsoleUtil {
    /**
     * This method should be invoked before any printStream utils be initialized.
     */
    public static void initialize() {
        synchronized (System.out) {
            PrintStream pm = new PrintStream(new ThreadByteArrayOutputStream(System.out));
            System.setOut(pm);
            System.setErr(pm);
        }
    }

    /**
     * Redirect the output of the System.out and System.err to the ThreadByteArrayOutputStream
     * this is the decorator for the orginal output settings.
     */
    public static void redirect() {
        ThreadByteArrayOutputStream.enableOutput();
    }

    /**
     * After redirect the output then must set the orginal output back, if not
     * the system will always put the content to the specified repository first.
     * The returned string is the content that stored during the process of the
     * redirection, after restore, then put the content into the specified repository.
     *
     * @param writer any type of writer which passed in
     * @throws java.io.IOException write the content error
     */
    public static void flush(Writer writer) throws IOException {
        String content = ThreadByteArrayOutputStream.getBufferedValue();
        ThreadByteArrayOutputStream.disableOutput();
        writer.write(content);
    }
}
