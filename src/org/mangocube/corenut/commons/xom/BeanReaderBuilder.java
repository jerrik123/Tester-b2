package org.mangocube.corenut.commons.xom;

public interface BeanReaderBuilder<T extends BeanReader> {
    public T getBeanReader(Class beanClazz, Object... args);
}
