package org.mangocube.corenut.commons.xom.betwixt;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ExtendedBaseRules;
import org.apache.commons.digester.RuleSet;
import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.BindingConfiguration;
import org.apache.commons.betwixt.XMLBeanInfo;
import org.apache.commons.betwixt.ElementDescriptor;
import org.apache.commons.betwixt.strategy.HyphenatedNameMapper;
import org.apache.commons.betwixt.io.read.ReadConfiguration;
import org.apache.commons.betwixt.io.read.ReadContext;
import org.apache.commons.betwixt.io.BeanRuleSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.util.List;
import java.util.ArrayList;
import java.lang.ref.WeakReference;
import java.beans.IntrospectionException;
import java.io.*;

/**
 * <p><code>XMLBeanReader</code> reads a tree of beans from an XML document. It's a SAX event handler.</p>
 * <p/>
 * <p>Call {@link #registerBeanClass(Class)} or {@link #registerBeanClass(String, Class)}
 * to add rules to map a bean class.</p>
 *
 * @since 1.0
 */
class XMLBeanReadingHandler extends Digester {
    /**
     * Introspector used
     */
    private XMLIntrospector introspector;
    /**
     * Log used for logging (Doh!)
     */
    private Log log = LogFactory.getLog(XMLBeanReadingHandler.class);
    /**
     * The registered classes
     */
    private List<WeakReference<Class>> registeredClasses = new ArrayList<WeakReference<Class>>();
    /**
     * Dynamic binding configuration settings
     */
    private BindingConfiguration bindingConfiguration;
    /**
     * Reading specific configuration settings
     */
    private ReadConfiguration readConfiguration;

    /**
     * Construct a new BeanReader with default properties.
     */
    public XMLBeanReadingHandler() {
        setRules(new ExtendedBaseRules());
    }

    //****************************** Properties ********************************//
    /**
     * <p> Get the introspector used. </p>
     * <p/>
     * <p> The {@link org.apache.commons.betwixt.XMLBeanInfo} used to map each bean is
     * created by the <code>XMLIntrospector</code>.
     * One way in which the mapping can be customized is by
     * altering the <code>XMLIntrospector</code>. </p>
     *
     * @return the <code>XMLIntrospector</code> used for the introspection
     */
    public XMLIntrospector getXMLIntrospector() {
        if (introspector == null) {
            introspector = new EsXMLIntrospector();
            introspector.getConfiguration().setAttributeNameMapper(new HyphenatedNameMapper());
            introspector.getConfiguration().setElementNameMapper(new HyphenatedNameMapper());
        }
        return introspector;
    }


    /**
     * <p> Set the introspector to be used. </p>
     * <p/>
     * <p> The {@link org.apache.commons.betwixt.XMLBeanInfo} used to map each bean is
     * created by the <code>XMLIntrospector</code>.
     * One way in which the mapping can be customized is by
     * altering the <code>XMLIntrospector</code>. </p>
     *
     * @param introspector use this introspector
     */
    public void setXMLIntrospector(XMLIntrospector introspector) {
        this.introspector = introspector;
    }

    /**
     * <p> Get the current level for logging. </p>
     *
     * @return the <code>Log</code> implementation this class logs to
     */
    public Log getLog() {
        return log;
    }

    /**
     * <p> Set the current logging level. </p>
     *
     * @param log the <code>Log</code>implementation to use for logging
     */
    public void setLog(Log log) {
        this.log = log;
        setLogger(log);
    }

    /**
     * Gets the dynamic configuration setting to be used for bean reading.
     *
     * @return the BindingConfiguration settings, not null
     * @since 0.5
     */
    public BindingConfiguration getBindingConfiguration() {
        if (bindingConfiguration == null) {
            bindingConfiguration = new BindingConfiguration(new EsObjectStringConverter(), false);
        }
        return bindingConfiguration;
    }

    /**
     * Sets the dynamic configuration setting to be used for bean reading.
     *
     * @param bindingConfiguration the BindingConfiguration settings, not null
     * @since 0.5
     */
    public void setBindingConfiguration(BindingConfiguration bindingConfiguration) {
        this.bindingConfiguration = bindingConfiguration;
    }

