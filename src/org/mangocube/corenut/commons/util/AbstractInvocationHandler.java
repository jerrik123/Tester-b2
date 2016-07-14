package org.mangocube.corenut.commons.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Abstract invocation handler of Proxy object. Do extend this class to implement
 * Proxy handler.
 * @since 1.0
 */
public abstract class AbstractInvocationHandler implements InvocationHandler {

    public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object res;

        if (method.getDeclaringClass().getName().equals(Object.class.getName())) {
            if (method.getName().equals("toString")) {
                res = getPrxoyDesc(proxy);
            } else {
                res = invokeLangObjectMethod(proxy, method, args);
            }
        } else {
            res = invokeMethod(proxy, method, args);
        }
        return res;
    }

    protected Object invokeLangObjectMethod(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(proxy, args);
    }

    protected abstract Object invokeMethod(Object proxy, Method method, Object[] args) throws Throwable;

    private String getPrxoyDesc(Object proxy) {
        StringBuilder sb = new StringBuilder();
        sb.append("Proxy");

        Class[] interfs = proxy.getClass().getInterfaces();
        sb.append("[");
        for (Class interf : interfs) {
            sb.append(interf.getName()).append(", ");
        }
        sb.replace(sb.length()-2, sb.length()-1, "]{");

        sb.append(this.getClass()).append("}");
        return sb.toString();
    }
}
