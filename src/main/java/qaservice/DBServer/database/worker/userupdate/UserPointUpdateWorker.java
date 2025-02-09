package qaservice.DBServer.database.worker.userupdate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.Common.dbaccesor.AnswerTableAccessor;
import qaservice.Common.dbaccesor.QuestionTableAccessor;
import qaservice.Common.dbaccesor.UserTableAccessor;
import qaservice.Common.userPoint.UserPointDefinition;
import qaservice.DBServer.database.DBConnectModel;
import qaservice.DBServer.util.DBServerPropReader;

public class UserPointUpdateWorker extends Thread {
	private final Connection conn;
	private static final Object lock = new Object();
	private static Queue<Integer> queue = new ConcurrentLinkedDeque<>();
	public UserPointUpdateWorker(DBConnectModel dbConnectModel) throws SQLException {
		this.conn = connectDB(dbConnectModel);
	}

	@Override
	public void run() {
		int intervalTime = Integer.parseInt(DBServerPropReader.getProperties("updateUserPointInterval").toString());
		boolean executeFlg = true;
		while(executeFlg) {
			try {
				intervalWait(intervalTime);
				updateUserPoint();
			} catch(Throwable e) {
				QasiteLogger.warn("DB back up logic failed. Back up collect thread end.", e);
				connectionClose();
				executeFlg = false;
			}
		}
	}

	private Connection connectDB(DBConnectModel dbConnectModel) throws SQLException {
		if(conn != null) {
			return conn;
		}
		if(dbConnectModel == null) { 
			return DriverManager.getConnection("jdbc:h2:tcp://localhost:9092/./databases/datasheed.db;MODE=MYSQL;", "sa", "");
		} else {
			return DriverManager.getConnection(dbConnectModel.getConnectPath(), dbConnectModel.getName(), dbConnectModel.getPass());
		}
	}

	private void updateUserPoint() {
		if(!queue.isEmpty()) {
			spcefiedUserUpdatePoint();
		} else {
			allUserUpdatePoint();
		}
	}

	private void allUserUpdatePoint() {
		List<Integer> userList = UserTableAccessor.getAllUserIdList(conn);
		for(int userid : userList) {
			int userPointSum = 0;
			userPointSum += (QuestionTableAccessor.getQuetionPostCount(conn, userid) * UserPointDefinition.POST_QUESTION);
			userPointSum += (AnswerTableAccessor.getAnswerPostCount(conn, userid) * UserPointDefinition.POST_ANSWER);
			userPointSum += (AnswerTableAccessor.getGooActCount(conn, userid) * UserPointDefinition.GOOD_ACTION);
			userPointSum += (AnswerTableAccessor.getHelpfulCount(conn, userid) * UserPointDefinition.HELPFUL_ACTION);
			UserTableAccessor.updateUserPointSet(conn, userid, userPointSum);
		}
	}

	private void spcefiedUserUpdatePoint() {
		while(existRequestData()) {
			int userid = getRequestUpdateUserId();
			int userPointSum = 0;
			userPointSum += (QuestionTableAccessor.getQuetionPostCount(conn, userid) * UserPointDefinition.POST_QUESTION);
			userPointSum += (AnswerTableAccessor.getAnswerPostCount(conn, userid) * UserPointDefinition.POST_ANSWER);
			userPointSum += (AnswerTableAccessor.getGooActCount(conn, userid) * UserPointDefinition.GOOD_ACTION);
			userPointSum += (AnswerTableAccessor.getHelpfulCount(conn, userid) * UserPointDefinition.HELPFUL_ACTION);
			UserTableAccessor.updateUserPointSet(conn, userid, userPointSum);
		}
	}


	private void intervalWait(int intervalTime) {
		synchronized (lock) {
			try {
				lock.wait((long)intervalTime);
			} catch(InterruptedException e) {
				
			}
		}
	}

	private void connectionClose() {
		try {
			conn.close();
		} catch(Exception e) {
		}
	}

	public static void requestUserPointUpdate() {
		synchronized (lock) {
			lock.notify();
		}
	}

	public static void requestUserPointUpdate(int userid) {
		registerRequestUpdateUserId(userid);
		synchronized (lock) {
			lock.notify();
		}
	}

	private static void registerRequestUpdateUserId(int userid) {
		queue.add(userid);
	}

	private static boolean existRequestData() {
		return queue.peek() != null;
	}

	private static int getRequestUpdateUserId() {
		return queue.poll();
	}
}
