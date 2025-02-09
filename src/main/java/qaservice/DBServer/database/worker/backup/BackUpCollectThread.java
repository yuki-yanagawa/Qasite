package qaservice.DBServer.database.worker.backup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.DBServer.database.DBConnectModel;
import qaservice.DBServer.util.DBServerPropReader;

public class BackUpCollectThread extends Thread {
	private final Connection conn_;
	public BackUpCollectThread(DBConnectModel dbConnectModel) throws SQLException {
		conn_ = connectDB(dbConnectModel);
	}

	@Override
	public void run() {
		int intervalTime = Integer.parseInt(DBServerPropReader.getProperties("backUpInterval").toString());
		String backUpDirPath = DBServerPropReader.getProperties("bakcUpDirPath").toString();
		try {
			Thread.sleep(3000);
		} catch(InterruptedException e) {
			
		}
		boolean executeFlg = true;
		while(executeFlg) {
			try {
				executeBackUp(backUpDirPath);
				intervalWait(intervalTime);
			} catch(Throwable e) {
				QasiteLogger.warn("DB back up logic failed. Back up collect thread end.", e);
				connectionClose();
				executeFlg = false;
			}
		}
	}

	private void executeBackUp(String backUpDirPath) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddHHmmss");
		String dateStr = sdf.format(date);
		String backUpFileName = backUpDirPath + "/" + dateStr + "_backUpDB.zip";

		String sql = "backup to '" + backUpFileName + "'";
		try(PreparedStatement ps = conn_.prepareStatement(sql)) {
			ps.execute();
		} catch(SQLException e) {
			QasiteLogger.warn("execute back up failed.", e);
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
