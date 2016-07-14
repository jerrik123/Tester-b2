package org.mangocube.corenut.commons.jta.resolver;

import org.mangocube.corenut.commons.exception.ErrorCode;
import org.mangocube.corenut.commons.jta.TransactionEnvironmentException;

import javax.transaction.TransactionManager;

/**
 * For the sake of using different transaction manager according to the different environment, implements
 * this resolver interface and choose the right instance by inspecting the environment type with the help of
 * EnvironmentDetector.
 *
 * @since 1.0
 */
public interface TransactionManagerResolver {
    public enum TransactionManagerResolveError {
        @ErrorCode(comment = "Lookup jta manager with JNDI name ${1} failed!")
        LOOKUP_JTA_MANAGER_FAILED,
        @ErrorCode(comment = "Could not find WebSphere 5.1/6.0/6.1 TransactionManager factory class ${1}!")
        LOAD_WS_51_FACTORY_CLASS_FAILED,
        @ErrorCode(comment = "WebSphere's TransactionManagerFactory.getTransactionManager method failed!")
        WS_FACTORY_METHOD_CALL_FAILED,
        @ErrorCode(comment = "Could not found the method ${1} class ${2}.")
        METHOD_NOT_FOUND_ERROR,
        @ErrorCode(comment = "Invoke method ${1} failed!")
        METHOD_INVOCATION_FAILED
    }

    public TransactionManager resolve() throws TransactionEnvironmentException;

}
