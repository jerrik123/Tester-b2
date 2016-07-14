
package org.mangocube.corenut.commons.bean;

import org.mangocube.corenut.commons.devprocess.CoverageMetric;
import org.mangocube.corenut.commons.io.resource.Resource;
import org.mangocube.corenut.commons.io.resource.ResourcePatternResolver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.InvalidPropertiesFormatException;

/**
 * Composite key properties util. Unlike the ordinary key-value pair, the key is composited hierarchy.
 * For example, the following entries are in property file.
 * SQL/SELECT/ORACLE = VAL1
 * SQL/UPDATE = VAL3
 * SQL/UPDATE/MSSQL = VAL4
 * SQL/UPDATE/ORACLE = VAL5
 * <code>
 * String v1 = getProperty("SQL", "SELECT", "ORACLE"); //v1 = VAL1
 * String v2 = getProperty("SQL", "SELECT"); //v2 = null
 * String v3 = getProperty("SQL", "UPDATE", "ORACLE"); //v3 = VAL5
 * String v4 = getProperty("SQL", "UPDATE", "MSSQL"); //v4 = VAL4
 * String v4 = getProperty("SQL", "UPDATE", "MYSQL"); //v4 = VAL3
 * String v5 = getProperty("SQL", "UPDATE", "DB2"); //v4 = VAL3
 * </code>
 * Keys are delimited by "/" character, just like the hierarchy file path.
 * In the above sample, notice that v4 and v5 is not null, and get value "VAL3" which is the value
 * of key "SQL/UPDATE". Because if the given composited key can't be found, then try to match the upper level key
 * iteratively. For example, "SQL/UPDATE/MYSQL" is not found, then try "SQL/UPDATE", and match, so return "VAL3".
 * Analogously, when retrieve value with key "SQL/SELECT", mismatch, then use upper level key "SQL", but still
 * mismatch, finally return null. That's why v2 is null.
 *
 * @since 1.0
 */
@CoverageMetric(metric = CoverageMetric.Level.STRICT)
public class CompositeKeyProperties extends Properties {
    private static final char DELIMITER = '/';

    //************************************** Properties file loading **********************************************//    

    public synchronized void load(String resourcePath, ClassLoader classLoader) throws IOException {
        //1. Resolve resources, find out all resource files to be loaded with given class loader.
        ResourcePatternResolver patternResolver = classLoader == null ?
                ResourcePatternResolver.getInstance() : ResourcePatternResolver.getInstance(null, classLoader);
        Resource[] resources = patternResolver.getResources(resourcePath);
        if (resources == null)
            throw new IOException("Resource " + resourcePath + " not found or unavailable!");

        //2. Load all resource files, and set property according to priority
        Map<String, Integer> pty_priority = new HashMap<String, Integer>();
        for (Resource resource : resources) {
            Properties ptys = loadPropertiesFromResource(resource);

            //set properties according to priority.
            for (Map.Entry<Object, Object> entry : ptys.entrySet()) {
                String key = entry.getKey().toString().trim();
                String val = entry.getValue().toString().trim();

                //2.1 extract priority.
                int priority = 0;
                String actual_key = key;
                if (key.charAt(key.length() - 1) == ']') {//key is end of "]"
                    int st = key.lastIndexOf('[', key.length() - 2);
                    if (st > 0) {//the last token of key is enclosed [***], indicates key priority.
                        try {
                            priority = Integer.valueOf(key.substring(st + 1, key.length() - 1));
                            actual_key = key.substring(0, st);
                        } catch (NumberFormatException e) {
                            /* To specify ovrriding priority, append snippet "[int]" such as "[1]", "[100]" to the end of property key.
                            The integer is enclosed with "[]", it could be arbitrary integer.*/
                            throw new InvalidPropertiesFormatException("Invalid priority : " + actual_key + " , should be integer!");
                        }
                    }
                }

                //2.2 set property value by priority
                if (pty_priority.containsKey(actual_key)) {//priority is set
                    int p = pty_priority.get(actual_key);
                    if (p < priority) {//priority is lower, then override value
                        this.setProperty(actual_key, val);
                        pty_priority.put(actual_key, priority);
                    }
                } else {//no priority is set yet.
                    this.setProperty(actual_key, val);
                    pty_priority.put(actual_key, priority);
                }
            }

            ptys.clear();
        }
    }

    private Properties loadPropertiesFromResource(Resource resource) throws IOException {
        String url;
        try {
            url = resource.getURL().toString();
        } catch (IOException e) {
            url = "";
        }

        Properties ptys = new Properties();
        if (url != null && url.endsWith(".xml")) {
            ptys.loadFromXML(resource.getInputStream());
        } else {
            ptys.load(resource.getInputStream());
        }
        return ptys;
    }

    //************************************************************************************************************//

    //*********************************** Composite Key matching *************************************************//

    public class MatchProperty {
        private String propertyKey;//Property key to be retrieved.
        private String propertyValue;//Property value
        private String matchedKey;//Matched key

        MatchProperty(String propertyKey, String matchedKey, String propertyValue) {
            this.matchedKey = matchedKey;
            this.propertyValue = propertyValue;
            this.propertyKey = propertyKey;
        }

        public String getMatchedKey() {
            return matchedKey;
        }

        public String getPropertyValue() {
            return propertyValue;
        }

        public String getPropertyKey() {
            return propertyKey;
        }
    }

    public MatchProperty getMatchedProperty(String... keys) {
        String key = getFullKey(keys);
        String value = getProperty(key);

        if (value != null) {
            return new MatchProperty(key, key, value);
        } else {
            String match_key = getMatchedKey(key);
            String val = getProperty(match_key);
            return val == null ? new MatchProperty(key, null, null) : new MatchProperty(key, match_key, getProperty(match_key));
        }
    }

    public String getCompositeProperty(String... keys) {
        String key = getFullKey(keys);
        String value = getProperty(key);

        return value != null ? value : getProperty(getMatchedKey(key));
    }

    private String getMatchedKey(String key) {
        String matched_key = key;
        int del_idx = matched_key.lastIndexOf(DELIMITER);
        while (del_idx > 0) {
            matched_key = matched_key.substring(0, del_idx);
            if (containsKey(matched_key)) break;
            del_idx = matched_key.lastIndexOf(DELIMITER);
        }
        return matched_key;
    }

    private String getFullKey(String... keys) {
        StringBuilder full_key = new StringBuilder(20);
        for (String k : keys) {
            full_key.append(k).append(DELIMITER);
        }
        full_key.setLength(full_key.length() - 1);
        return full_key.toString();
    }
    //*************************************************************************************************************//
}
