package org.mangocube.corenut.commons.xom.betwixt;

import org.mangocube.corenut.commons.xom.BeanWriterBuilder;

public class XMLBeanWriterBuilder implements BeanWriterBuilder<XMLBeanWriter> {
    public XMLBeanWriter getBeanWriter(Object... agrs) {
        return new XMLBeanWriter();
    }
}
