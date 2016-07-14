/**
 * Copyright Mangocity Limited (c) 2010. All rights reserved.
 * This software is proprietary to and embodies the confidential
 * technology of Mangocity Limited.  Possession, use, or copying
 * of this software and media is authorized only pursuant to a
 * valid written license from Mangocity or an authorized sublicensor.
 */
package org.mangocube.corenut.commons.db.log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;

/**
 * TODO dengtailin: Change to the actual description of this class
 * @version   Revision History
 * <pre>
 * Author     Version       Date        Changes
 * Allen      1.0           2010-12-29     Created
 *
 * </pre>
 * @since 1.0
 */
public class DataSourceWrapperFactoryBean implements FactoryBean, InvocationHandler {

	private String dataSourceId;
	private DataSource dataSource;
	private Object proxy;

	public String getDataSourceId() {
		return dataSourceId;
	}

	public void setDataSourceId(String dataSourceId) {
		this.dataSourceId = dataSourceId;
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Object getObject() throws Exception {
		if (proxy == null) {
			proxy = Proxy.newProxyInstance(DataSourceWrapperFactoryBean.class.getClassLoader(), 
						                   new Class[]{DataSource.class},
						                   this);
		}
		return proxy;
	}

	@SuppressWarnings("rawtypes")
	public Class getObjectType() {
		return DataSource.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {		
		Object result = method.invoke(dataSource, args);

		String methodName = method.getName();
        if ("getConnection".equals(methodName)) {
            if (result instanceof Connection) {
            	//log on open
    			ConnectionLogger.openConnectionLog(result, dataSourceId);
    			
    			//wrap connection
    			result = Proxy.newProxyInstance(DataSourceWrapperFactoryBean.class.getClassLoader(), 
		                   new Class[]{Connection.class},
		                   new ConnectionWrapper((Connection) result, dataSourceId));
    		}
		}
        
        return result;
	}
	
	static class ConnectionWrapper implements InvocationHandler {
		
		private Connection con;
		private String dataSourceId;

		public ConnectionWrapper(Connection con, String dataSourceId) {
			this.con = con;
			this.dataSourceId = dataSourceId;
		}

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			Object result = method.invoke(con, args);

			String methodName = method.getName();
			//log on open
	        if ("close".equals(methodName)) {
    			ConnectionLogger.closeConnectionLog(con, dataSourceId);
			} 
	        
	        return result;
		}
		
	}

}
