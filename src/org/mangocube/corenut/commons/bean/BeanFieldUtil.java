
package org.mangocube.corenut.commons.bean;

import org.mangocube.corenut.commons.exception.ErrorCode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Accesses the bean fields and sets or gets the value of the properties.
 * <p/>
 *
 * @since 1.0
 */
public class BeanFieldUtil {
    private static final Log logger = LogFactory.getLog(BeanFieldUtil.class);

    public enum FieldUtilError {
        /**
         * When property name is null.
         */
        @ErrorCode(comment = "Field name is null!")
        FIELD_NAME_NULL,

        /**
         * Input bean is null
         */
        @ErrorCode(comment = "Bean is null!")
        BEAN_IS_NULL,

        /**
         * Fail to get specific field value, may be the field doesn't exist or meet other unexpected exception.
         */
        @ErrorCode(comment = "Fail to get field ${1}#${2}!")
        GET_FIELD_FAIL,

        /**
         * Fail to set specific field value, may be the field doesn't exist or meet other unexpected exception.
         */
        @ErrorCode(comment = "Fail to set field ${1}#${2}!")
        SET_FIELD_FAIL,

        /**
         * When use combined method such as "field1.field2.field3" to set nested property value, all field values
         * except the last one, must not be null. For example, if "field2" is null, then unable to set "field1.field2.field3"
         */
        @ErrorCode(comment = "Field [${1}] value is null")
        FIELD_VALUE_NULL,

        /**
         * Invalid operator for specific type. [] is applicable to list or array. If corresponding
         * nested property type is not applicable to operator "[]", then raise exception.
         */
        @ErrorCode(comment = "Invalid operator ${1}!")
        INVALID_OPERATOR,
        @ErrorCode(comment = "The field type [${1}] is both abstract and customized Collection! Cannot be initialed!")
        INVALID_COLLECTION_FIELD_TYPE,
        @ErrorCode(comment = "The field type [${1}] is both abstract and customized Map! Cannot be initialed!")
        INVALID_MAP_FIELD_TYPE
    }

    /**
     * Sets the specified bean field value to the bean object.
     *
     * @param bean      instance of the bean object
     * @param fieldName field name
     * @param value     value of the field
     */
    public void setBeanField(Object bean, String fieldName, Object value) {
        if (bean == null) {
            throw new BeanUtilException(FieldUtilError.BEAN_IS_NULL);
        }
        if (fieldName == null) {
            throw new BeanUtilException(FieldUtilError.FIELD_NAME_NULL);
        }

        if (fieldName.indexOf('.') < 0 && fieldName.indexOf('[') < 0) {
            setBeanFieldValue(bean, fieldName.trim(), value);
        } else {
            setNestedFieldValue(bean, fieldName.trim(), value);
        }
    }

    @SuppressWarnings("unchecked")
    private void setNestedFieldValue(Object bean, String fieldName, Object value) {
        Object opr_obj = bean;
        StringBuilder cur_field_name = new StringBuilder();
        for (int i = 0; i < fieldName.length(); i++) {
            char c = fieldName.charAt(i);
            if (Character.isSpaceChar(c)) continue;

            if (c == '.' || c == '[') {
                String fn = cur_field_name.toString();
                if (!"".equals(fn)) {
                    opr_obj = retrieveBeanField(opr_obj, fn);
                }
                if (opr_obj == null)
                    throw new BeanUtilException(FieldUtilError.FIELD_VALUE_NULL, fieldName.substring(0, i));
                cur_field_name.setLength(0);
            } else if (c == ']') {
                //if meet the "]" then treat the opr_obj as a list or an Array
                //and gets the value object by its index
                if (opr_obj == null)
                    throw new BeanUtilException(FieldUtilError.FIELD_VALUE_NULL, fieldName.substring(0, i));

                int index = Integer.valueOf(cur_field_name.toString());
                if (i == fieldName.length() - 1) {//The last character, then set the element
                    //The opr_obj might be a list or an Array
                    if (opr_obj instanceof List) {
                        ((List) opr_obj).set(index, value);
                    } else if (opr_obj.getClass().isArray()) {
                        ((Object[]) opr_obj)[index] = value;
                    } else {
                        throw new BeanUtilException(FieldUtilError.INVALID_OPERATOR, fieldName.substring(0, i + 1));
                    }
                } else {//not the last element, get element
                    //The opr_obj might be a list or an Array
                    if (opr_obj instanceof List) {
                        opr_obj = ((List) opr_obj).get(index);
                    } else if (opr_obj.getClass().isArray()) {
                        opr_obj = ((Object[]) opr_obj)[index];
                    } else {
                        throw new BeanUtilException(FieldUtilError.INVALID_OPERATOR, fieldName.substring(0, i + 1));
                    }
                }
                cur_field_name.setLength(0);
            } else {
                cur_field_name.append(c);
            }
        }

        if (cur_field_name.length() != 0 && opr_obj != null) {//handle the last field name snippet
            setBeanFieldValue(opr_obj, cur_field_name.toString(), value);
        }
    }

