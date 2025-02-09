package qaservice.Common.userPoint;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.Common.dbaccesor.UserTableAccessor;
import qaservice.Common.prop.CommonPropReader;

public class UserPointCommnucatiReciver {
	private static Object communicateEnableNotify = new Object();
	private static class ConnectionUserTable {
		private Connection connection;
		private void setConnection(String connectionPath) {
			try {
				this.connection = DriverManager.getConnection(connectionPath, "sa", "");
			} catch(SQLException e) {
				QasiteLogger.warn("UserPointConnectionError.", e);
				this.connection = null;
			}
		}
		public synchronized void updateUserPoint(int userId, int userPoint) {
			if(connection == null) {
				QasiteLogger.warn("updateUserPoint failed. Because connection is broken");
				return;
			}
			UserTableAccessor.updateUserPoint(connection, userId, userPoint);
		}
	}
	private static class UserPointUpdateWorker extends Thread {
		private static Object lock = new Object();
		private final ConnectionUserTable connectionUserTable;
		private boolean enableHandleRequst = true;
		private int userId;
		private int addPoint;
		UserPointUpdateWorker(ConnectionUserTable connectionUserTable) {
			this.connectionUserTable = connectionUserTable;
		}
		@Override
		public void run() {
			while(true) {
				synchronized (lock) {
					enableHandleRequst = true;
					try {
						communicateEnableNotify();
						lock.wait();
					} catch(InterruptedException e) {
						QasiteLogger.info("Interrupt", true);
					}
				}
				updateUserPoint();
				enableHandleRequst = false;
			}
		}

		private void communicateEnableNotify() {
			synchronized (communicateEnableNotify) {
				communicateEnableNotify.notifyAll();
			}
		}

		public boolean enableHandleRequest() {
			return enableHandleRequst;
		}

		public void requestUpdateData(int point, int userId) {
			setUserId(userId);
			setAddPoint(point);
			lock.notifyAll();
		}

		private void setUserId(int userId) {
			this.userId = userId;
		}

		private void setAddPoint(int addPoint) {
			this.addPoint = addPoint;
		}

		private void updateUserPoint() {
			connectionUserTable.updateUserPoint(userId, addPoint);
		}
	}
	private static List<UserPointUpdateWorker> userPointUpdateWorkers = Collections.synchronizedList(new ArrayList<>());
	public static final String UPDATE_FROM_GOODPOINT = "ANSWERGOODPOINTTABLE";
	public static final String UPDATE_FROM_HELPFUL = "ANSWERHELPFULPOINTTABLE";
	public static final String UPDATE_FROM_ANSWER = "ANSWERTABLE";
	public static final String UPDATE_FROM_QUESTION = "QUESTIONTABLE";
	private static final int REQUEST_MAX_SIZE;
	static {
		REQUEST_MAX_SIZE = Integer.parseInt(CommonPropReader.getProperties("REQUEST_MAX_SIZE").toString());
		String dbConnectionPath = CommonPropReader.getProperties("DBCONNECTION_PATH").toString();
		ConnectionUserTable connectionUserTable = new ConnectionUserTable();
		connectionUserTable.setConnection(dbConnectionPath);
		createWorkerThread(connectionUserTable);
	}
	public static synchronized void recvieRequest(String tableName, int userId) {
		UserPointUpdateWorker userPointUpdateWorker = getEnableHandlerRequestWorker();
		if(UPDATE_FROM_GOODPOINT.equals(tableName)) {
			userPointUpdateWorker.requestUpdateData(UserPointDefinition.GOOD_ACTION, userId);
		}
		if(UPDATE_FROM_HELPFUL.equals(tableName)) {
			userPointUpdateWorker.requestUpdateData(UserPointDefinition.HELPFUL_ACTION, userId);
		}
		if(UPDATE_FROM_ANSWER.equals(tableName)) {
			userPointUpdateWorker.requestUpdateData(UserPointDefinition.POST_ANSWER, userId);
		}
		if(UPDATE_FROM_QUESTION.equals(tableName)) {
			userPointUpdateWorker.requestUpdateData(UserPointDefinition.POST_QUESTION, userId);
		}
	}

	private static UserPointUpdateWorker getEnableHandlerRequestWorker() {
		for(UserPointUpdateWorker userPointUpdateWorker : userPointUpdateWorkers) {
			if(userPointUpdateWorker.enableHandleRequest()) {
				return userPointUpdateWorker;
			}
		}
		synchronized(communicateEnableNotify) {
			try {
				communicateEnableNotify.wait();
			} catch(InterruptedException e) {
				QasiteLogger.info("interrupt", true);
			}
		}
		return getEnableHandlerRequestWorker();
	}

	private static synchronized void createWorkerThread(ConnectionUserTable connectionUserTable) {
		for(int i = 0; i < REQUEST_MAX_SIZE; i++) {
			userPointUpdateWorkers.add(new UserPointUpdateWorker(connectionUserTable));
		}
		for(UserPointUpdateWorker userPointUpdateWorker : userPointUpdateWorkers) {
			userPointUpdateWorker.start();
		}
	}
}
