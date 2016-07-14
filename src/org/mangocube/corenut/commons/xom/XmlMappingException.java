package org.mangocube.corenut.commons.xom;

import org.mangocube.corenut.commons.exception.UncheckedException;


public class XmlMappingException extends UncheckedException {
    public XmlMappingException(Enum errorCode) {
        super(errorCode);
    }

    public XmlMappingException(Enum errorCode, Object... para) {
        super(errorCode, para);
    }

    public XmlMappingException(Enum errorCode, Throwable throwable, Object... para) {
        super(errorCode, throwable, para);
    }
}
