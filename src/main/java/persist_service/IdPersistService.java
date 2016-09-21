package persist_service;


import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

/**
 * Contains code related to update the last generated id
 * 
 * @author hitansu
 *
 */
public class IdPersistService {
	
	static String[] prefixes = { "BUG-", "STORY-", "EPIC-", "SPRINT-", "TASK-", "ISSUE-", "US-", "TICKET-" };
	
	public static void main(String[] args) throws PropertyVetoException {
		IdPersistService obj= new IdPersistService();
		Random rand = new Random();
		ConnectionManager.initialize();
		for(int i= 01;i<=1000;i++) {
			String prefix_key = prefixes[rand.nextInt(prefixes.length)];
			obj.persistId(prefix_key, i);
		}
	}
	
	public boolean persistId(String prefixKey, long id) {
		String table_name= "IDTICKET";
		String schema_name= "JENA";
		String insert_query= "INSERT INTO "+schema_name+"."+table_name+" (key, id) SELECT '"+prefixKey+"' ,"+id+" FROM DUAL WHERE NOT EXISTS (SELECT key FROM "+schema_name+"."+table_name+" WHERE key='"+prefixKey+"')";
		String update_query= "UPDATE "+schema_name+"."+table_name+" SET id="+id+" WHERE key='"+prefixKey+"'";
		
		Statement createStatement= null;
		boolean isUpdateSuccessful= false;
		Connection conn= null;
		try {
			conn= ConnectionManager.getConnectionPool().get(prefixKey);
			if(conn== null) return false;
			conn.setAutoCommit(false);
			createStatement= conn.createStatement();
			createStatement.addBatch(insert_query);
			createStatement.addBatch(update_query);
			createStatement.executeBatch();
			conn.commit();
			isUpdateSuccessful= true;
			conn.setAutoCommit(true);	
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeStSilently(createStatement);
		//	returnConnection(conn);
		}
		return isUpdateSuccessful;
	}
	
	public long getIdFromDB(String prefixKey) {
		String table_name= "IDTICKET";
		String schema_name= "JENA";
		String select_query= "SELECT id FROM "+schema_name+"."+table_name+" WHERE key='"+prefixKey+"'";
		Statement st= null;
		ResultSet rs= null;
		long id= Integer.MIN_VALUE;
		Connection conn= null;
		try {
				conn= ConnectionManager.getConnectionPool().get(prefixKey);
				st= conn.createStatement();
				rs = st.executeQuery(select_query);
				if(rs.next()) {
					id= rs.getLong(1);
				}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			closeStSilently(st);
			closeRsSilently(rs);
		//	returnConnection(conn);
		}
		return id;
	}
	
	private void closeRsSilently(ResultSet rs) {
		if(null!= rs) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void closeStSilently(Statement st) {
		if(null!= st) {
			try {
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void returnConnection(Connection conn) {
		if(null!= conn) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void closeAllConn(Connection conn) {
		if(conn!= null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
