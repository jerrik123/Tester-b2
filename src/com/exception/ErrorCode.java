
package com.exception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotaion is used to define various properties of exception code.
 * 1. Get corresponding error message which support multi-language.
 * 2. Transaction process indicator.
 *
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ErrorCode {
	/**
	 * The exception comment. May not equals to the error message defined in DB/Resource-file
	 * It's just used to improve code readability.
     *
     * @return default error message string
     */
	String comment();

    /**
     * Only apply to the checked exception. If recoverable, the transaction will not roll back.
     * Or the transaction will roll back automatically.
     * It's default value is false, means that transaction will roll back.
     * If you want to control the transaction roll back manually, set it to true.
     * 
     * @return boolean transaction processing indicator.
     */
    boolean recoverable() default false;
}
