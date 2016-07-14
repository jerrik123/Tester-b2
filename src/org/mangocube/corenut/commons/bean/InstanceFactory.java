
package org.mangocube.corenut.commons.bean;

import org.mangocube.corenut.commons.devprocess.CoverageMetric;
import org.mangocube.corenut.commons.exception.ErrorCode;
import org.mangocube.corenut.commons.util.ClassUtils;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;

/**
 * Factory that return instance according to configuration. The config is stored in property file, with composite keys.
 * The keys are defined by user arbitrarily, and value is the matched Class full name.
 * When retrieve instance by InstanceFactory, the factory will check its pool first, try to get cached instance if available.
 * If no cached instance is found, then retrieve matched class name from property file and create the instance. Afterwards,
 * put instance into pool when necessary, and return the instance.
 *
 * @since 1.0
 */
@CoverageMetric(metric = CoverageMetric.Level.STRICT)
public class InstanceFactory<T> {
    public enum FactoryError {
        @ErrorCode(comment = "Load factory configuration properties file(s) [${1}]  fails!")
        LOAD_FACTORY_CONFIG_FAIL,
        @ErrorCode(comment = "No class is defined for key ${1} !")
        NO_CLASS_DEF,
        @ErrorCode(comment = "Fail to create instance of class ${1}!")
        CREATE_INSTANCE_FAIL,
        @ErrorCode(comment = "Fail to initialize instance of class ${1}!")
        INI_INSTANCE_FAIL
    }

    protected CompositeKeyProperties factoryProperties;
    protected Map<String, T> instancePool;

    private InstanceManage insMgrOpt = InstanceManage.NA;

    public enum InstanceManage {
        /**
         * No instance pool.
         */
        NA,
        /**
         * Cache only one instance for individual key
         */
        CACHE_BY_KEY,
        /**
         * All keys under certain catalog will share the same instance
         */
        CACHE_BY_CATALOG,
        /**
         * For certain object class, only cache one instance.
         */
        REUSE_INSTANCE
    }

    /**
     * new an instance with the resource pattern and instance manager. *
     * <p/>
     * <b>WARNING:</b> Note that "<code>classpath*:</code>" when combined
     * with Ant-style patterns will only work reliably with at least one root
     * directory before the pattern starts, unless the actual target files
     * reside in the file system. This means that a pattern like "<code>classpath*:*.xml</code>"
     * will <i>not</i> retrieve files from the root of jar files but rather
     * only from the root of expanded directories. This originates from a
     * limitation in the JDK's <code>ClassLoader.getResources()</code> method
     * which only returns file system locations for a passed-in empty String
     * (indicating potential roots to search). guaranteed to find matching
     * resources if the root package to search is available in multiple class
     * path locations. This is because a resource such as
     * <p/>
     * <pre>
     * com / mycompany / package1 / service-context.properties
     * </pre>
     * <p/>
     * may be in only one location, but when a path such as
     * <p/>
     * <pre>
     *     classpath:com/mycompany/** /service-context.properties
     * </pre>
     * <p/>
     * is used to try to resolve it, the resolver will work off the (first) URL
     * returned by <code>getResource("com/mycompany");</code>. If this base
     * package node exists in multiple classloader locations, the actual end
     * resource may not be underneath. Therefore, preferably, use "<code>classpath*:<code>" with the same
     * Ant-style pattern in such a case, which will search <i>all</i> class path
     * locations that contain the root package.
     *
     * @param resourcePattern the ant-style pattern support. *
     * @param mgrOpt          instance pool option
     */
    public InstanceFactory(String resourcePattern, InstanceManage mgrOpt) {
        construct(resourcePattern, mgrOpt, null);
    }

    public InstanceFactory(Class cls, InstanceManage mgrOpt) {
        construct(cls.getName().replace('.', '/') + ".properties", mgrOpt, null);
    }

    public InstanceFactory(String resourcePattern, InstanceManage mgrOpt, ClassLoader classLoader) {
        construct(resourcePattern, mgrOpt, classLoader);
    }

    public InstanceFactory(Class cls, InstanceManage mgrOpt, ClassLoader classLoader) {
        construct(cls.getName().replace('.', '/') + ".properties", mgrOpt, classLoader);
    }

    public InstanceFactory(CompositeKeyProperties factoryProperties, InstanceManage mgrOpt) {
        this.factoryProperties = factoryProperties;

        if (!mgrOpt.equals(InstanceManage.NA) && instancePool == null) {
            instancePool = new ConcurrentHashMap<String, T>();
        }
        insMgrOpt = mgrOpt;
    }

