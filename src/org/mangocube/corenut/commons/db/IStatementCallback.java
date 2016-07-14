/**
 * Copyright Mangocity Limited (c) 2010. All rights reserved.
 * This software is proprietary to and embodies the confidential
 * technology of Mangocity Limited.  Possession, use, or copying
 * of this software and media is authorized only pursuant to a
 * valid written license from Mangocity or an authorized sublicensor.
 */
package org.mangocube.corenut.commons.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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
public interface IStatementCallback<T> {
	
	public T run(Statement stmt) throws SQLException;
	
	public Statement createStatement(Connection con) throws SQLException;
	
}
