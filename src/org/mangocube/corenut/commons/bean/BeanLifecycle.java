
package org.mangocube.corenut.commons.bean;

/**
 * POJO lifecycle call back interface. When the bean is managed by certain factory/container, the
 * call back method will be invoked to trigger initialization/destroy.
 */
public interface BeanLifecycle {
    /**
	 * When bean create, this method will invoke, so could do some initial
	 * logic in this method.
	 */
	public void onCreate();

	/**
	 * When bean destroy, this method will invoke, so could release resource
	 * in this method.
	 */
	public void onDestroy();
}
