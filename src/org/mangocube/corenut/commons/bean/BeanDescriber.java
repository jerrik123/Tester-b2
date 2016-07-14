
package org.mangocube.corenut.commons.bean;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;

import java.beans.PropertyDescriptor;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Stack;

/**
 * A util class for present the object description. A object description is a
 * string, it contains the class of this object, and all the getter properties
 * of this object description.
 * <p/>
 * <pre>
 * A object description is depend on the type of this object:
 * <p/>
 * 1. primitive type, such as int, double, show the object class and toString() directly.
 * 2. wrapper type, such as Integer, Double, BigInteger, BigDecimal, show the object class and toString() directly.
 * 3. String, StringBuffer, StringBuilder, show the object class and toString() directly.
 * 4. java.util.Date, java.uitl.Calendar, show the object class and the default format view.
 * 5. java bean, can contains the primitive type, wrapper type, util type(date,time), Array, Collection, Map or java bean.
 * Show the getter properties with indent.
 * 6. Array type, Collection, or Map, the element can contains primitive type, wrapper type, util type(date,time), Array,
 * Collection, Map or java bean.
 * </pre>
 *
 * @since 1.0
 */
public class BeanDescriber {

    private static final String NULL = "null";
    private static final String NEW_LINE = "\r\n";

    private int maxDescriptionLength = 4096;

    public BeanDescriber(int maxDescriptionLength) {
        this.maxDescriptionLength = maxDescriptionLength;
    }

    public BeanDescriber() {
    }

    private static final BeanDescriber defaultDescriber = new BeanDescriber();

    /**
     * Describe a bean with string. The string contains all the bean's
     * description and it's properties description. If it's properties also a
     * bean, then describe them too.
     * <p/>
     * <pre>
     * For example:
     * 1. A null value will be described to: null
     * 2. A primitive type will be described to: java.lang.Integer [1]
     * 3. A date type will be described to: java.util.Date [Fri Jul 11 14:29:53 CST 2008]
     * 4. A calendar type will be described to: java.util.GregorianCalendar [Fri Jul 11 14:29:53 CST 2008]
     * 5. A simple pojo will be described to:
     * org.mangocube.corenut.commons.xml.bean.test.Role <code>@14fe5c</code> {
     * 	 cacheUniqueKey():  [role1]
     * 	 name():  [role1]
     * 	 parent(): null
     * 	 roleId(): [1]
     * }
     * 6. A list will be described to:
     * java.util.ArrayList <code>@56de22ed</code> {
     * 	 [0] [1]
     * 	 [1] [1]
     * }
     * 7. A map will be described to:
     * java.util.HashMap <code>@db0915ce</code> {
     * 	  {
     * 	    KEY:  [1]
     * 	    VALUE:  [1]
     * 	  }
     * }
     * 8. An array will be described to:
     * [Ljava.lang.Integer; <code>@6b97fd</code> {
     * 	 [0]  [1]
     * 	 [1]  [1]
     * }
     * 9. All object that combined by the previous object.
     * </pre>
     *
     * @param bean the given bean.
     * @return the description of the given bean.
     */
    public static String descriptBean(Object bean) {
        return defaultDescriber.describe(bean);
    }

    public String describe(Object bean) {
        if (bean == null) return NULL;

        BeanUtilsBean bu = BeanUtilsBean.getInstance();
        PropertyUtilsBean pub = bu.getPropertyUtils();
        Stack<Object> stack = new Stack<Object>();

        return descBean(bean, pub, stack, new StringBuilder()).toString();
    }

