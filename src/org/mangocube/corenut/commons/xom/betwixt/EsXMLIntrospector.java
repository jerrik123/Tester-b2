package org.mangocube.corenut.commons.xom.betwixt;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.IntrospectionConfiguration;
import org.apache.commons.betwixt.XMLBeanInfo;
import org.apache.commons.betwixt.digester.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.net.URL;
import java.io.StringReader;
import java.io.IOException;

class EsXMLIntrospector extends XMLIntrospector {
    /**
     * Digester used to parse the betwixt XML descriptor files
     */
    protected XMLBeanInfoDigester digester;

    public EsXMLIntrospector() {
        super();
    }

    public EsXMLIntrospector(IntrospectionConfiguration configuration) {
        super(configuration);
    }

    //****************************** MangoCube Modification ***********************************//
    private class InnerXMLBeanInfoDigester extends XMLBeanInfoDigester {
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
            //Needn't try to load betwixt DTD and validate descriptor files.
            return new InputSource(new StringReader(""));
        }

        /**
         * Reset configure for new digestion.
         */
        protected void configure() {
            if (!configured) {
                configured = true;
                // add the various rules

                addRule("info", new InfoRule());
                addRuleSet(new EsCommonRuleSet());
            }

            // now initialize
            // setAttributesForPrimitives(true);
            getProcessedPropertyNameSet().clear();
            getXMLIntrospector().getRegistry().flush();
        }
    }

    /**
     * Configures the single <code>Digester</code> instance used by this introspector.
     *
     * @param aClass <code>Class</code>, not null
     */
    protected synchronized void configureDigester(Class aClass) {
        if (digester == null) {
            digester = new InnerXMLBeanInfoDigester();
            digester.setXMLIntrospector(this);
        }
        digester.setBeanClass(aClass);
    }
    //************************************************************************************//

    /**
     * Attempt to lookup the XML descriptor for the given class using the
     * classname + ".betwixt" using the same ClassLoader used to load the class
     * or return null if it could not be loaded
     *
     * @param aClass digester .betwixt file for this class
     * @return XMLBeanInfo digested from the .betwixt file if one can be found.
     *         Otherwise null.
     */
    protected synchronized XMLBeanInfo findByXMLDescriptor(Class aClass) {
        // trim the package name
        String name = aClass.getName();
        int idx = name.lastIndexOf('.');
        if (idx >= 0) {
            name = name.substring(idx + 1);
        }
        name += ".betwixt";

        URL url = aClass.getResource(name);
        if (url != null) {
            try {
                String urlText = url.toString();
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Parsing Betwixt XML descriptor: " + urlText);
                }
                // synchronized method so this digester is only used by one thread at once
                configureDigester(aClass);
                return (XMLBeanInfo) digester.parse(urlText);
            } catch (Exception e) {
                getLog().warn("Caught exception trying to parse: " + name, e);
            }
        }

        if (getLog().isWarnEnabled()) {
            getLog().warn("Could not find betwixt file " + name + " for class [" + aClass + "], use default mapping instead!");
        }
        return null;
    }

    public synchronized XMLBeanInfo introspect(Class aClass, InputSource source) throws IOException, SAXException {
        // need to synchronize since we only use one instance and SAX is essentially one thread only
        configureDigester(aClass);
        return (XMLBeanInfo) digester.parse(source);
    }
}
