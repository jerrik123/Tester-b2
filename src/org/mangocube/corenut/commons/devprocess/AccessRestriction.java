
package org.mangocube.corenut.commons.devprocess;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The access restriction of classes or methods. In design stage, some of the classes and methods are local in
 * certain scope. To represent such design intention, we can take advantage of class or method(public, protected,
 * private, etc.) visiblity, which is one of OO encapsulation features. But this mechanism sometimes can meet our
 * requirement. For example, we want to grant method accessibility to classes that within the same package and sub
 * packages. Just declare the method as package local doesn't work, because classes in sub packages can't call it.
 * So have to declare it as public method. However, "public" is relaxed in this scenario, and doesn't define its scope
 * precisely. To help us achieve higher precision, use the AccessRestriction annotation to define access scope explicitly.
 *
 * Summer code review tool can help us the enforce the access restriction, if any violation is found, it will generate
 * warning report. For more details of code review tool, see Summer product doc.
 * @since 1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessRestriction {
    /**
     * Defines class name match pattern, classes which full name match the specific pattern are granted to refer and call
     * corresponding element (Classes, interfaces, methods, constructors and fields). Others are restrict to access.
     *
     * By default, pattern is empty, means that classes within the same package or in sub packages have access privilege.
     * @return access restriction match pattern string.
     */
    String pattern() default "";
}