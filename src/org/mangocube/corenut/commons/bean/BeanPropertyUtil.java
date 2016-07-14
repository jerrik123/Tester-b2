
package org.mangocube.corenut.commons.bean;

import org.mangocube.corenut.commons.exception.ErrorCode;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.beans.PropertyDescriptor;

/**
 * An util class help to retrieve property value of specific object. The property may be complex nested one.
 * It's thread-safe and stateless, so can be constructed as a static field/variable.
 * <p/>
 *
 * @since 1.0
 */
public class BeanPropertyUtil {
    public enum PropertyUtilError {
        /**
         * When property name is null.
         */
        @ErrorCode(comment = "Property name is null!")
        PROPERTY_NULL_NAME,

        @ErrorCode(comment = "Bean is null!")
        BEAN_IS_NULL,

        /**
         * When use combined method such as "pty1.pty2.ptyA" to retrieve nested property value, all property values
         * except the last one, must not be null. For example, if "pty2" is null, then unable to retrieve "pty1.pty2.ptyA"
         */
        @ErrorCode(comment = "Property [${1}] value is null")
        PROPERTY_NULL_VALUE,
        /**
         * Property name cannot start with "." or "]" or ")".
         */
        @ErrorCode(comment = "Property name cannot start with special character [${1}]!")
        INVALID_PROPERTY_NAME,

        /**
         * Fail to retrieve property value with Java reflection mechanism. Make sure that the bean conform JavaBean
         * naming convention and has proper getter.
         */
        @ErrorCode(comment = "Fail to retrieve property [${1}#${2}] value!")
        RETRIEVE_PROPERY_FAIL,

        /**
         * Invalid operator for specific type. [] is applicable to list or array, and () is for map. If corresponding
         * nested property type is not applicable to operator "[]"/"()", then raise exception.
         */
        @ErrorCode(comment = "Invalid operator ${1}!")
        INVALID_OPERATOR
    }

    private String getMethodName = "get";
    private BeanFieldUtil fieldUtil = new BeanFieldUtil();

    public BeanPropertyUtil() {
    }

    public BeanPropertyUtil(String getMethodName) {
        this.getMethodName = getMethodName;
    }

    /**
     * Retrieve the property value of specific object. The parameter propertyName is the key specifies
     * the identity of the value. There are many forms of the propertyName:
     * <p/>
     * Simple (name) - The specified name identifies an individual property in the workingContext.
     * <p/>
     * if the returned value is a complex object, and the user wants to get the specified nested property of the object,then
     * use the complex propertyName with ".", "[]", "()"
     * <p/>
     * Nested (name1.name2.name3)- The first name element is used to select an object from workingContext, the name2 after "."
     * is the proeprty name of the first object, and the name3 is the property of the second object.
     * The object returned for this property is then consulted, using the same approach,
     * for a property getter for a property named name2, and so on.
     * The property value that is ultimately retrieved or modified is the one identified by the last name element.
     * Indexed (name[index]) -  If the returned object is a list or an Array user can specify which element of the list should return. Just as: "ListBean[1]".
     * Mapped (name(key)) -     If the returned object is a map, then wen can get the value of the specified key. Just as: "Map[key]".
     * Combined (name1.name2[index].name3(key)) - For more complex object we can get the desired value by composite the property name together.
     * Combined (name[index](key)[index](key)) - This form of combined object property is also supported just as the previous form.
     *
     * @param bean         the object to be retrieved property value
     * @param propertyName property name
     * @return value of the property
     */
    public Object getPropertyValue(Object bean, String propertyName) {
        //if bean is null
        if (bean == null) {
            throw new BeanUtilException(PropertyUtilError.BEAN_IS_NULL);
        }

        //if propertyName is null
        if (propertyName == null) {
            throw new BeanUtilException(PropertyUtilError.PROPERTY_NULL_NAME);
        }

        //if propertyName start with the special character then raise an exception
        if (propertyName.startsWith(".") || propertyName.startsWith("[")) {
            throw new BeanUtilException(PropertyUtilError.INVALID_PROPERTY_NAME, propertyName);
        }

        //if the propertyName dost not contains the "." or "[" or "(" then return the value from the map
        if (!propertyName.contains(".") && !propertyName.contains("[")) {
            return retrievePropertyValue(bean, propertyName.trim());
        } else {
            return getNestedPropertyValue(bean, propertyName.trim());
        }
    }