    /**
     * Gets read specific configuration details.
     *
     * @return the ReadConfiguration, not null
     * @since 0.5
     */
    public ReadConfiguration getReadConfiguration() {
        if (readConfiguration == null) {
            readConfiguration = new ReadConfiguration();
            readConfiguration.setBeanCreationChain(EsChainedBeanCreatorFactory.createChain());
        }
        return readConfiguration;
    }

    /**
     * Sets the read specific configuration details.
     *
     * @param readConfiguration not null
     * @since 0.5
     */
    public void setReadConfiguration(ReadConfiguration readConfiguration) {
        this.readConfiguration = readConfiguration;
    }

    private boolean isBeanRegister(Class beanClass) {
        for (WeakReference<Class> ref : registeredClasses) {
            if (ref.get() != null && ref.get() == beanClass) return true;
        }
        return false;
    }
    //**********************************************************************************//

    /**
     * <p>Register a bean class and add mapping rules for this bean class.</p>
     * <p/>
     * <p>A bean class is introspected when it is registered.
     * It will <strong>not</strong> be introspected again even if the introspection
     * settings are changed.
     * If re-introspection is required, then {@link #deregisterBeanClass} must be called
     * and the bean re-registered.</p>
     * <p/>
     * <p>A bean class can only be registered once.
     * If the same class is registered a second time, this registration will be ignored.
     * In order to change a registration, call {@link #deregisterBeanClass}
     * before calling this method.</p>
     * <p/>
     * <p>All the rules required to digest this bean are added when this method is called.
     * Other rules that you want to execute before these should be added before this
     * method is called.
     * Those that should be executed afterwards, should be added afterwards.</p>
     *
     * @param beanClass the <code>Class</code> to be registered
     * @throws java.beans.IntrospectionException
     *          if the bean introspection fails
     */
    public void registerBeanClass(Class beanClass) throws IntrospectionException {
        if (!isBeanRegister(beanClass)) {
            register(beanClass, null);
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Cannot add class " + beanClass.getName() + " since it already exists");
            }
        }
    }

    /**
     * Registers the given class at the given path.
     *
     * @param beanClass <code>Class</code> for binding
     * @param path      the path at which the bean class should be registered
     *                  or null if the automatic path is to be used
     * @throws IntrospectionException
     */
    private void register(Class beanClass, String path) throws IntrospectionException {
        if (log.isTraceEnabled()) {
            log.trace("Registering class " + beanClass);
        }
        XMLBeanInfo xmlInfo = getXMLIntrospector().introspect(beanClass);
        registeredClasses.add(new WeakReference<Class>(beanClass));

        ElementDescriptor elementDescriptor = xmlInfo.getElementDescriptor();

        if (path == null) {
            path = elementDescriptor.getQualifiedName();
        }

        if (log.isTraceEnabled()) {
            log.trace("Added path: " + path + ", mapped to: " + beanClass.getName());
        }
        addBeanCreateRule(path, elementDescriptor, beanClass);
    }

    /**
     * <p>Registers a bean class
     * and add mapping rules for this bean class at the given path expression.</p>
     * <p/>
     * <p/>
     * <p>A bean class is introspected when it is registered.
     * It will <strong>not</strong> be introspected again even if the introspection
     * settings are changed.
     * If re-introspection is required, then {@link #deregisterBeanClass} must be called
     * and the bean re-registered.</p>
     * <p/>
     * <p>A bean class can only be registered once.
     * If the same class is registered a second time, this registration will be ignored.
     * In order to change a registration, call {@link #deregisterBeanClass}
     * before calling this method.</p>
     * <p/>
     * <p>All the rules required to digest this bean are added when this method is called.
     * Other rules that you want to execute before these should be added before this
     * method is called.
     * Those that should be executed afterwards, should be added afterwards.</p>
     *
     * @param path      the xml path expression where the class is to registered.
     *                  This should be in digester path notation
     * @param beanClass the <code>Class</code> to be registered
     * @throws IntrospectionException if the bean introspection fails
     */
    public void registerBeanClass(String path, Class beanClass) throws IntrospectionException {
        if (!isBeanRegister(beanClass)) {
            register(beanClass, path);
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Cannot add class " + beanClass.getName() + " since it already exists");
            }
        }
    }

    /**
     * <p>Registers a class with a multi-mapping.
     * This mapping is specified by the multi-mapping document
     * contained in the given <code>InputSource</code>.
     * </p><p>
     * <strong>Note:</strong> the custom mappings will be registered with
     * the introspector. This must remain so for the reading to work correctly
     * It is recommended that use of the pre-registeration process provided
     * by {@link XMLIntrospector#register}  be considered as an alternative to this method.
     * </p>
     *
     * @param mapping <code>InputSource</code> giving the multi-mapping document specifying
     *                the mapping
     * @throws IntrospectionException
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     * @see #registerBeanClass(Class) since the general notes given there
     *      apply equally to this
     * @see XMLIntrospector#register(org.xml.sax.InputSource) for more details on the multi-mapping format
     * @since 0.7
     */
    public void registerMultiMapping(InputSource mapping) throws IntrospectionException, IOException, SAXException {
        Class[] mappedClasses = getXMLIntrospector().register(mapping);
        for (int i = 0, size = mappedClasses.length; i < size; i++) {
            Class beanClass = mappedClasses[i];
            if (!isBeanRegister(beanClass)) {
                register(beanClass, null);
            }
        }
    }

    /**
     * <p>Registers a class with a custom mapping.
     * This mapping is specified by the standard dot betwixt document
     * contained in the given <code>InputSource</code>.
     * </p><p>
     * <strong>Note:</strong> the custom mapping will be registered with
     * the introspector. This must remain so for the reading to work correctly
     * It is recommended that use of the pre-registeration process provided
     * by {@link XMLIntrospector#register}  be considered as an alternative to this method.
     * </p>
     *
     * @param mapping   <code>InputSource</code> giving the dot betwixt document specifying
     *                  the mapping
     * @param beanClass <code>Class</code> that should be register
     * @throws IntrospectionException
     * @throws SAXException
     * @throws IOException
     * @see #registerBeanClass(Class) since the general notes given there
     *      apply equally to this
     * @since 0.7
     */
    public void registerBeanClass(InputSource mapping, Class beanClass) throws IntrospectionException, IOException, SAXException {
        if (!isBeanRegister(beanClass)) {
            getXMLIntrospector().register(beanClass, mapping);
            register(beanClass, null);
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Cannot add class " + beanClass.getName() + " since it already exists");
            }
        }
    }

    /**
     * Adds a new bean create rule for the specified path
     *
     * @param path              the digester path at which this rule should be added
     * @param elementDescriptor the <code>ElementDescriptor</code> describes the expected element
     * @param beanClass         the <code>Class</code> of the bean created by this rule
     */
    protected void addBeanCreateRule(String path, ElementDescriptor elementDescriptor, Class beanClass) {
        if (log.isTraceEnabled()) {
            log.trace("Adding BeanRuleSet for " + beanClass);
        }
        RuleSet ruleSet = new BeanRuleSet(getXMLIntrospector(), path, elementDescriptor,
                beanClass, makeContext());
        addRuleSet(ruleSet);
    }

    /**
     * Override ReadContext to fix class loader reference problem.
     * Retrieving Class loader should use weak reference, so that avoid holding the classloader which may lead to
     * memory leak. If class loader is hold, it can't be  relevant static variables and classes may not be 

     */
    private class InnerReadContext extends ReadContext {
        private WeakReference<ClassLoader> classLoader;

        private InnerReadContext(Log log, BindingConfiguration bindingConfiguration, ReadConfiguration readConfiguration) {
            super(log, bindingConfiguration, readConfiguration);
        }

        public ClassLoader getClassLoader() {
            return classLoader.get();
        }

        public void setClassLoader(ClassLoader classLoader) {
            this.classLoader = new WeakReference<ClassLoader>(classLoader);
        }
    }

    /**
     * Factory method for new contexts.
     * Ensure that they are correctly configured.
     *
     * @return the ReadContext created, not null
     */
    private ReadContext makeContext() {
        return new InnerReadContext(log, getBindingConfiguration(), getReadConfiguration());
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
        try {
            return super.resolveEntity(publicId, systemId);
        } catch (SAXException e) {
            log.warn("Resolve entity fail", e);
            //When DTD/Schema can't be loaded, just give up, do not throw exception.
            return new InputSource(new StringReader(""));
        }
    }

    protected void configure() {
        super.configure();
    }

    protected void cleanup() {
        //super.cleanup();
    }
}