    /**
     * Gets field list from the specified class.
     * These fields are non-static or non-transient, because they are used for serialize or decompose.
     * Also the field type is Class or inner class is not in the list.
     * When the super class contains such member fields, then should gather them altogether.
     *
     * @param clazz defiend the memeber fields
     * @return all avaliable fields that used for serialization or object reference
     */
    public List<Field> getClassFields(Class clazz) {
        List<Field> fieldList = new ArrayList<Field>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (field.getType() == Class.class || field.getName().startsWith("this$")
                    || Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) continue;
            fieldList.add(field);
        }
        //Gets the valid fields from super class cursively
        Class superClazz = clazz.getSuperclass();
        if (superClazz != null && superClazz != Object.class) {
            fieldList.addAll(getClassFields(superClazz));
        }
        return fieldList;
    }

    //if the field is in the super class then should lookup from super.
    private Field getSpecField(Class clazz, String fieldName) {
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            logger.debug("Field[" + fieldName + "] cannot be found in class [" + clazz.getName() + "]");
        }
        if (field != null) return field;
        Class superClazz = clazz.getSuperclass();
        if (superClazz == null || superClazz == Object.class) return null;
        return getSpecField(superClazz, fieldName);
    }

    /**
     * Retreives the bean field by fieldId and the bean object.
     * Using the bean flection to get thespecified field.
     *
     * @param bean  bean object instance
     * @param field bean field
     * @return field object
     */
    public Object retrieveBeanField(Object bean, String field) {
        if (bean instanceof Map) {
            return ((Map) bean).get(decodeMapKey(field));
        } else {
            Field f = null;
            Object res = null;
            boolean org_acc = true;
            try {
                f = getSpecField(bean.getClass(), field);
                org_acc = f.isAccessible();
                f.setAccessible(true);
                res = f.get(bean);
            } catch (Exception e) {
                throw new BeanUtilException(FieldUtilError.GET_FIELD_FAIL, bean.getClass().getName(), field, e);
            } finally {
                if (f != null) {
                    f.setAccessible(org_acc);
                }
            }
            return res;
        }
    }

    /**
     * To support retrieving the static field's value by specifying the bean class
     * and the field name;
     *
     * @param clazz bean class
     * @param field name of the field
     * @return retrieved static field's value
     */
    public Object retrieveBeanField(Class clazz, String field) {
        Field f = null;
        Object res = null;
        boolean org_acc = true;
        try {
            f = getSpecField(clazz, field);
            org_acc = f.isAccessible();
            f.setAccessible(true);
            res = f.get(null);
        } catch (Exception e) {
            throw new BeanUtilException(FieldUtilError.GET_FIELD_FAIL, clazz.getName(), field, e);
        } finally {
            if (f != null) {
                f.setAccessible(org_acc);
            }
        }
        return res;
    }

    /**
     * Sets the bean field value according to the instance type of the bean object.
     *
     * @param bean  instance of the bean object
     * @param field name of the field
     * @param value value of the field
     */
    @SuppressWarnings("unchecked")
    protected void setBeanFieldValue(Object bean, String field, Object value) {
        if (bean instanceof Map) {
            ((Map) bean).put(decodeMapKey(field), value);
        } else {
            Object retrivedValue = value;
            Field f = null;
            boolean org_acc = true;
            try {
                f = getSpecField(bean.getClass(), field);
                org_acc = f.isAccessible();
                f.setAccessible(true);
                Class fieldType = f.getType();
                int modifier = fieldType.getModifiers();
                if (value != null) {
                    if (value instanceof Collection && Collection.class.isAssignableFrom(fieldType)) {
                        if (fieldType.isInterface() || Modifier.isAbstract(modifier)) {
                            if (List.class == fieldType || AbstractList.class == fieldType) {
                                retrivedValue = new ArrayList();
                                ((List) retrivedValue).addAll((List) value);
                            } else if (Set.class == fieldType || AbstractSet.class == fieldType) {
                                retrivedValue = new HashSet();
                                ((Set) retrivedValue).addAll((Set) value);
                            } else if (Queue.class == fieldType || AbstractQueue.class == fieldType) {
                                retrivedValue = new LinkedList();
                                for (Object o : (List) value) {
                                    ((Queue) retrivedValue).offer(o);
                                }
                            } else {
                                throw new BeanUtilException(FieldUtilError.INVALID_COLLECTION_FIELD_TYPE, fieldType);
                            }
                        } else {
                            retrivedValue = f.getType().newInstance();
                            ((Collection) retrivedValue).addAll((Collection) value);
                        }
                    } else if (value instanceof Map && Map.class.isAssignableFrom(f.getType())) {
                        if (fieldType.isInterface() || Modifier.isAbstract(modifier)) {
                            if (Map.class == fieldType || AbstractMap.class == fieldType) {
                                retrivedValue = new HashMap();
                                ((Map) retrivedValue).putAll((Map) value);
                            } else {
                                throw new BeanUtilException(FieldUtilError.INVALID_MAP_FIELD_TYPE, fieldType);
                            }
                        } else {
                            retrivedValue = f.getType().newInstance();
                            ((Map) retrivedValue).putAll((Map) value);
                        }
                    }
                }
                f.set(bean, retrivedValue);
            } catch (Exception e) {
                throw new BeanUtilException(FieldUtilError.SET_FIELD_FAIL, bean.getClass().getName(), field, e);
            } finally {
                if (f != null && f.isAccessible() != org_acc) {
                    f.setAccessible(org_acc);
                }
            }
        }
    }

    private static final String ESCAPE_DOT = "$@&";
    private static final String ESCAPE_LEFT_BRACKET = "@|&";
    private static final String ESCAPE_RIGHT_BRACKET = "&|@";

    protected String decodeMapKey(String key) {
        return key.replace(ESCAPE_DOT, ".").replace(ESCAPE_LEFT_BRACKET, "[").replace(ESCAPE_RIGHT_BRACKET, "]");
    }

    public String encodeKeyMapKey(String key) {
        StringBuilder sb = new StringBuilder(key.length());
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            switch (c) {
                case '.':
                    sb.append(ESCAPE_DOT);
                    break;
                case '[':
                    sb.append(ESCAPE_LEFT_BRACKET);
                    break;
                case ']':
                    sb.append(ESCAPE_RIGHT_BRACKET);
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
