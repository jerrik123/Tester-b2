package org.mangocube.corenut.commons.jta;

import org.mangocube.corenut.commons.exception.UncheckedException;

/**
 * When retrieve the corresponding transaction manager at runtime failed, then raise this exception.
 *
 * @since 1.0
 */
public class TransactionEnvironmentException extends UncheckedException {
    public TransactionEnvironmentException(Enum errorCode) {
        super(errorCode);
    }

    public TransactionEnvironmentException(Enum errorCode, Object... para) {
        super(errorCode, para);
    }

    public TransactionEnvironmentException(Enum errorCode, Throwable throwable) {
        super(errorCode, throwable);
    }

    public TransactionEnvironmentException(Enum errorCode, Throwable throwable, Object... para) {
        super(errorCode, throwable, para);
    }
}
