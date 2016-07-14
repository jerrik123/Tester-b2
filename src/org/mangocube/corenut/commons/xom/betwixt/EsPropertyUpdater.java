package org.mangocube.corenut.commons.xom.betwixt;

import org.apache.commons.betwixt.expression.TypedUpdater;
import org.apache.commons.betwixt.expression.Context;
import org.apache.commons.beanutils.BeanUtils;

class EsPropertyUpdater extends TypedUpdater {
    private String propertyName;

    public EsPropertyUpdater(String propertyName, Class propertyType) {
        this.propertyName = propertyName;
        setValueType(propertyType);
    }

    protected void executeUpdate(Context context, Object bean, Object value) throws Exception {
        BeanUtils.copyProperty(bean, propertyName, value);        
    }
}
