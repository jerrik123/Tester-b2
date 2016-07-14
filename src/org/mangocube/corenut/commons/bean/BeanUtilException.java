
package org.mangocube.corenut.commons.bean;

import org.mangocube.corenut.commons.exception.UncheckedException;

/**
 * Bean property util exception.
 * @since 1.0
 */
public class BeanUtilException extends UncheckedException {
    public BeanUtilException(Enum errorCode) {
        super(errorCode);
    }

    public BeanUtilException(Enum errorCode, Object... para) {
        super(errorCode, para);
    }

    public BeanUtilException(Enum errorCode, Throwable throwable) {
        super(errorCode, throwable);
    }

    public BeanUtilException(Enum errorCode, Throwable throwable, Object... para) {
        super(errorCode, throwable, para);
    }
}
