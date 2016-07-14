package org.mangocube.corenut.commons.xom.betwixt;

import org.mangocube.corenut.commons.exception.ErrorCode;
import org.mangocube.corenut.commons.xom.BeanReader;
import org.mangocube.corenut.commons.xom.XmlMappingException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.beans.IntrospectionException;
import java.io.IOException;


public class XMLBeanReader implements BeanReader {
    public enum XMLReaderCreateError {
        @ErrorCode(comment = "Fail to register root bean class : ${1}")
        FAIL_REGISTER_BEAN
    }

    private XMLBeanReadingHandler handler = new XMLBeanReadingHandler();

    public XMLBeanReader(Class rootBeanClazz) {
        try {
            handler.registerBeanClass(rootBeanClazz);
        } catch (IntrospectionException e) {
            throw new XmlMappingException(XMLReaderCreateError.FAIL_REGISTER_BEAN, e, rootBeanClazz);
        }
    }

    public Object read(Source source) throws XmlMappingException, IOException {
        if (source == null) throw new IllegalArgumentException("Null input source is invalid!");

        if (!supports(source)) throw new XmlMappingException(ReaderError.UNSUPPORT_SOURCE, source.getClass());

        Object res = null;
        try {
            if (source instanceof SAXSource) {
                res = parse((SAXSource) source);
            } else if (source instanceof StreamSource) {
                res = parse((StreamSource) source);
            } else if (source instanceof DOMSource) {
                res = parse(source);
            }
        } catch (XmlMappingException xe) {
            throw xe;
        } catch (Exception e) {
            throw new XmlMappingException(ReaderError.READ_BEAN_FAIL, e, source);
        }

        return res;
    }

    protected Object parse(Source source) throws TransformerException {
        Result sax_res = new SAXResult(handler);
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, sax_res);
        return handler.getRoot();
    }

    protected Object parse(StreamSource streamSource) throws SAXException, IOException {
        XMLReader reader = handler.getXMLReader();
        handler.configure();

        if (streamSource.getInputStream() != null) {
            reader.parse(new InputSource(streamSource.getInputStream()));
        } else if (streamSource.getReader() != null) {
            reader.parse(new InputSource(streamSource.getReader()));
        } else if (streamSource.getSystemId() != null) {
            reader.parse(new InputSource(streamSource.getSystemId()));
        } else {
            throw new XmlMappingException(ReaderError.INVALID_SOURCE, streamSource);
        }

        handler.cleanup();
        return handler.getRoot();
    }

    protected Object parse(SAXSource saxSrc) throws SAXException, IOException {
        InputSource is = saxSrc.getInputSource();

        if (is == null || (is.getByteStream() == null && is.getCharacterStream() == null))
            throw new XmlMappingException(ReaderError.INVALID_SOURCE, saxSrc);

        XMLReader reader = saxSrc.getXMLReader();
        reader = reader == null ? handler.getXMLReader() : reader;

        handler.configure();
        reader.parse(saxSrc.getInputSource());
        handler.cleanup();
        return handler.getRoot();
    }

    public boolean supports(Object source) {
        if (source == null) return false;

        Class src_cls = source instanceof Class ? (Class) source : source.getClass();

        return SAXSource.class.isAssignableFrom(src_cls) ||
                StreamSource.class.isAssignableFrom(src_cls) ||
                DOMSource.class.isAssignableFrom(src_cls);
    }
}
