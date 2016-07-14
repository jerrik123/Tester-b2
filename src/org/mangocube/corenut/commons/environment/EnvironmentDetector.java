package org.mangocube.corenut.commons.environment;

/**
 * EnvironmentDetector can be used to inspect whether the deploy environment is production or DEV. 
 *
 * @since 1.0
 */
public class EnvironmentDetector {
    private static final String RUNTIME_ENV = "ENV-TYPE";
    private static EnvironmentDetector envDetector;
    private boolean dev;

    public static synchronized EnvironmentDetector getInstance() {
        if(envDetector == null){
            envDetector = new EnvironmentDetector();
        }
        return envDetector;
    }

    private EnvironmentDetector() {
        String result = System.getProperty(RUNTIME_ENV);
        this.dev = !(result == null || !"DEV".equals(result));
    }

    public boolean isDev() {
        return dev;
    }
}
