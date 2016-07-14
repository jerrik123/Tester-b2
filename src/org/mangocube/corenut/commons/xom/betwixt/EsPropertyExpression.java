package org.mangocube.corenut.commons.xom.betwixt;

import org.apache.commons.betwixt.expression.Expression;
import org.apache.commons.betwixt.expression.Context;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class EsPropertyExpression implements Expression {
    private static final Log log = LogFactory.getLog(EsPropertyExpression.class);

    private String propertyName;

    public EsPropertyExpression(String propertyName) {
        this.propertyName = propertyName;
    }

    public Object evaluate(Context context) {
        try {
            return PropertyUtils.getProperty(context.getBean(), propertyName);            
        } catch (Exception e) {
            log.warn("Fail to set bean [" + context.getBean() + "] property :" + propertyName);
            return null;
        }
    }

    public void update(Context context, String newValue) {
        //do nothing
    }
}
