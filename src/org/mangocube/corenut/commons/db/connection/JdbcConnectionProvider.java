/**
 * Copyright Mangocity Limited (c) 2010. All rights reserved.
 * This software is proprietary to and embodies the confidential
 * technology of Mangocity Limited.  Possession, use, or copying
 * of this software and media is authorized only pursuant to a
 * valid written license from Mangocity or an authorized sublicensor.
 */
package org.mangocube.corenut.commons.db.connection;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

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
public class JdbcConnectionProvider implements IConnectionProvider {
	private final static Set<String> registerDrivers = new HashSet<String>();

	private final String url;
	private String user;
	private String password;
	private String driverClassName;

	public JdbcConnectionProvider(String driverClassName, String url, String user, String password) {
		this.driverClassName = driverClassName;
		this.url = url;
		this.user = user;
		this.password = password;
	}

	public Connection getConnection() throws SQLException {
		registerDriver(driverClassName);

		Connection con = DriverManager.getConnection(url, user, password);

		return con;
	}
	
	static void registerDriver(String driverClassName) {
		if (!registerDrivers.contains(driverClassName)) {
			try {
				Driver driver = (Driver) Class.forName(driverClassName).newInstance();
				DriverManager.registerDriver(driver);
			} catch (Exception e) {
				throw new RuntimeException("not found driver class name:" + driverClassName, e);
			} 

			registerDrivers.add(driverClassName);
		}
	}
}