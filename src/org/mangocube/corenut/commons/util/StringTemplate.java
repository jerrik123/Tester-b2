package org.mangocube.corenut.commons.util;

import org.mangocube.corenut.commons.bean.BeanPropertyUtil;
import org.mangocube.corenut.commons.exception.ErrorCode;
import org.mangocube.corenut.commons.exception.UncheckedException;
import org.mangocube.corenut.commons.io.resource.ResourcePatternResolver;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * This class use replace the parameter define in the template string.
 * the templete string can use the "${}" define parameter replace .
 * <p>Some examples:<br>
 * if the template define the parameter use ${1},the paraemter which index value is 1 will replace the ${1};
 * if the temolate define the parameter use ${exp},the exp support the Jexl.and the parameters will be map,and
 * the ${exp} will be replace by the exp evla value
 * if the template define teh parameter use ${propertyeName}.the propertyName will be property in a javaBean,will find
 * the property value in the parameter and replace the ${propertyName}
 * @since 1.0
 */
public class StringTemplate {

    public enum StringTempletError {
        @ErrorCode(comment = "Expression format is error, please check it!")
        INVALID_EXPRESSION_FORMAT,
        @ErrorCode(comment = "The index [${1}] extract from expression is out of the length of parameter!")
        INDEX_OUT_OF_LENGHT,
        @ErrorCode(comment = "The parameters is null value")
        NOT_GET_PARAMETER,
        @ErrorCode(comment = "Fail to evaluate expression : [${1}]")
        EVALUATE_FAIL,
        @ErrorCode(comment = "Fail read resource from [${1}]")
        CAN_ONT_READ_FROM_RESOURCE
    }

    private static final Log logger = LogFactory.getLog(StringTemplate.class);
    private static final char PREFIX = '{';
    private static final char POSTFIX = '}';
    private static final char SEPARATOR = '$';
    private static final BeanPropertyUtil beanUtil = new BeanPropertyUtil();
    private static final ResourcePatternResolver resourceLoader = ResourcePatternResolver.getInstance();

    String template;

    private StringTemplate(String resource) {
        this.template = getTemplateFromResource(resource);
    }

    /**
     * parase the template
     *
     * @param template   the string template for parase,the define paremater in the template use the index.${1}
     * @param parameters the pareamter will be use replace the template, and will replace in the template by index
     * @return the result string paraser.
     */
    public static String generate(String template, Object... parameters) {
        return parserTemplate(template, parameters);
    }

    /**
     * parase the template
     *
     * @param template   the string template for parase,the define paremater in the template use the index:${1}
     * @param parameters the pareamter will be use replace the template, and will replace in the template by index
     * @return the result string paraser.
     */
    public static String generate(String template, List parameters) {
        return parserTemplate(template, parameters);
    }

    /**
     * parase the template
     *
     * @param template   the string template for parase,the define paremater in the template use the ex:${exp}.
     *                   the exp support the jexl
     * @param parameters the pareamter will be use replace the template,
     *                   and will replace in the template by result eval exp
     * @return the result string paraser.
     */
    public static String generate(String template, Map parameters) {
        return parserTemplate(template, parameters);
    }

    /**
     * parase the template
     *
     * @param template  template  the string template for parase,the define paremater in the template use the property.
     *                  use the property value of javabean
     * @param paramster the javabean,which property value will be replace the template
     * @return the result string paraser
     */
    public static String generate(String template, Object paramster) {
        String ret = "";
        if (paramster instanceof Map) {
            ret = generate(template, (Map) paramster);
        } else if (paramster instanceof List) {
            ret = generate(template, (List) paramster);
        } else {
            ret = parserTemplate(template, paramster);
        }
        return ret;
    }

    /**
     * parase the template from resource
     *
     * @param resource the path of resource
     * @return the  string templet
     */
    public static StringTemplate generateStringTempletFromResource(String resource) {
        return new StringTemplate(resource);
    }

    /**
     * parase the template the string template for parase,the define paremater in the template use the index.${1}
     *
     * @param parameters the pareamter will be use replace the template, and will replace in the template by index
     * @return the result string paraser.
     */
    public String generate(Object... parameters) {
        return parserTemplate(this.template, parameters);
    }

