package qaservice.WebServer.accessDBServer;

import java.sql.Connection;
import java.sql.SQLException;

import qaservice.Common.dbaccesor.UserTableAccessor;
import qaservice.Common.model.user.UserInfo;
import qaservice.WebServer.dbconnect.DBConnectionOperation;

public class UserDataLogic {
	public static UserInfo getUserInfoData(String username, String password) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return UserTableAccessor.getUserInfoData(conn, username, password);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static UserInfo getUserInfoDataByUsername(String username) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return UserTableAccessor.getUserInfoDataByUsername(conn, username);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static String getUserIntroductionData(int userId) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return UserTableAccessor.getUserIntroductionData(conn, userId);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static UserInfo getUserInfoDataByUserId(int userId) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return UserTableAccessor.getUserInfoDataByUserId(conn, userId);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static boolean existUserData(String username, String password) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return UserTableAccessor.existUserData(conn, username, password);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static int getUserIdByUserName(String username) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return UserTableAccessor.getUserIdByUserName(conn, username);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static boolean existEmailAddress(String mailAddress) throws SQLException {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return UserTableAccessor.existEmailAddress(conn, mailAddress);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static boolean registUser(String username, String password, String mailText) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return UserTableAccessor.registUser(conn, username, password, mailText);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static boolean updateUserName(String username, int userId) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return UserTableAccessor.updateUserName(conn, username, userId);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static boolean exisitUserName(String username) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return UserTableAccessor.exisitUserName(conn, username);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static boolean updateUserIntroduction(int userId, byte[] introduction) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return UserTableAccessor.updateUserIntroduction(conn, userId, introduction);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}
}