    private StringBuilder descBean(Object bean, PropertyUtilsBean pub, Stack<Object> stack, StringBuilder beanDescription) {
        if (bean == null) {
            beanDescription.append(NULL);
            return beanDescription;
        }

        if (beanDescription.length() > maxDescriptionLength) {
            beanDescription.append(" ... ");
            return beanDescription;
        }

        for (Object ele : stack) {
            if (ele == bean) { // the bean is already defined previous.
                beanDescription.append("Refer to ").append(bean.getClass().getName()).append(" @").append(
                        Integer.toHexString(bean.hashCode()));
                return beanDescription;
            }
        }

        if (bean.getClass().isPrimitive() || bean instanceof Number || bean instanceof Boolean
                || bean instanceof Character || bean instanceof CharSequence) { // the bean is primitive type
            beanDescription.append(" [").append(bean.toString()).append("]");
        } else if (bean instanceof Date) { // the bean is date type
            beanDescription.append(" [").append(bean.toString()).append("]");
        } else if (bean instanceof Calendar) { // the bean is calendar type
            beanDescription.append(" [").append(((Calendar) bean).getTime().toString()).append("]");
        } else if (bean.getClass().isArray()) {
            stack.push(bean);
            beanDescription.append(bean.getClass().getCanonicalName()).append(" {").append(NEW_LINE);
            for (Object ele : (Object[]) bean) {
                beanDescription.append(getIndent(stack.size()));
                beanDescription = descBean(ele, pub, stack, beanDescription);
                beanDescription.append(NEW_LINE);
            }
            stack.pop();
            beanDescription.append(getIndent(stack.size())).append("}").append(NEW_LINE);
        } else if (bean instanceof Collection) { // the bean is a collection
            stack.push(bean);
            beanDescription.append(bean.getClass().getName()).append(" @")
                    .append(Integer.toHexString(bean.hashCode())).append(" {").append(NEW_LINE);
            int i = 0;
            for (Object ele : (Collection) bean) {
                beanDescription.append(getIndent(stack.size()));
                beanDescription.append("[").append(i).append("] ");
                beanDescription = descBean(ele, pub, stack, beanDescription);
                beanDescription.append(NEW_LINE);
                i++;
            }
            stack.pop();
            beanDescription.append(getIndent(stack.size())).append("}").append(NEW_LINE);
        } else if (bean instanceof Map) { // the bean is a map
            stack.push(bean);
            Map mapBean = (Map) bean;
            beanDescription.append(bean.getClass().getName()).append(" @")
                    .append(Integer.toHexString(bean.hashCode())).append(" {").append(NEW_LINE);
            for (Object key : mapBean.keySet()) {
                beanDescription.append(getIndent(stack.size())).append("{").append(NEW_LINE);
                stack.push(new Object());
                beanDescription.append(getIndent(stack.size()));
                beanDescription.append("KEY: ");
                beanDescription = descBean(key, pub, stack, beanDescription);
                beanDescription.append(NEW_LINE);
                beanDescription.append(getIndent(stack.size()));
                beanDescription.append("VALUE: ");
                beanDescription = descBean(mapBean.get(key), pub, stack, beanDescription);
                beanDescription.append(NEW_LINE);
                stack.pop();
                beanDescription.append(getIndent(stack.size())).append("}").append(NEW_LINE);
            }
            stack.pop();
            beanDescription.append(getIndent(stack.size())).append("}").append(NEW_LINE);
        } else { // other bean processing
            stack.push(bean);
            beanDescription.append(bean.getClass().getName()).append(" @")
                    .append(Integer.toHexString(bean.hashCode())).append(" { ").append(NEW_LINE);

            PropertyDescriptor[] properties = pub.getPropertyDescriptors(bean);

            try {
                for (PropertyDescriptor pd : properties) {
                    if ("class".equals(pd.getName()) || pd.getReadMethod() == null)
                        continue;
                    beanDescription.append(getIndent(stack.size()));
                    beanDescription.append(pd.getName()).append("(): ");
                    Object val = pub.getProperty(bean, pd.getName());
                    beanDescription = descBean(val, pub, stack, beanDescription);
                    beanDescription.append(NEW_LINE);
                }
            } catch (Exception e) {
                beanDescription.append("Error raise: ").append(e.getMessage()).append(" ...");
                beanDescription.append(NEW_LINE);
            }

            stack.pop();
            beanDescription.append(getIndent(stack.size())).append("}");
        }

        return beanDescription;
    }

    private static String getIndent(int level) {
        char[] indent = new char[level * 2];
        for (int i = 0; i < indent.length; i++) {
            indent[i] = ' ';
        }
        return new String(indent);
    }

}
