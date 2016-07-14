package org.mangocube.corenut.commons.xom;

import org.mangocube.corenut.commons.exception.ErrorCode;

import javax.xml.transform.Source;
import java.io.IOException;

public interface BeanReader {
    public enum ReaderError {
        @ErrorCode(comment = "Unsupported input source ${1}!")
        UNSUPPORT_SOURCE,
        @ErrorCode(comment = "Invalid input source ${1}!")
        INVALID_SOURCE,
        @ErrorCode(comment = "Fail to read bean from ${1}!")
        READ_BEAN_FAIL
    }

    /**
     * Read the given {@link javax.xml.transform.Source} into an object graph.
     *
     * @param source the source to read from
     * @return the object graph
     * @throws XmlMappingException if the given source cannot be mapped to an object
     * @throws java.io.IOException if an I/O Exception occurs
     */
    <T> T read(Source source) throws XmlMappingException, IOException;

    /**
     * Indicates whether this reader can read instances of the supplied type.
     *
     * @param source the input source object instance/class that this reader is being asked if it can read
     * @return <code>true</code> if this reader can indeed read from the input source; <code>false</code> otherwise
     */
    boolean supports(Object source);
}
