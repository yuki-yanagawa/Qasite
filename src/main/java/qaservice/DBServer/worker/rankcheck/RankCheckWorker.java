package qaservice.DBServer.worker.rankcheck;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import qaservice.DBServer.database.rankCashe.RankCashe;
import qaservice.DBServer.util.DBServerPropReader;

public class RankCheckWorker extends Thread {
	private Connection conn_;
	public RankCheckWorker() throws SQLException {
		connectDB();
	}

	@Override
	public void run() {
		int intervalTime = Integer.parseInt(DBServerPropReader.getProperties("rankUpdateInterval").toString());
		while(true) {
			try {
				connectDB();
				RankCashe.updateRankCashe(conn_);
				intervalWait(intervalTime);
			} catch(Throwable e) {
				e.printStackTrace();
				connectionClose();
			}
		}
	}

	private void intervalWait(int intervalTime) {
		try {
			Thread.sleep(1000 * intervalTime);
		} catch(InterruptedException e) {
			
		}
	}

	private void connectDB() throws SQLException {
		if(conn_ != null) {
			return;
		}
		conn_ =  DriverManager.getConnection("jdbc:h2:tcp://localhost:9092/./databases/datasheed.db;MODE=MYSQL;", "sa", "");
	}
	private void connectionClose() {
		try {
			conn_.close();
		} catch(Exception e) {
		}
	}
}
