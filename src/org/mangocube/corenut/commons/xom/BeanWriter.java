package org.mangocube.corenut.commons.xom;

import org.mangocube.corenut.commons.exception.ErrorCode;

import javax.xml.transform.Result;
import java.io.IOException;

public interface BeanWriter {
    public enum WriterError {
        @ErrorCode(comment = "Write bean ${1} fails!")
        WRITE_BEAN_FAIL
    }

    /**
     * Write the object graph with the given root into the provided {@link javax.xml.transform.Result}.
     *
     * @param graph  the root of the object graph to marshal
     * @param result the result to write to
     * @throws XmlMappingException if the given object cannot be written to the result
     * @throws java.io.IOException if an I/O exception occurs
     */
    void write(Object graph, Result result) throws XmlMappingException, IOException;

    void write(Object graph, Result result, String encoding) throws XmlMappingException, IOException;

    /**
     * Indicates whether this writer can write instances of the supplied type.
     *
     * @param graph the object instance/class that this writer is being asked if it can write
     * @return <code>true</code> if this writer can indeed write instances of the supplied object;
     *         <code>false</code> otherwise
     */
    boolean supports(Object graph);
}
