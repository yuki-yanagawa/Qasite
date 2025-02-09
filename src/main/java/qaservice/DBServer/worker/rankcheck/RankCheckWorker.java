package qaservice.DBServer.worker.rankcheck;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.DBServer.database.DBConnectModel;
import qaservice.DBServer.database.rankCashe.RankCashe;
import qaservice.DBServer.util.DBServerPropReader;

public class RankCheckWorker extends Thread {
	private final Connection conn_;
	//private final DBConnectModel dbConnectModel_;
	public RankCheckWorker(DBConnectModel dbConnectModel) throws SQLException {
		conn_ = connectDB(dbConnectModel);
	}

	@Override
	public void run() {
		int intervalTime = Integer.parseInt(DBServerPropReader.getProperties("rankUpdateInterval").toString());
		while(true) {
			try {
				RankCashe.updateRankCashe(conn_);
				intervalWait(intervalTime);
			} catch(Throwable e) {
				QasiteLogger.warn("Rank cashe update error.", e);
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

	private Connection connectDB(DBConnectModel dbConnectModel) throws SQLException {
		if(conn_ != null) {
			return conn_;
		}
		if(dbConnectModel == null) { 
			return DriverManager.getConnection("jdbc:h2:tcp://localhost:9092/./databases/datasheed.db;MODE=MYSQL;", "sa", "");
		} else {
			return DriverManager.getConnection(dbConnectModel.getConnectPath(), dbConnectModel.getName(), dbConnectModel.getPass());
		}
	}
	private void connectionClose() {
		try {
			conn_.close();
		} catch(Exception e) {
		}
	}
}