    /**
     * Retrieve non-nested property value. If bean is a map, then get value by key, the proper name actually is the key.
     * If bean is POJO/JavaBean, then get value by corresponding getter. Override this method to support special
     * value retrieve logic.
     *
     * @param bean         object to be retrieved value
     * @param propertyName property name
     * @return property value
     */
    protected Object retrievePropertyValue(Object bean, String propertyName) {
        if (bean instanceof Map) {
            return ((Map) bean).get(propertyName);
        } else {
            PropertyDescriptor propertyDescriptor;
            try {
                //1. if the bean object has such an property then lookup the getter method,
                // if it exeists, then use PropertyUtils.getProperty method to retrieve its value
                propertyDescriptor = PropertyUtils.getPropertyDescriptor(bean, propertyName);
                if (propertyDescriptor != null) {
                    return PropertyUtils.getProperty(bean, propertyName);
                } else {
                    try {
                        //2. try to retrieve the property value by calling the specified method
                        //method name equals to the property name, if the method is not exesits
                        //raise the exception
                        Method method = bean.getClass().getMethod(propertyName);
                        if (method.getReturnType() != null) {
                            Object[] params = {};
                            return method.invoke(bean, params);
                        } else {
                            return null;
                        }
                    } catch (Exception ie) {
                        try {
                            //3. finally, if all previous way of retrieving the value is not working
                            //try to call the get(String key) method, if this method does not exesits
                            //then throw an exception
                            Method getMethod = bean.getClass().getMethod(getMethodName, String.class);
                            if (getMethod != null) {
                                return getMethod.invoke(bean, propertyName);
                            } else {
                                //4. try to retrieve the value from a bean field
                                return fieldUtil.retrieveBeanField(bean, propertyName);
                            }
                        } catch (Exception ge) {
                            throw new BeanUtilException(PropertyUtilError.RETRIEVE_PROPERY_FAIL, bean.getClass().getName(),
                                    propertyName, ge);
                        }
                    }
                }
            } catch (Exception e) {
                throw new BeanUtilException(PropertyUtilError.RETRIEVE_PROPERY_FAIL, bean.getClass().getName(),
                        propertyName, e);
            }
        }
    }

    protected Object getNestedPropertyValue(Object bean, String propertyName) {
        StringBuilder current_property = new StringBuilder();
        Object opr_obj = bean;
        for (int i = 0; i < propertyName.length(); i++) {
            char c = propertyName.charAt(i);
            //if meet "." checks if the opr_obj is already has value
            //if no value then get it from the cached map
            //otherwise get it using propertyUtils and beanName
            if (c == '.' || c == '[') {
                String beanName = current_property.toString().trim();
                if (!"".equals(beanName)) {
                    opr_obj = retrievePropertyValue(opr_obj, beanName);
                }
                if (opr_obj == null)
                    throw new BeanUtilException(PropertyUtilError.PROPERTY_NULL_VALUE, propertyName.substring(0, i));
                current_property.setLength(0);
            } else if (c == ']') {
                //if meet the "]" then treat the opr_obj as a list or an Array
                //and gets the value object by its index
                if (opr_obj == null)
                    throw new BeanUtilException(PropertyUtilError.PROPERTY_NULL_VALUE, propertyName.substring(0, i));
                int index = Integer.valueOf(current_property.toString());
                //The opr_obj might be a list or an Array
                if (opr_obj instanceof List) {
                    opr_obj = ((List) opr_obj).get(index);
                } else if (opr_obj.getClass().isArray()) {
                    opr_obj = ((Object[]) opr_obj)[index];
                } else {
                    throw new BeanUtilException(PropertyUtilError.INVALID_OPERATOR, propertyName.substring(0, i + 1));
                }
                current_property.setLength(0);
            } else if (!Character.isSpaceChar(c)) {
                //if the character is not the special one then append to the StringBuffer
                current_property.append(c);
            }
        }
        //at last checks if the property name is ending
        if (current_property.length() != 0 && opr_obj != null) {
            opr_obj = retrievePropertyValue(opr_obj, current_property.toString().trim());
        }

        return opr_obj;
    }
}
