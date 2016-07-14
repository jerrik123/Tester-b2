/**
 * Copyright Mangocity Limited (c) 2010. All rights reserved.
 * This software is proprietary to and embodies the confidential
 * technology of Mangocity Limited.  Possession, use, or copying
 * of this software and media is authorized only pursuant to a
 * valid written license from Mangocity or an authorized sublicensor.
 */
package org.mangocube.corenut.commons.db.connection;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

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
public class JNDIConnectionProvider implements IConnectionProvider {
	private final String jndiName;
	private DataSource ds;

	public JNDIConnectionProvider(String jndiName) {
		this.jndiName = jndiName;
	}

	public Connection getConnection() throws SQLException {
		if (ds == null) {
			try {
				Context txt = new InitialContext();
				ds = (DataSource) txt.lookup(jndiName);
			} catch (NamingException e) {
				throw new RuntimeException("not found jndi:" + jndiName, e);
			}
		}
		return ds.getConnection();
	}
}