/**
 * Copyright Mangocity Limited (c) 2010. All rights reserved.
 * This software is proprietary to and embodies the confidential
 * technology of Mangocity Limited.  Possession, use, or copying
 * of this software and media is authorized only pursuant to a
 * valid written license from Mangocity or an authorized sublicensor.
 */
package org.mangocube.corenut.commons.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mangocube.corenut.commons.db.connection.IConnectionProvider;
import org.mangocube.corenut.commons.db.connection.JNDIConnectionProvider;
import org.mangocube.corenut.commons.db.connection.JdbcConnectionProvider;


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
public class JdbcTemplate {
	private static Log log = LogFactory.getLog(JdbcTemplate.class);
	private final IConnectionProvider conProvider;

	public JdbcTemplate(IConnectionProvider conProvider) {
		this.conProvider = conProvider;
	}
	
	public JdbcTemplate(String jndiName) {
		this.conProvider = new JNDIConnectionProvider(jndiName);
	}

	public JdbcTemplate(String driverName, String url, String user, String password) {
		this.conProvider = new JdbcConnectionProvider(driverName, url, user, password);
	}

	public <T> T execute(IConnectionCallback<T> callback) throws SQLException {
		Connection con = null;
		try {
			con = getConnection();
			T result = callback.run(con);
			con.commit();

			return result;
		} catch (SQLException e) {
			if (con != null) {
				con.rollback();
			}

			throw e;
		} finally {
			releaseConnection(con);
		}
	}

	public <T> T execute(IStatementCallback<T> callback) throws SQLException {
		Connection con = null;
		Statement stmt = null;
		try {
			con = getConnection();
			stmt = callback.createStatement(con);

			T result = callback.run(stmt);
			con.commit();

			return result;
		} catch (SQLException e) {
			if (con != null) {
				con.rollback();
			}

			throw e;
		} finally {
			closeStatement(stmt);
			releaseConnection(con);
		}
	}

	public <T> T executeQuery(String sql, IQueryCallback<T> callback)
			throws SQLException {
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			if (callback instanceof IPreparedQueryCallback) {
				stmt = ((IPreparedQueryCallback<T>) callback).createStatement(con, sql);
				rs = ((PreparedStatement) stmt).executeQuery();
			} else {
				stmt = con.createStatement();
				rs = stmt.executeQuery(sql);
			}

			T result = callback.run(rs);
			return result;
		} catch (SQLException e) {
			throw e;
		} finally {
			closeResultSet(rs);
			closeStatement(stmt);
			releaseConnection(con);
		}
	}

	public <T> T execute(IPrepareStatementCallback<T> callback)
			throws SQLException {
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = getConnection();
			stmt = callback.createStatement(con);

			T result = callback.run(stmt);
			con.commit();

			return result;
		} catch (SQLException e) {
			if (con != null) {
				con.rollback();
			}

			throw e;
		} finally {
			closeStatement(stmt);
			releaseConnection(con);
		}
	}

	private Connection getConnection() throws SQLException {
		Connection con = conProvider.getConnection();
		if (con != null && con.getAutoCommit()) {
			con.setAutoCommit(false);
		}
		return con;
	}

	public void releaseConnection(Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				log.error("fail to release JDBC Connection", e);
			} catch (Throwable ex) {
				log.error("Unexpected exception on release JDBC Connection", ex);
			}
		}
	}

	public static void closeStatement(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				log.error("fail to close JDBC Statement", e);
			} catch (Throwable ex) {
				log.error("Unexpected exception on closing JDBC Statement", ex);
			}
		}
	}

	public static void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				log.error("fail to close JDBC ResultSet", e);
			} catch (Throwable ex) {
				log.error("Unexpected exception on closing JDBC ResultSet", ex);
			}
		}
	}

}
