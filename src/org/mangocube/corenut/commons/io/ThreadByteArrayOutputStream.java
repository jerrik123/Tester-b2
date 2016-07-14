package org.mangocube.corenut.commons.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Customized ouputStream for redirect the logged content into the specified repository.
 * <p/>
 *
 * @since 1.0
 */
public class ThreadByteArrayOutputStream extends OutputStream {
    private static class LocalByteBuffer {
        private int count = 0;
        private byte[] buffer;

        byte[] getBuffer() {
            return buffer == null ? new byte[128] : buffer;
        }

        void setBuffer(byte[] buffer) {
            this.buffer = buffer;
        }

        void write(int b) {
            int newcount = count + 1;
            byte[] buf = getBuffer();
            if (newcount > buf.length) {
                byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
                System.arraycopy(buf, 0, newbuf, 0, count);
                buf = newbuf;
            }
            buf[count] = (byte) b;
            count = newcount;
            setBuffer(buf);
        }

    }


    //    private int count;
    //decorated outputStream
    private OutputStream orgOut;
    //threadlocal variable for buffering the byte[]
    private static ConcurrentMap<Long, Stack<LocalByteBuffer>> localBufferMap = new ConcurrentHashMap<Long, Stack<LocalByteBuffer>>();

    /**
     * Thread id filter, only thread which id is in this list could write data to its localBuffer.
     * Call enableOutput to add permited Thread id to threadIdFilter.
     */
    private static List<Long> threadIdFilter = Collections.synchronizedList(new ArrayList<Long>());

    /**
     * enables the outPut
     */
    public static void enableOutput() {
        long threadId = Thread.currentThread().getId();
        if (!threadIdFilter.contains(threadId)) {
            threadIdFilter.add(threadId);
        }
        Stack<LocalByteBuffer> bufferStack = null;
        if (localBufferMap.containsKey(threadId)) {
            bufferStack = localBufferMap.get(threadId);
            bufferStack.push(new LocalByteBuffer());
        } else {
            bufferStack = new Stack<LocalByteBuffer>();
            bufferStack.push(new LocalByteBuffer());
            localBufferMap.put(threadId, bufferStack);
        }
    }

    /**
     * disables the output
     */
    public static void disableOutput() {
        long threadId = Thread.currentThread().getId();
        if (threadIdFilter.contains(threadId)) {
            Stack<LocalByteBuffer> bufferStack = localBufferMap.get(threadId);
            if (bufferStack.isEmpty()) {
                threadIdFilter.remove(threadId);
            }
        }
    }

    /**
     * Checks if the threadIDList is empty.
     *
     * @return true if is empty. otherwise false.
     */
    public static boolean isFilterEmpty() {
        return threadIdFilter.isEmpty();
    }

    /**
     * Gets the output content.
     *
     * @return content which output during the redirection.
     */
    public static String getBufferedValue() {
        Stack<LocalByteBuffer> bufferStack = localBufferMap.get(Thread.currentThread().getId());
        if (bufferStack == null) return "";
        LocalByteBuffer buffer = bufferStack.pop();
        return buffer == null ? "" : new String(buffer.getBuffer());
    }

    /**
     * Constructor for this decorator.
     *
     * @param out outputStream which will be decorated.
     */
    public ThreadByteArrayOutputStream(OutputStream out) {
        this.orgOut = out;
    }

    /**
     * writes byte into the local buffer.
     *
     * @param b byte value
     * @throws IOException Unexpected exception
     */
    public void write(int b) throws IOException {
        long threadId = Thread.currentThread().getId();
        if (threadIdFilter.contains(threadId)) {
            Stack<LocalByteBuffer> bufferStack = localBufferMap.get(threadId);
            LocalByteBuffer buffer = bufferStack.peek();
            buffer.write(b);
        }
        orgOut.write(b);
    }
}
