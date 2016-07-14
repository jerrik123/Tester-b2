package org.mangocube.corenut.commons.jta.resolver;


import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.mangocube.corenut.commons.jta.TransactionEnvironmentException;

/**
 * In DEV environment, the JTA manager is provided by the tecsgo container.
 * The jta manager is bind to the JNDI with name "UserTransaction" already.
 * Thus, this class just retrieve this transaction manager with context look
 * up.
 *
 * @since 1.0
 */
public class DevEnvJTAManagerResolver implements TransactionManagerResolver {
    private static final String JTA_JNDI_NAME = "UserTransaction";

    public TransactionManager resolve() throws TransactionEnvironmentException {
        try {
            Context ctx = new InitialContext();
            return (TransactionManager) ctx.lookup(JTA_JNDI_NAME);
        } catch (NamingException e) {
            throw new TransactionEnvironmentException(TransactionManagerResolveError.LOOKUP_JTA_MANAGER_FAILED, e, JTA_JNDI_NAME);
        }
    }
}
