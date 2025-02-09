package qaservice.Common.dbaccesor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.Common.charcterutil.CharUtil;
import qaservice.Common.model.user.UserInfo;

public class UserTableAccessor {
	public static enum UserTableColoumn {
		USERID("userId"),
		USERNAME("useName"),
		USERPASSWORD("userPassword");
		public String clientCommonName;
		private UserTableColoumn(String name) {
			clientCommonName = name;
		}
	}

	private static enum SQLTextCreator {
		UPDATE,
		INSERT;
		private String sqlText_;
		void setSqlText(String sql) {
			sqlText_ = sql;
		}
		String getSqlText() {
			return sqlText_;
		}
	}

	private static final int USERDATA_MAX_CASHE = 50;
	private static Map<String, Integer> userDataCasheByUserName_ = new HashMap<>();
	private static final String TABLENAME = "USERTABLE";
	private static final String SUBTABLENAME_INTRODUCTION = "USERINTRODUCTIONTABLE";
	private static final String SUBTABLENAME_PICTURE = "USERPICTURETABLE";

	public static UserInfo getUserInfoData(Connection conn, String username, String password) {
//		String sql = "SELECT UT.USERID, UP.PICTUREDATA, UT.USERLEVEL FROM USERTABLE AS UT LEFT OUTER JOIN USERPICTURETABLE AS UP"
//				+ " ON UT.USERID = UP.USERID"
//				+ " WHERE UT.USERNAME=? AND UT.USERPASSWORD=?";
		String sql = "SELECT UT.USERID, UP.PICTUREDATA, UT.USERPOINT FROM USERTABLE AS UT LEFT OUTER JOIN USERPICTURETABLE AS UP"
				+ " ON UT.USERID = UP.USERID"
				+ " WHERE UT.USERNAME=? AND UT.USERPASSWORD=?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, username);
			ps.setString(2, password);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				return new UserInfo(rs.getInt(1), username, rs.getBytes(2), rs.getInt(3));
			}
		} catch(SQLException e) {
			QasiteLogger.warn("user table accsess error", e);
		}
		return null;
	}

	public static UserInfo getUserInfoDataByUsername(Connection conn, String username) {
		String sql = "SELECT UT.USERID, UP.PICTUREDATA, UT.USERPOINT FROM USERTABLE AS UT LEFT OUTER JOIN USERPICTURETABLE AS UP"
				+ " ON UT.USERID = UP.USERID"
				+ " WHERE UT.USERNAME=?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				return new UserInfo(rs.getInt(1), username, rs.getBytes(2), rs.getInt(3));
			}
		} catch(SQLException e) {
			QasiteLogger.warn("user table accsess error", e);
		}
		return null;
	}

	public static UserInfo getUserInfoDataByUserId(Connection conn, int userId) {
		String sql = "SELECT UT.USERNAME, UP.PICTUREDATA, UT.USERPOINT FROM USERTABLE AS UT LEFT OUTER JOIN USERPICTURETABLE AS UP"
				+ " ON UT.USERID = UP.USERID"
				+ " WHERE UT.USERID=?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				return new UserInfo(userId, rs.getString(1), rs.getBytes(2), rs.getInt(3));
			}
		} catch(SQLException e) {
			QasiteLogger.warn("user table accsess error", e);
		}
		return null;
	}

	public static String getUserIntroductionData(Connection conn, int userId) {
		String sql = "SELECT INTRODUCTION FROM USERINTRODUCTIONTABLE WHERE USERID = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				return new String(rs.getBytes(1), CharUtil.getCharset());
			}
		} catch(SQLException e) {
			QasiteLogger.warn("user table accsess error", e);
		}
		return null;
	}

	public static byte[] getUserPictureData(Connection conn, int userId) {
		String sql = "SELECT PICTUREDATA FROM " + SUBTABLENAME_PICTURE +" WHERE USERID = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				return rs.getBytes(1);
			}
		} catch(SQLException e) {
			QasiteLogger.warn("user table accsess error", e);
		}
		return null;
	}

	public static ResultSet spGetUserPictureData(Connection conn, int userId) {
		String sql = "SELECT PICTUREDATA FROM " + SUBTABLENAME_PICTURE +" WHERE USERID = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			return ps.executeQuery();
		} catch(SQLException e) {
			QasiteLogger.warn("user picutre table accsess error", e);
		}
		return null;
	}

	public static boolean existUserData(Connection conn, String username, String password) {
		boolean result = false;
		String sql = "SELECT USERID FROM USERTABLE WHERE USERNAME=? AND USERPASSWORD=?";
		try (PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1, username);
			ps.setString(2, password);
			ResultSet rs = ps.executeQuery();
			int userId = -1;
			while(rs.next()) {
				result = true;
				userId = rs.getInt(1);
			}
			if(userId != -1) {
				userDataCasheByUserName_.put(username, userId);
			}
		} catch(SQLException e) {
			QasiteLogger.warn("user table accsess error", e);
		}
		return result;
	}
	
	/**
	 * @param username
	 * @return userId; if userId getting failed then return is -1;
	 */
	public static int getUserIdByUserName(Connection conn, String username) {
		if(userDataCasheByUserName_.containsKey(username)) {
			return userDataCasheByUserName_.get(username);
		}
		String sql = "SELECT USERID FROM USERTABLE WHERE USERNAME=?";
		try (PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			int userId = -1;
			while(rs.next()) {
				userId = rs.getInt(1);
			}
			if(userId != -1) {
				userDataCasheByUserName_.put(username, userId);
			}
			return userId;
		} catch(SQLException e) {
			QasiteLogger.warn("user table accsess error", e);
			return -1;
		}
		
	}

	public static boolean existEmailAddress(Connection conn, String mailAddress) throws SQLException {
		String sql = "SELECT USERID FROM USERTABLE WHERE MAILADDRESS=?";
		try (PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setString(1, mailAddress);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				return true;
			}
			return false;
		} catch(SQLException e) {
			throw e;
		}
	}

	public static synchronized boolean registUser(Connection conn, String username, String password, String mailText) {
		int newId = getRegistNumber(conn);
		if(newId == -1) {
			QasiteLogger.warn("new id getting Error");
			return false;
		}
		String sql = "INSERT INTO USERTABLE (USERID, USERNAME, USERPASSWORD, MAILADDRESS, USERPOINT)"
				+ " VALUES(?, ?, ?, ?, ?);";
		
		try (PreparedStatement ps = conn.prepareStatement(sql)){
			ps.setInt(1, newId);
			ps.setString(2, username);
			ps.setString(3, password);
			ps.setString(4, mailText);
			ps.setInt(5, 0);
			int result = ps.executeUpdate();
			if(result == 1) {
				return true;
			}
		} catch(SQLException e) {
			QasiteLogger.warn("user table insert error", e);
		}
		return false;
	}

	public static synchronized boolean updateUserName(Connection conn, String username, int userId) {
		String sql = "UPDATE USERTABLE SET USERNAME = ? WHERE USERID = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, username);
			ps.setInt(2, userId);
			int result = ps.executeUpdate();
			if(result == 1) {
				return true;
			}
		} catch(SQLException e) {
			QasiteLogger.warn("username update error", e);
		}
		return false;
	}

	public static boolean exisitUserName(Connection conn, String username) {
		String sql = "SELECT USERID FROM USERTABLE WHERE USERNAME = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, username);
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				return true;
			}
		} catch(SQLException e) {
			QasiteLogger.warn("username exsit check error", e);
		}
		return false;
	}

	public static boolean updateUserIntroduction(Connection conn, int userId, byte[] introduction) {
		try {
			boolean exisitanceResult = exisitanceUserData(conn, userId, SUBTABLENAME_INTRODUCTION);
			SQLTextCreator sqlText;
			if(exisitanceResult) {
				sqlText = SQLTextCreator.UPDATE;
				sqlText.setSqlText("UPDATE " + SUBTABLENAME_INTRODUCTION + " SET INTRODUCTION = ? WHERE USERID = ?");
			} else {
				sqlText = SQLTextCreator.INSERT;
				sqlText.setSqlText("INSERT INTO " + SUBTABLENAME_INTRODUCTION + " (USERID, INTRODUCTION) VALUES(?, ?)");
			}
			try(PreparedStatement ps = conn.prepareStatement(sqlText.getSqlText())) {
				if(sqlText == SQLTextCreator.UPDATE) {
					ps.setBytes(1, introduction);
					ps.setInt(2, userId);
				} else {
					ps.setInt(1, userId);
					ps.setBytes(2, introduction);
				}
				int result = ps.executeUpdate();
				if(result == 1) {
					return true;
				}
			}
		} catch(SQLException e) {
			QasiteLogger.warn("exsit check error", e);
		}
		return false;
	}

	public static boolean updateUserPictureData(Connection conn, int userId, byte[] pictureData) {
		try {
			boolean exisitanceResult = exisitanceUserData(conn, userId, SUBTABLENAME_PICTURE);
			SQLTextCreator sqlText;
			if(exisitanceResult) {
				sqlText = SQLTextCreator.UPDATE;
				sqlText.setSqlText("UPDATE " + SUBTABLENAME_PICTURE + " SET PICTUREDATA = ? WHERE USERID = ?");
			} else {
				sqlText = SQLTextCreator.INSERT;
				sqlText.setSqlText("INSERT INTO " + SUBTABLENAME_PICTURE + " (USERID, PICTUREDATA) VALUES(?, ?)");
			}
			try(PreparedStatement ps = conn.prepareStatement(sqlText.getSqlText())) {
				if(sqlText == SQLTextCreator.UPDATE) {
					ps.setBytes(1, pictureData);
					ps.setInt(2, userId);
				} else {
					ps.setInt(1, userId);
					ps.setBytes(2, pictureData);
				}
				int result = ps.executeUpdate();
				if(result == 1) {
					return true;
				}
			}
		} catch(SQLException e) {
			QasiteLogger.warn("updateUserPictureData error", e);
		}
		return false;
	}


	private static boolean exisitanceUserData(Connection conn, int userId, String tableName) throws SQLException {
		String sql = "SELECT USERID FROM " + tableName + " WHERE USERID = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				return true;
			}
		} catch(SQLException e) {
			QasiteLogger.warn("exsit check error", e);
			throw e;
		}
		return false;
	}

	private static synchronized int getRegistNumber(Connection conn) {
		return NumberingTableAccessor.getMyId(conn, TABLENAME);
	}

	public static synchronized void updateUserPoint(Connection conn, int userId, int addUserPoint) {
		String sql = "UPDATE " + TABLENAME + " SET USERPOINT=USERPOINT + ? WHERE USERID=?" ;
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, addUserPoint);
			ps.setInt(2, userId);
		} catch(SQLException e) {
			QasiteLogger.warn("update user point failed", e);
		}
	}

	public static synchronized void updateUserPointSet(Connection conn, int userId, int userpoint) {
		String sql = "UPDATE " + TABLENAME + " SET USERPOINT=? WHERE USERID=?" ;
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, userpoint);
			ps.setInt(2, userId);
			ps.executeUpdate();
		} catch(SQLException e) {
			QasiteLogger.warn("update user point failed", e);
		}
	}

	public static List<Integer> getAllUserIdList(Connection conn) {
		String sql = "SELECT USERID FROM " + TABLENAME;
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ResultSet rs = ps.executeQuery();
			List<Integer> retList = new ArrayList<>();
			while(rs.next()) {
				retList.add(rs.getInt(1));
			}
			return retList;
		} catch(SQLException e) {
			QasiteLogger.warn("get user list");
			return new ArrayList<>();
		}
	}
}
