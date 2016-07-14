package org.mangocube.corenut.commons.xom;

import org.mangocube.corenut.commons.bean.InstanceFactory;
import org.mangocube.corenut.commons.bean.InstanceFactory.InstanceManage;


public class BeanRWFactory {
    private static final String PROVIDER_RESOURCE = "config/core/xom/builder/*.provider.properties";

    private static final String DEFAULT_READER_KEY = "XOM/READER";
    private static final String DEFAULT_WRITER_KEY = "XOM/WRITER";

    private static final InstanceFactory factory = new InstanceFactory(PROVIDER_RESOURCE, InstanceManage.REUSE_INSTANCE);

    private String providerKey = "";

    public BeanRWFactory() {
    }

    public BeanRWFactory(String providerKey) {
        this.providerKey = providerKey.toUpperCase();
    }

    public BeanReaderBuilder newBeanReaderBuilder() {
        return (BeanReaderBuilder)factory.getInstance(DEFAULT_READER_KEY + providerKey);
    }

    public BeanWriterBuilder newBeanWriterBuilder() {
        return (BeanWriterBuilder)factory.getInstance(DEFAULT_WRITER_KEY + providerKey);
    }
}
