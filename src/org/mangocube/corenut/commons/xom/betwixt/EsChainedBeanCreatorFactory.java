package org.mangocube.corenut.commons.xom.betwixt;

import org.mangocube.corenut.commons.bean.InstanceFactory;
import org.mangocube.corenut.commons.bean.InstanceFactory.InstanceManage;
import org.mangocube.corenut.commons.util.ClassUtils;
import org.apache.commons.betwixt.ElementDescriptor;
import org.apache.commons.betwixt.Options;
import org.apache.commons.betwixt.XMLBeanInfo;
import org.apache.commons.betwixt.io.read.BeanCreationChain;
import org.apache.commons.betwixt.io.read.BeanCreationList;
import org.apache.commons.betwixt.io.read.ChainedBeanCreator;
import org.apache.commons.betwixt.io.read.ElementMapping;
import org.apache.commons.betwixt.io.read.ReadContext;
import org.apache.commons.logging.Log;

import java.beans.IntrospectionException;
import java.util.Arrays;

/**
 * Group of factory methods for <code>ChainedBeanCreator</code>'s.
 * The standard implementations used by Betwixt are present here.
 *
 * @author Robert Burrell Donkin
 * @since 0.5
 */
class EsChainedBeanCreatorFactory {

    /**
     * Creates the <code>BeanCreationChain</code> used when reading beans.
     *
     * @return a <code>BeanCreationList</code> with the creators loader in order, not null
     */
    public static BeanCreationChain createChain() {
        BeanCreationList chain = new BeanCreationList();
        chain.addBeanCreator(instanceFactoryCreator);
        chain.addBeanCreator(derivedBeanCreator);
        chain.addBeanCreator(elementTypeBeanCreator);
        return chain;
    }

    /**
     * Singleton instance that creates beans based on IDREF
     */
    private static final ChainedBeanCreator idRefBeanCreator = new ChainedBeanCreator() {
        public Object create(ElementMapping elementMapping, ReadContext context, BeanCreationChain chain) {
            if (!context.getMapIDs()) {
                return chain.create(elementMapping, context);
            }

            String idref = elementMapping.getAttributes().getValue("idref");
            if (idref != null) {
                // XXX need to check up about ordering
                // XXX this is a very simple system that assumes that
                // XXX id occurs before idrefs
                // XXX would need some thought about how to implement a fuller system
                context.getLog().trace("Found IDREF");
                Object bean = context.getBean(idref);
                if (bean != null) {
                    if (context.getLog().isTraceEnabled()) {
                        context.getLog().trace("Matched bean " + bean);
                    }
                    return bean;
                }
                context.getLog().trace("No match found");
            }
            return chain.create(elementMapping, context);
        }
    };