    /**
     * parase the template
     * the string template for parase,the define paremater in the template use the index:${1}
     *
     * @param parameters the pareamter will be use replace the template, and will replace in the template by index
     * @return the result string paraser.
     */
    public String generate(List parameters) {
        return parserTemplate(this.template, parameters);
    }

    /**
     * parase the template
     * the string template for parase,the define paremater in the template use the ex:${exp}.
     * the exp support the jexl
     *
     * @param parameters the pareamter will be use replace the template,
     *                   and will replace in the template by result eval exp
     * @return the result string paraser.
     */
    public String generate(Map parameters) {
        return parserTemplate(this.template, parameters);
    }

    /**
     * parase the template
     * template  the string template for parase,the define paremater in the template use the property.
     * use the property value of javabean
     *
     * @param paramster the javabean,which property value will be replace the template
     * @return the result string paraser
     */
    public String generate(Object paramster) {
        String ret = "";
        if (paramster instanceof Map) {
            ret = this.generate((Map) paramster);
        } else if (paramster instanceof List) {
            ret = this.generate((List) paramster);
        } else {
            ret = parserTemplate(this.template, paramster);
        }
        return ret;
    }

    private static String getTemplateFromResource(String resource) {
        StringBuilder ret = new StringBuilder();        
        BufferedReader reader = null;
        try {
           InputStream template  = resourceLoader.getResource(resource).getInputStream();
           InputStreamReader streamReader = new InputStreamReader(template);
            reader = new BufferedReader(streamReader);
            String line;
            while ((line = reader.readLine()) != null) {
                ret.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new UncheckedException(StringTempletError.CAN_ONT_READ_FROM_RESOURCE, e, resource);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.warn("close the file error", e);
                }
            }
        }
        return ret.toString();
    }

    private static String parserTemplate(String template, Object parameters) {
        StringBuilder builder = new StringBuilder();
        if (parameters == null) {
            return template;
        }
        for (int i = 0; i < template.length(); i++) {
            if (SEPARATOR == template.charAt(i) && PREFIX == template.charAt(i + 1)) {
                int start = i + 1;
                int end = template.indexOf(POSTFIX, start);
                if (end == -1) throw new UncheckedException(StringTempletError.INVALID_EXPRESSION_FORMAT);
                String expression = template.substring(start + 1, end).trim();

                String result = null;
                if (parameters instanceof Map) {
                    Map parameter = (Map) parameters;
                    result = evalParameterForMap(expression, parameter);
                } else if (parameters instanceof Object[]) {
                    Object[] parameter = (Object[]) parameters;
                    result = evalParameterForVariational(expression, parameter);
                } else if (parameters instanceof List) {
                    List parameter = (List) parameters;
                    result = evalParameterForList(expression, parameter);
                } else {
                    result = evalParmaterForBean(expression, parameters);
                }
                builder.append(result != null ? result : "");
                i = end;
            } else {
                builder.append(template.charAt(i));
            }
        }
        return builder.toString();
    }

    private static String evalParameterForMap(String exp, Map parameters) {
        try {
            Expression expression = ExpressionFactory.createExpression(exp);
            JexlContext evaluateContext = JexlHelper.createContext();
            addParameters(evaluateContext, parameters);
            Object ret = expression.evaluate(evaluateContext);
            return ret == null ? "" : ret.toString();
        } catch (Exception e) {
            throw new UncheckedException(StringTempletError.EVALUATE_FAIL, e, exp);
        }
    }

    @SuppressWarnings("unchecked")
    private static void addParameters(JexlContext evaluateContext, Map parameters) {
        for (Object e : parameters.entrySet()) {
            Map.Entry entry = (Map.Entry) e;
            evaluateContext.getVars().put(entry.getKey(), entry.getValue());
        }
    }

    private static String evalParameterForVariational(String exp, Object... parameters) {
        int index = Integer.valueOf(exp) - 1;
        if (parameters != null && index < parameters.length && index >= 0) {
            return parameters[index].toString();
        }
        return exp;
    }

    private static String evalParameterForList(String exp, List parameters) {
        int index = Integer.parseInt(exp) - 1; 
        if (parameters != null && index < parameters.size() && index >= 0) { 
            return parameters.get(index).toString();
        }
        return exp;
    }

    private static String evalParmaterForBean(String exp, Object bean) {
        return beanUtil.getPropertyValue(bean, exp).toString();
    }
}
