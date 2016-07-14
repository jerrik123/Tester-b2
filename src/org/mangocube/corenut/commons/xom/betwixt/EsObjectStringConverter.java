package org.mangocube.corenut.commons.xom.betwixt;

import org.apache.commons.betwixt.strategy.ConvertUtilsObjectStringConverter;
import org.apache.commons.betwixt.expression.Context;
import org.apache.commons.beanutils.ConvertUtils;

class EsObjectStringConverter extends ConvertUtilsObjectStringConverter {
    public Object stringToObject(String value, Class type, String flavour, Context context) {
        if (value == null || "".equals(value)){
            return null;
        } else if (type.isEnum()) {
            return Enum.valueOf(type, value);
        } else {
            return ConvertUtils.convert( value, type );
        }
    }
}