    /**
     * Singleton instance for creating derived beans
     */
    private static final ChainedBeanCreator instanceFactoryCreator = new ChainedBeanCreator() {
        private static final String OPT_INSTANCE_FACTORY = "org.mangocube.corenut.commons.xom.betwixt.instance-factory";
        private static final String OPT_INSTANCE_FACTORY_INSMGR = "org.mangocube.corenut.commons.xom.betwixt.factory-instance-manage";
        private static final String OPT_FACTORY_METHOD_ARGS = "org.mangocube.corenut.commons.xom.betwixt.factory-method-arg-atts";

        public Object create(ElementMapping element, ReadContext context, BeanCreationChain chain) {
            Log log = context.getLog();

            Options opts = element.getDescriptor().getOptions();
            String opt_factory = opts.getValue(OPT_INSTANCE_FACTORY);
            String opt_method_args_attrs = opts.getValue(OPT_FACTORY_METHOD_ARGS);
            if (opt_factory == null || opt_method_args_attrs == null) {
                return chain.create(element, context);
            }

            String[] opt_method_args = opt_method_args_attrs.split(",");
            if (opt_method_args.length == 0) {
                return chain.create(element, context);
            }

            String[] opt_method_args_vals;
            if (opt_method_args.length == 1) {
                opt_method_args_vals = element.getAttributes().getValue(opt_method_args[0]).split("/");
            } else {
                opt_method_args_vals = new String[opt_method_args.length];
                for (int i = 0; i < opt_method_args.length; i++) {
                    opt_method_args_vals[i] = element.getAttributes().getValue(opt_method_args[i]);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Use instance factory to create bean....");
                log.debug("Instance factory: " + opt_factory);
                log.debug("         keys:" + Arrays.toString(opt_method_args_vals));
            }

            String opt_factory_insmgr = opts.getValue(OPT_INSTANCE_FACTORY_INSMGR);
            try {
                String factory_key = "INSTANCE_FACTORY_" + element.getDescriptor().hashCode();
                InstanceFactory factory = (InstanceFactory) context.getBean(factory_key);
                if (factory == null) {
                    InstanceManage ins_mgr_opt = opt_factory_insmgr == null ? InstanceManage.NA : InstanceManage.valueOf(opt_factory_insmgr);
                    factory = new InstanceFactory(opt_factory, ins_mgr_opt);
                    context.putBean(factory_key, factory);
                }
                return factory.getInstance(opt_method_args_vals);
            } catch (Exception e) {
                log.warn("Fail to create instance using factory!", e);
                return null;
            }
        }
    };

    /**
     * Singleton instance for creating derived beans
     */
    private static final ChainedBeanCreator derivedBeanCreator = new ChainedBeanCreator() {
        public Object create(ElementMapping elementMapping, ReadContext context, BeanCreationChain chain) {
            Log log = context.getLog();

            String className = elementMapping.getAttributes().getValue(context.getClassNameAttribute());

            if (className == null) {
                // pass responsibility down the chain
                return chain.create(elementMapping, context);
            }

            try {
                // load the class we should instantiate
                ClassLoader classLoader = context.getClassLoader();
                Class clazz = null;
                if (classLoader == null) {
                    log.warn("Read context classloader not set.");
                } else {
                    try {
                        clazz = classLoader.loadClass(className);
                    } catch (ClassNotFoundException e) {
                        log.info("Class not found in context classloader:");
                        log.debug(clazz, e);
                    }
                }
                classLoader = Thread.currentThread().getContextClassLoader();
                if (classLoader == null) {
                    clazz = Class.forName(className);
                } else {
                    Class.forName(className, true, classLoader);
                }
                return newInstance(clazz, elementMapping, log);
            } catch (Exception e) {
                // it would be nice to have a pluggable strategy for exception management
                log.warn("Could not create instance of type: " + className);
                log.debug("Create new instance failed: ", e);
                return null;
            }
        }
    };

    private static final String INSTANCE_CONSTRUCTOR = "org.mangocube.corenut.commons.xom.betwixt.instance-constructor";

    /**
     * Constructs a new instance of the given class.
     * Access is forced.
     *
     * @param theClass       <code>Class</code>, not null
     * @param elementMapping
     * @param log            <code>Log</code>, not null @return <code>Object</code>, an instance of the given class
     * @throws Exception creation error
     */
    private static Object newInstance(Class theClass, ElementMapping elementMapping, Log log) throws Exception {
        Object result = null;

        String construct_opt = elementMapping.getDescriptor().getOptions().getValue(INSTANCE_CONSTRUCTOR);
        try {
            if (construct_opt == null) {
                result = ClassUtils.createInstance(theClass);
            } else {
                String[] args = construct_opt.split(",");
                String[] arg_vals = new String[args.length];
                for (int i = 0; i < args.length; i++) {
                    arg_vals[i] = elementMapping.getAttributes().getValue(args[i].trim());
                }
                result = ClassUtils.createInstance(theClass, arg_vals);
            }
        } catch (Exception e) {
            // it would be nice to have a pluggable strategy for exception management
            log.warn("Could not create instance of type: " + theClass.getName());
            log.debug("Create new instance failed: ", e);
        }
        return result;
    }

    /**
     * Singleton instance that creates beans based on type
     */
    private static final ChainedBeanCreator elementTypeBeanCreator = new ChainedBeanCreator() {
        public Object create(ElementMapping element, ReadContext context, BeanCreationChain chain) {
            Log log = context.getLog();
            Class theClass = null;

            ElementDescriptor descriptor = element.getDescriptor();
            if (descriptor != null) {
                // check for polymorphism
                theClass = context.resolvePolymorphicType(element);

                if (theClass == null) {
                    // created based on implementation class
                    theClass = descriptor.getImplementationClass();
                }
            }

            if (theClass == null) {
                // create based on type
                theClass = element.getType();
            }

            if (descriptor != null && descriptor.isPolymorphic()) {
                // check that the type is suitably named
                try {
                    XMLBeanInfo xmlBeanInfo = context.getXMLIntrospector().introspect(theClass);
                    String namespace = element.getNamespace();
                    String name = element.getName();
                    if (namespace == null) {
                        if (!name.equals(xmlBeanInfo.getElementDescriptor().getQualifiedName())) {
                            context.getLog().debug("Polymorphic type does not match element");
                            return null;
                        }
                    } else if (!namespace.equals(xmlBeanInfo.getElementDescriptor().getURI())
                            || !name.equals(xmlBeanInfo.getElementDescriptor().getLocalName())) {
                        context.getLog().debug("Polymorphic type does not match element");
                        return null;
                    }
                } catch (IntrospectionException e) {
                    context.getLog().warn("Could not introspect type to test introspection: " + theClass.getName());
                    context.getLog().debug("Introspection failed: ", e);
                    return null;
                }

            }

            if (log.isTraceEnabled()) {
                log.trace("Creating instance of class " + theClass.getName() + " for element " + element.getName());
            }

            try {
                return newInstance(theClass, element, log);
            } catch (Exception e) {
                // it would be nice to have a pluggable strategy for exception management
                context.getLog().warn("Could not create instance of type: " + theClass.getName());
                context.getLog().debug("Create new instance failed: ", e);
                return null;
            }
        }
    };
}
