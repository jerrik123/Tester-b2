package org.mangocube.corenut.commons.jta;

import javax.transaction.TransactionManager;

import org.mangocube.corenut.commons.bean.InstanceFactory;
import org.mangocube.corenut.commons.environment.EnvironmentDetector;
import org.mangocube.corenut.commons.jta.resolver.TransactionManagerResolver;
import org.springframework.beans.factory.FactoryBean;

/**
 * This is the factory bean which take the responsibility of creating the transaction manager for specified environment.
 * It resolves the corresponding JTA manager according to the environment type (PROD/DEV).
 * Each configuration should replace the old fashion to this implementation.
 * 
 * @since 1.0
 */
public class TransactionManagerAdapter implements FactoryBean {
    private static final String PRODUCTION_JTA_ENV = "PROD";
    private static final String DEV_JTA_ENV = "DEV";
    private final TransactionManager transactionManager;
    private static final InstanceFactory<TransactionManagerResolver> resolverFactory = new InstanceFactory<TransactionManagerResolver>("/org/mangocube/corenut/commons/jta/JTAManagerResolver.properties", InstanceFactory.InstanceManage.REUSE_INSTANCE);

    public TransactionManagerAdapter() throws TransactionEnvironmentException {
        //the environment detector can inspect whether the current runtime environment is production or development
        EnvironmentDetector detector = EnvironmentDetector.getInstance();
        String env = detector.isDev() ? DEV_JTA_ENV : PRODUCTION_JTA_ENV;
        //construct the transaction manager resolver according to the environment type
        TransactionManagerResolver tmResolver = resolverFactory.getInstance(env);
        //and then invokes the resolve method to resolve the transaction manager instance
        this.transactionManager = tmResolver.resolve();
    }

    public Object getObject() {
        return this.transactionManager;
    }

    public Class getObjectType() {
        return this.transactionManager.getClass();
    }

    public boolean isSingleton() {
        return true;
    }
}