    private void construct(String resourcePattern, InstanceManage mgrOpt, ClassLoader classLoader) {
        factoryProperties = new CompositeKeyProperties();
        try {
            factoryProperties.load(resourcePattern, classLoader);
        } catch (IOException e) {
            throw new FactoryException(FactoryError.LOAD_FACTORY_CONFIG_FAIL, e, resourcePattern);
        }

        if (!mgrOpt.equals(InstanceManage.NA)) instancePool = new ConcurrentHashMap<String, T>();
        insMgrOpt = mgrOpt;
    }

    public Properties getFactoryProperties() {
        return new Properties(factoryProperties);
    }

    /**
     * Get instance by key. Factory will try to retrieve corresponding entry from properties files with given key.
     * The key is composite and may comprise several parts, usually it's string array, and it may also be single string
     * which delimiter is "/". Once matched entry is found, factory will create/return instance according to
     * InstanceManage option. If the instance implements org.mangocube.corenut.commons.bean.BeanLifecycle, then onCreate
     * method will be invoked.  
     *
     * @param keys composite keys to retrieve corresponding instance.
     * @return object instance mathed the given keys. if not matched, return null. 
     */
    public T getInstance(String... keys) {
        if (keys.length == 1 && keys[0].indexOf('/') > 0) {
            keys = keys[0].split("//");
        }

        CompositeKeyProperties.MatchProperty match = factoryProperties.getMatchedProperty(keys);
        if (match.getPropertyValue() == null) {
            if (match.getMatchedKey() == null) {
                return null;
            } else {
                throw new FactoryException(FactoryError.NO_CLASS_DEF, match.getPropertyKey());
            }
        }

        T instance = null;
        String instance_key = getInstanceKey(match);
        String cls_name = match.getPropertyValue();

        synchronized (cls_name) {
            if (instancePool != null) {
                instance = instancePool.get(instance_key);
            }

            if (instance == null) {
                instance = createInstance(cls_name);

                try {
                    initialize(instance);
                } catch (Exception e) {
                    throw new FactoryException(FactoryError.INI_INSTANCE_FAIL, e, cls_name);
                }

                if (instancePool != null) {
                    instancePool.put(instance_key, instance);
                }
            }
        }

        return instance;
    }

    protected String getInstanceKey(CompositeKeyProperties.MatchProperty match) {
        String ins_key = "";
        switch (insMgrOpt) {
            case CACHE_BY_KEY:
                ins_key = match.getPropertyKey();
                break;
            case CACHE_BY_CATALOG:
                ins_key = match.getMatchedKey();
                break;
            case REUSE_INSTANCE:
                ins_key = match.getPropertyValue();
                break;
            default:
                // nothing to do
        }

        return ins_key;
    }

    private T createInstance(String clsName) {
        if (clsName.contains(")")) {
            return createInstanceByClassWithParameters(clsName);
        } else {
            return createInstanceByClass(clsName);
        }
    }

    /**
     * This method is for creating the object instance which defined in the properties file and followed with the parameters.
     * Using this method to create the object instance have some limitations:
     * 1. If the class have some override constructors with the same quantity of parameters, then this method just pick up
     * the first one when iteration, this may cause the exception.
     * 2. when cannot find any of the constructor matches the given parameters quantity, then will throw NoSuchMethodException.
     *
     * @param clsName name of the instance object type
     * @return new instance of the object
     */
    protected T createInstanceByClassWithParameters(String clsName) {
        String actual_cls_name = clsName.substring(0, clsName.indexOf("("));
        String param_str = clsName.substring(clsName.indexOf("(") + 1, clsName.indexOf(")"));
        if (param_str == null || "".equals(param_str.trim()))
            throw new FactoryException(FactoryError.CREATE_INSTANCE_FAIL, clsName);
        String[] params = param_str.trim().split(",");
        try {
            return (T) ClassUtils.createInstance(ClassUtils.lookupClass(actual_cls_name), params);
        } catch (Exception e) {
            throw new FactoryException(FactoryError.CREATE_INSTANCE_FAIL, e, clsName);
        }
    }

    protected T createInstanceByClass(String clsName) {
        try {
            return (T) ClassUtils.createInstance(ClassUtils.lookupClass(clsName));
        } catch (Exception e) {
            throw new FactoryException(FactoryError.CREATE_INSTANCE_FAIL, e, clsName);
        }
    }

    protected void initialize(T instance) throws Exception {
        if (instance instanceof BeanLifecycle) {
            ((BeanLifecycle) instance).onCreate();
        }
    }
}
