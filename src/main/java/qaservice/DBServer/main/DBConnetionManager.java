package qaservice.DBServer.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.DBServer.util.DBServerPropReader;

public class DBConnetionManager {
	private static DBConnetionManager dbConnetionManager_ = null;
	private Map<Connection, Boolean> dbConnectionPool_ = new HashMap<>();

	private DBConnetionManager() {
	}
	
	public static synchronized DBConnetionManager getInstance() {
		if(dbConnetionManager_ == null) {
			dbConnetionManager_ = new DBConnetionManager();
		}
		return dbConnetionManager_;
	}
	
	void createConnectionPool(String dbPath) {
		int conetionPoolCount = Integer.parseInt(DBServerPropReader.getProperties("connectionPoolCount").toString());
		try {
			if(dbConnectionPool_.size() > 0) {
				Iterator<Connection> concheck = dbConnectionPool_.keySet().iterator();
				while(concheck.hasNext()) {
					Connection contmp = concheck.next();
					if(contmp != null && !contmp.isClosed()) {
						contmp.close();
					}
				}
				dbConnectionPool_.clear();
			}
			for(int i = 0; i < conetionPoolCount; i++) {
				Connection con = DriverManager.getConnection(dbPath, "sa", "");
				dbConnectionPool_.put(con, false);
			}
		} catch(SQLException e) {
			QasiteLogger.warn("create db connection error", e);
		}
	}
	
	void connectionPoolClose() {
		try {
			if(dbConnectionPool_.size() > 0) {
				Iterator<Connection> concheck = dbConnectionPool_.keySet().iterator();
				while(concheck.hasNext()) {
					Connection contmp = concheck.next();
					if(contmp != null && !contmp.isClosed()) {
						contmp.close();
					}
				}
				dbConnectionPool_.clear();
			}
		} catch(SQLException e) {
			QasiteLogger.warn("connection pool close error", e);
		}
	}

	public synchronized Connection getConnetion() {
		for(Entry<Connection, Boolean> conn : dbConnectionPool_.entrySet()){
			if(conn.getValue() == false) {
				dbConnectionPool_.put(conn.getKey(), true);
				return conn.getKey();
			}
		}
		return null;
	}
	
	public synchronized void endUsedConnctionNotify(Connection conn) {
		dbConnectionPool_.put(conn, false);
	}
}
