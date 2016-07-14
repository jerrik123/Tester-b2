package org.mangocube.corenut.commons.xom.betwixt;

import org.mangocube.corenut.commons.util.ClassLoaderResolver;
import org.apache.commons.betwixt.digester.RuleSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.ref.WeakReference;

/**
 * <p>Factors out common code used by Betwixt rules that access bean properties.
 * Maybe a lot of this should be moved into <code>BeanUtils</code>.</p>
 * @since 1.0
 */
abstract class EsMappedPropertyRule extends RuleSupport {

    /**
     * Logger
     */
    private static final Log log = LogFactory.getLog(EsMappedPropertyRule.class);

    /**
     * Classloader used to load classes by name
     */
    private WeakReference<ClassLoader> classLoader;

    /**
     * Base constructor
     */
    public EsMappedPropertyRule() {
        this.classLoader = new WeakReference<ClassLoader>(ClassLoaderResolver.getClassLoader());
    }

    // Implementation methods
    //-------------------------------------------------------------------------    

    /**
     * Returns the property descriptor for the class and property name.
     * Note that some caching could be used to improve performance of
     * this method. Or this method could be added to PropertyUtils.
     *
     * @param beanClass    descriptor for property in this class
     * @param propertyName descriptor for property with this name
     * @return property descriptor for the named property in the given class
     */
    protected PropertyDescriptor getPropertyDescriptor(Class beanClass,
                                                       String propertyName) {
        if (beanClass == null || propertyName == null) return null;

        if (log.isTraceEnabled()) {
            log.trace("Searching for property " + propertyName + " on " + beanClass);
        }

        PropertyDescriptor pty_desc = null;

        if (propertyName.indexOf('.') > 0) {
            String[] pty_names = propertyName.split("\\.");
            Class bean_cls = beanClass;
            for (String pty_name : pty_names) {
                pty_desc = getBeanPropertyDescriptor(bean_cls, pty_name);
                bean_cls = pty_desc.getPropertyType();
            }
        } else {
            pty_desc = getBeanPropertyDescriptor(beanClass, propertyName);
        }

        return pty_desc;
    }

    private PropertyDescriptor getBeanPropertyDescriptor(Class beanClass, String propertyName) {
        try {
            // TODO: replace this call to introspector to an object call
            // which finds all property descriptors for a class. this allows extra property descriptors to be added
            BeanInfo beanInfo;
            if (getXMLIntrospector().getConfiguration().ignoreAllBeanInfo()) {
                beanInfo = Introspector.getBeanInfo(beanClass, Introspector.IGNORE_ALL_BEANINFO);
            } else {
                beanInfo = Introspector.getBeanInfo(beanClass);
            }
            PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
            if (descriptors != null) {
                for (int i = 0, size = descriptors.length; i < size; i++) {
                    PropertyDescriptor descriptor = descriptors[i];
                    if (propertyName.equals(descriptor.getName())) {
                        log.trace("Found matching method.");
                        return descriptor;
                    }
                }
            }
            // for interfaces, check all super interfaces
            if (beanClass.isInterface()) {
                Class[] superinterfaces = beanClass.getInterfaces();
                for (int i = 0, size = superinterfaces.length; i < size; i++) {
                    PropertyDescriptor descriptor = getPropertyDescriptor(superinterfaces[i], propertyName);
                    if (descriptor != null) {
                        return descriptor;
                    }
                }
            }

            log.trace("No match found.");
        } catch (Exception e) {
            log.warn("Caught introspection exception", e);
        }

        return null;
    }


    /**
     * Gets the type of a property
     *
     * @param propertyClassName class name for property type (may be null)
     * @param beanClass         class that has property
     * @param propertyName      the name of the property whose type is to be determined
     * @return property type
     */
    protected Class getPropertyType(String propertyClassName,
                                    Class beanClass, String propertyName) {
        // XXX: should use a ClassLoader to handle  complex class loading situations
        if (propertyClassName != null) {
            try {
                Class answer = classLoader.get().loadClass(propertyClassName);
                if (answer != null) {
                    if (log.isTraceEnabled()) {
                        log.trace("Used specified type " + answer);
                    }
                    return answer;
                }
            } catch (Exception e) {
                log.warn("Cannot load specified type", e);
            }
        }

        PropertyDescriptor descriptor = getPropertyDescriptor(beanClass, propertyName);
        if (descriptor != null) {
            return descriptor.getPropertyType();
        }

        if (log.isTraceEnabled()) {
            log.trace("Cannot find property type.");
            log.trace("  className=" + propertyClassName + " base=" + beanClass + " name=" + propertyName);
        }
        return null;
    }
}
