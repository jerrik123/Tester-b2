package org.mangocube.corenut.commons.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.ConvertUtils;

/**
 * This util defines some useful method to obtain the information about the given class.
 * <p/>
 *
 * @version Revision History
 *          <p/>
 *          Author     Version         Date        Changes
 *          Howard      1.0         ${Date}     Created
 * @since 1.0
 */
public abstract class ClassUtils {
    private static final List<Class> EMPTY_INTERF_LIST = Collections.unmodifiableList(new ArrayList<Class>(0));

    /**
     * Gets all implemented super interfaces for the specified class.
     *
     * @param clazz class to be audit.
     * @return all implemented interfaces (includes super interfaces), excludes JDK interfaces.
     *         if no implemented interface, return empty list.
     */
    public static List<Class> getAllImplementedInterfaces(Class clazz) {
        return getAllImplementedInterfaces(clazz, true);
    }

    /**
     * Gets all implemented super interfaces for the specified class.
     *
     * @param clazz            class to be audit.
     * @param excludeJDKInterf indicate whether to exclude JDK interfaces.
     * @return all implemented interfaces (includes super interfaces), if no implemented interface, return empty list.
     */
    public static List<Class> getAllImplementedInterfaces(Class clazz, boolean excludeJDKInterf) {
        if (clazz == null) return EMPTY_INTERF_LIST;

        List<Class> superInterf = new ArrayList<Class>();
        Class[] currentInterf = clazz.getInterfaces();
        for (Class inter : currentInterf) {
            if (excludeJDKInterf && (inter.getName().startsWith("java.") || inter.getName().startsWith("javax.")))
                continue;

            superInterf.add(inter);
            List<Class> uperInterfs = getAllImplementedInterfaces(inter);
            if (uperInterfs.size() > 0) {
                superInterf.addAll(uperInterfs);
            }
        }
        return superInterf.isEmpty() ? EMPTY_INTERF_LIST : superInterf;
    }

    /**
     * Use caller class loader to lookup Class with given class name.
     *
     * @param clazzName class name
     * @return Class
     * @throws ClassNotFoundException lookup fail, not found.
     */
    public static Class lookupClass(String clazzName) throws ClassNotFoundException {
        return lookupClass(clazzName, ClassLoaderResolver.getClassLoader());
    }

    public static Class lookupClass(String clazzName, ClassLoader loader) throws ClassNotFoundException {
        if (clazzName == null) return null;

        if (loader == null) {//if no class loader is specified, then use ClassUtils by default.
            loader = ClassUtils.class.getClassLoader();
        }

        Class cls;
        try {
            //1. try the sepcific class loader first.
            cls = loader.loadClass(clazzName);
        } catch (ClassNotFoundException e) {
            //2. try the current thread class loader.
            loader = Thread.currentThread().getContextClassLoader();
            if (loader != null) {
                cls = loader.loadClass(clazzName);
            } else {
                throw e;
            }
        }
        return cls;
    }

    private static final Class[] EMPTY_CLASS_ARRAY = {};
    private static final Object[] EMPTY_OBJECT_ARRAY = {};

    public static <T> T createInstance(Class<T> clazz, String... params) throws IllegalAccessException,
            InstantiationException, InvocationTargetException, NoSuchMethodException {
        if (params == null || params.length == 0) {//no parameterms, use default constructor
            Constructor constructor = clazz.getConstructor(EMPTY_CLASS_ARRAY);
            try {
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                return (T) constructor.newInstance(EMPTY_OBJECT_ARRAY);
            } catch (Exception e) {
                return clazz.newInstance();
            }
        }

        //Gets all the constructors of this class.
        Constructor[] constructors = clazz.getConstructors();
        //Iterates constructors and find one that matches the given parameters quantity & type
        for (Constructor constructor : constructors) {
            if (constructor.getParameterTypes().length == params.length) {
                Object[] arguments = convert2ParameterType(params, constructor);
                if (arguments == null) continue;
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                return (T) constructor.newInstance(arguments);
            }
        }
        throw new NoSuchMethodException("No such constructor of " + clazz + " " + Arrays.toString(params));
    }

    /**
     * Converts the string parameters to the actual type of parameters used in the constructor.
     *
     * @param params      parameter values in the type of String
     * @param constructor instance of constructor object
     * @return parameters with the correct type
     */
    private static Object[] convert2ParameterType(String[] params, Constructor constructor) {
        Object[] arguments = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            String para_val = params[i];

            if (para_val == null) {
                arguments[i] = null;
            } else {
                para_val = para_val.trim();
                try {
                    Class para_type = constructor.getParameterTypes()[i];
                    if (Enum.class.isAssignableFrom(para_type)) {
                        arguments[i] = Enum.valueOf(para_type, para_val);
                    } else {
                        arguments[i] = ConvertUtils.convert(para_val, para_type);
                    }
                } catch (Exception e) {
                    return null;//if conversion fails, means the parameters somehow mismatch the contructor, skip!
                }
            }
        }
        return arguments;
    }
}
