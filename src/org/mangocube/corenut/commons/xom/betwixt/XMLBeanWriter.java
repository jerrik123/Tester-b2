package org.mangocube.corenut.commons.xom.betwixt;

import org.apache.commons.betwixt.BindingConfiguration;
import org.mangocube.corenut.commons.xom.BeanWriter;
import org.mangocube.corenut.commons.xom.XmlMappingException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;


public class XMLBeanWriter implements BeanWriter {
    public void write(Object graph, Result result) throws XmlMappingException, IOException {
        if (graph == null || result == null) return;

        try {
            StringWriter wr = new StringWriter();
            org.apache.commons.betwixt.io.BeanWriter bwr = new org.apache.commons.betwixt.io.BeanWriter(wr);
            bwr.setBindingConfiguration(new BindingConfiguration(new EsObjectStringConverter(), false));
            bwr.setWriteEmptyElements(false);
            bwr.setXMLIntrospector(new EsXMLIntrospector());
            bwr.enablePrettyPrint();
            bwr.write(graph);

            StreamSource st_src = new StreamSource(new StringReader(wr.toString()));
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(st_src, result);
        } catch (Exception e) {
            throw new XmlMappingException(WriterError.WRITE_BEAN_FAIL, e, graph);
        }
    }

    public void write(Object graph, Result result, String encoding) throws XmlMappingException, IOException {
        if (graph == null || result == null) return;

        try {
            StringWriter wr = new StringWriter();
            org.apache.commons.betwixt.io.BeanWriter bwr = new org.apache.commons.betwixt.io.BeanWriter(wr);
            bwr.setBindingConfiguration(new BindingConfiguration(new EsObjectStringConverter(), false));
            bwr.setWriteEmptyElements(false);
            bwr.setXMLIntrospector(new EsXMLIntrospector());
            bwr.enablePrettyPrint();
            bwr.write(graph);

            StreamSource st_src = new StreamSource(new StringReader(wr.toString()));
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.ENCODING,encoding);
            xformer.transform(st_src, result);
        } catch (Exception e) {
            throw new XmlMappingException(WriterError.WRITE_BEAN_FAIL, e, graph);
        }
    }

    public boolean supports(Object graph) {
        return true;
    }
}
