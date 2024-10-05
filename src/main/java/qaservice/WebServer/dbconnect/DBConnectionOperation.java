package qaservice.WebServer.dbconnect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import qaservice.WebServer.propreader.ServerPropKey;
import qaservice.WebServer.propreader.ServerPropReader;

public class DBConnectionOperation {
	private static DBConnectionOperation dbConnetionManager_ = null;
	private Map<Connection, Boolean> dbConnectionPool_ = new HashMap<>();

	private DBConnectionOperation() {
	}
	
	public static synchronized DBConnectionOperation getInstance() {
		if(dbConnetionManager_ == null) {
			dbConnetionManager_ = new DBConnectionOperation();
		}
		return dbConnetionManager_;
	}
	
	public void createConnectionPool(String dbPath) {
		if(!enabledCreateDBConnectionFucntionCalled()) {
			return;
		}
		int conetionPoolCount = Integer.parseInt(ServerPropReader.getProperties(ServerPropKey.ConnectionPoolCount.getKey()).toString());
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
			e.printStackTrace();
		}
	}
	
	public void connectionPoolClose() {
		if(!enabledCreateDBConnectionFucntionCalled()) {
			return;
		}
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
			e.printStackTrace();
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
		System.out.println(conn);
		dbConnectionPool_.entrySet().forEach(e -> {
			System.out.println(e.getKey() + "/" + e.getValue());
		});
	}

	private static boolean enabledCreateDBConnectionFucntionCalled() {
		System.out.println(Thread.currentThread().getStackTrace()[3].getClassName());
		if("qaservice.WebServer.gui.GuiMainFrame".equals(Thread.currentThread().getStackTrace()[3].getClassName()) ||
			"qaservice.WebServer.main.WebServerStartingOperation".equals(Thread.currentThread().getStackTrace()[3].getClassName())) {
			return true;
		}
		return false;
	}
}
