package org.mangocube.corenut.commons.xom;


public interface BeanWriterBuilder <T extends BeanWriter> {
    T getBeanWriter(Object... agrs);
}
