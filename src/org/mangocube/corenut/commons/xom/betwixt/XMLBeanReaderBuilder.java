package org.mangocube.corenut.commons.xom.betwixt;

import org.mangocube.corenut.commons.xom.BeanReaderBuilder;

public class XMLBeanReaderBuilder implements BeanReaderBuilder<XMLBeanReader> {
    public XMLBeanReader getBeanReader(Class beanClazz, Object... args) {
        return new XMLBeanReader(beanClazz);
    }
}
