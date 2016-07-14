package org.mangocube.corenut.commons.jta.resolver;


import javax.transaction.TransactionManager;

import org.mangocube.corenut.commons.jta.TransactionEnvironmentException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Resolves the WebSphere's transaction manager by invoking the transactionManagerFactory's getTransactionManager() method.
 * This resolver will be used when the application is deployed on the production environment.
 *
 * @since 1.0
 */
public class ProductionEnvJTAManagerResolver implements TransactionManagerResolver {
    private static final String WS_5_1_JTA_MANAGER_FACTORY = "com.ibm.ws.Transaction.TransactionManagerFactory";
    private static final String GET_TRANSACTION_MANAGER_METHOD = "getTransactionManager";


    public TransactionManager resolve() throws TransactionEnvironmentException {
        try {
            // Using the thread context class loader for compatibility with the WSAD test server.
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(WS_5_1_JTA_MANAGER_FACTORY);
            Method method = clazz.getMethod(GET_TRANSACTION_MANAGER_METHOD, (Class[]) null);
            return (TransactionManager) method.invoke(null, (Object[]) null);
        }
        catch (ClassNotFoundException ex) {
            throw new TransactionEnvironmentException(TransactionManagerResolveError.LOAD_WS_51_FACTORY_CLASS_FAILED, ex, WS_5_1_JTA_MANAGER_FACTORY);
        }
        catch (InvocationTargetException ex) {
            throw new TransactionEnvironmentException(TransactionManagerResolveError.WS_FACTORY_METHOD_CALL_FAILED, ex.getTargetException());
        } catch (NoSuchMethodException e) {
            throw new TransactionEnvironmentException(TransactionManagerResolveError.METHOD_NOT_FOUND_ERROR, e, GET_TRANSACTION_MANAGER_METHOD, WS_5_1_JTA_MANAGER_FACTORY);
        } catch (IllegalAccessException e) {
            throw new TransactionEnvironmentException(TransactionManagerResolveError.METHOD_INVOCATION_FAILED, e, GET_TRANSACTION_MANAGER_METHOD);
        }
    }
}
