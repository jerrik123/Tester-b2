package org.mangocube.corenut.commons.util;

/**
 * Method call(invocation) context ClassLoader helper class. 
 * <p/>
 *
 * @since 1.0
 */
public class ClassLoaderResolver {
    /**
     * A helper class to get the call context. It subclasses SecurityManager
     * to make getClassContext() accessible. An instance of CallerResolver
     * only needs to be created, not installed as an actual security
     * manager.
     */
    private static final class CallerResolver extends SecurityManager {
        protected Class[] getClassContext() {
            return super.getClassContext();
        }
    }

    private static CallerResolver CALLER_RESOLVER;

    static {
        try {
            //this can fail if the current SecurityManager does not allow RuntimePermission ("createSecurityManager"):
            CALLER_RESOLVER = new CallerResolver();
        }
        catch (SecurityException se) {
            throw new RuntimeException("ClassLoaderResolver: could not create CallerResolver: " + se);
        }
    }

    public static ClassLoader getClassLoader() {
        if (CALLER_RESOLVER == null) return null;

        Class[] ctx_cls = CALLER_RESOLVER.getClassContext();
        ClassLoader child_loader = null;
        for (Class cls : ctx_cls) {
            ClassLoader cur_loader = cls.getClassLoader();
            if (isChild(child_loader, cur_loader)) {
                child_loader = cur_loader;
            }
        }

        ClassLoader t_loader = Thread.currentThread().getContextClassLoader();
        if (child_loader!= null && isChild(child_loader, t_loader)) {
            child_loader = t_loader;
        }

        return child_loader;
    }

    /**
     * Returns 'true' if 'loader2' is a delegation child of 'loader1' [or if
     * 'loader1'=='loader2']. Of course, this works only for classloaders that
     * set their parent pointers correctly. 'null' is interpreted as the
     * primordial loader [i.e., everybody's parent].
     */
    private static boolean isChild(final ClassLoader loader1, ClassLoader loader2) {
        if (loader1 == loader2) return true;
        if (loader2 == null) return false;
        if (loader1 == null) return true;

        for (; loader2 != null; loader2 = loader2.getParent()) {
            if (loader2 == loader1) return true;
        }

        return false;
    }

    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = getClassLoader();
        return cl == null ? Thread.currentThread().getContextClassLoader() : cl;
    }
}
