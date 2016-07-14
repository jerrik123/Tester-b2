
package org.mangocube.corenut.commons.devprocess;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Code coverage metric of certain class or method. Use this annotation to denote the expected line coverage rate. We should
 * define a metric at design stage, and before each QA release, code coverage should achieve expected rate, or QA can
 * refuse to perform test. Sufficient coverage can guarantee that codes are tested adequately before released.
 *
 * Summer coverage tool will generate coverage report after running unit test case, and then perform coverage audit according
 * to metric annotations. For more details of coverage tool, see Summer product doc.
 * @since 1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface CoverageMetric {
    /**
     * Code coverage rate metric level. It's configurable, the following
     * comment is the default setting.
     */
    public enum Level {
        FULL, //100%
        STRICT,//95%
        AVERAGE, //90%
        OK //80%
    }

    Level metric();
}