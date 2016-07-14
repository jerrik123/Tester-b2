
package org.mangocube.corenut.commons.bean;

import org.mangocube.corenut.commons.exception.UncheckedException;

/**
 * Instance factory exception, thrown instance creation exceptions. 
 *
 * @since 1.0
 */
public class FactoryException extends UncheckedException {
	public FactoryException(Enum anEnum, Object... strings) {
        super(anEnum, strings);
    }

    public FactoryException(Enum anEnum, Throwable throwable, Object... strings) {
        super(anEnum, throwable, strings);
    }
}
