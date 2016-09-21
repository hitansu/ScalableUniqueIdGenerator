package persist_service;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
/**
 * Creates a connection pool by creating connection for each unique key.
 * NOTE: This is just for testing
 * 
 * 
 * @author hitansu
 *
 */
public class ConnectionManager {

	static Map<String, Connection> connectionPool= new HashMap<String, Connection>();
	static String[] prefixes = { "BUG-", "STORY-", "EPIC-", "SPRINT-", "TASK-", "ISSUE-", "US-", "TICKET-" };
	
	public static void initialize() {
		for(String prefix: prefixes) {
			Connection conn= null;
			try {
				conn = getDS().getConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			connectionPool.put(prefix.trim(), conn);
		}
	
	}
	
	public static void releaseAllConnection() {

		for(String prefix: prefixes) {
			Connection conn= connectionPool.get(prefix.trim());
			returnConnection(conn);
		}
	}
	
	private static void returnConnection(Connection conn) {
		if(null!= conn) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Map<String, Connection> getConnectionPool() {
		return connectionPool;
	}
	
	private static DataSource getDS() {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
		ds.setUrl("jdbc:oracle:thin:@localhost:1521:orcl");
		ds.setUsername("jena");
		ds.setPassword("jena");

		return ds;
	}
}
