package qaservice.Common.dbaccesor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import datasheet.hellostatistics.dbconnect.DBConnectionOperation;
import datasheet.hellostatistics.logger.ServerLogger;
import qaservice.Common.charcterutil.CharUtil;
import qaservice.Common.dbaccesor.cashe.TimestampWrapCahse;
import qaservice.Common.model.user.UserInfo;

public class AnswerTableAccessor {
	private static Map<Integer, Map<String, TimestampWrapCahse<Integer>>> poinActCasheByAnswerId_;
	private static Map<Integer, List<Integer>> goodPointActUserIdCasheByAnswerId_;
	private static Map<Integer, List<Integer>> helpfulPoinActUserIdtCasheByAnswerId_;
	private static final String POINTACTCASHE_GOODPOINT_KEY = "keyGoodPoint";
	private static final String POINTACTCASHE_HELPFULPOINT_KEY = "keyhelpfulPoint";
	private static final int FRAME_CASHE_SIZE_MAX = 20;
	private static final int INNER_CASHE_SIZE_MAX = 20;
	private static final String TABLENAME = "ANSWERTABLE";
	private static final String SUBTABLENAME_IMAGE = "ANSWERSUBTABLEIMAGE";
	private static final String SUBTABLENAME_LINKFILE = "ANSWERSUBTABLELINKFILE";

	static {
		//initializeCasheData();
	}

	public static enum AnswerTableColoumn {
		ANSWERID("answerId"),
		QUESTIONID("questionId"),
		ANSWER_DETAIL_DATA("answerDetailData"),
		USERID("uesrId"),
		UPDATE_DATE("answerUpdateDate");
		public String clientCommonName;
		private AnswerTableColoumn(String name) {
			clientCommonName = name;
		}
	}

//	private static void initializeCasheData() {
//		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
//		Connection conn = dbConnOpe.getConnetion();
//		String actionSql = "SELECT A.ANSWERID,"
//				+ " SUM(CASE WHEN AG.GOOD_ACTION_USER_ID = 0 THEN 0 ELSE 1 END) AS GOOD_ACT_SUM,"
//				+ " SUM(CASE WHEN AH.HELPFUL_ACTION_USER_ID = 0 THEN 0 ELSE 1 END) AS HELPFUL_ACT_SUM"
//				+ " FROM ANSWERTABLE AS A"
//				+ " LEFT OUTER JOIN ANSWERGOODPOINTTABLE AS AG ON A.ANSWERID = AG.ANSWERID AND AG.GOOD_ACTION = true"
//				+ " LEFT OUTER JOIN ANSWERHELPFULPOINTTABLE AS AH ON A.ANSWERID = AH.ANSWERID AND AH.GOOD_ACTION = true"
//				+ " GROUP BY A.ANSWERID";
//		try (PreparedStatement ps = conn.prepareStatement(actionSql)) {
//			ResultSet rs = ps.executeQuery();
//			while(rs.next()) {
//				int answerId = rs.getInt(1);
//				if(!poinActCasheByAnswerId_.containsKey(answerId)) {
//					Map<String, TimestampWrapCahse<Integer>> pointMap = new HashMap<>();
//					//poinActCasheByAnswerId_.put(key, value)
//				}
//				//poinActCasheByAnswerId_.put(rs.ge, value)
//			}
//		} catch(SQLException e) {
//			
//		} finally {
//			dbConnOpe.endUsedConnctionNotify(conn);
//		}
//		
//	}

	public static Map<Integer, Map<String, Object>> getAnswerIdAndLatestUpdateDateGroupingQuestionId(Connection conn) {
		if(conn == null) {
			ServerLogger.getInstance().warn("AnswerTable access getAnswerIdAndUpdateDateGroupingQuestionId DB connection Error");
			return new HashMap<>();
		}
//		String sql = "SELECT A.QUESTIONID, A.ANSWERID, A.UPDATE_DATE FROM ANSWERTABLE AS A" 
//				+ " JOIN (SELECT QUESTIONID, MAX(UPDATE_DATE) AS MAX_UPDATE FROM ANSWERTABLE WHERE ANSWERVALID = true GROUP BY QUESTIONID) AS J"
//				+ " ON A.QUESTIONID = J.QUESTIONID AND A.UPDATE_DATE = J.MAX_UPDATE WHERE ANSWERVALID = true";
		String sql = "SELECT QUESTIONID, COUNT(ANSWERID), MAX(UPDATE_DATE) FROM ANSWERTABLE WHERE ANSWERVALID = true GROUP BY QUESTIONID";
		Map<Integer, Map<String, Object>> retMap = new HashMap<>();
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				Map<String, Object> innerMap = new HashMap<>();
				innerMap.put("answerCount", rs.getInt(2));
				innerMap.put("latestTime", rs.getTimestamp(3));
				retMap.put(rs.getInt(1), innerMap);
			}
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "AnswerTable access getAnswerIdAndUpdateDateGroupingQuestionId Error");
			return new HashMap<>();
		}
		return retMap;
	}

	public static Map<Integer, Map<Integer, Timestamp>> getAnswerIdAndUpdateDateByQuestionId(Connection conn, int[] answerId) {
		if(conn == null) {
			ServerLogger.getInstance().warn("AnswerTable access getAnswerCountByQuestionId DB connection Error");
			return new HashMap<>();
		}
		String sql = "SELECT DISTINCT QUESTIONID, ANSWERID, UPDATE_DATE FROM ANSWERTABLE WHERE QUESTIONID in (" + createSeparateLinerCharcter('?', answerId.length) +") ANSWERVALID = true";
		Map<Integer, Map<Integer, Timestamp>> retMap = new HashMap<>();
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				Map<Integer, Timestamp> innerMap = new HashMap<>();
				innerMap.put(rs.getInt(2), rs.getTimestamp(3));
				retMap.put(rs.getInt(1), innerMap);
			}
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "AnswerTable access getAnswerCountByQuestionId Error");
			return new HashMap<>();
		}
		return retMap;
	}

	public static int insertAnswer(Connection conn, byte[] answerData, int questionId, UserInfo userInfo) {
		int newId = getRegistNumber(conn);
		if(newId == -1) {
			ServerLogger.getInstance().warn("new id getting Error");
			return -1;
		}
		
		int userId = userInfo.getUserId();
		if(userId == -1) {
			ServerLogger.getInstance().warn("user id getting Error");
			return -1;
		}

		String sql = "INSERT INTO ANSWERTABLE (ANSWERID, QUESTIONID, ANSWER_DETAIL_DATA, USERID, ANSWERVALID, UPDATE_DATE)"
				+ " VALUES(?, ?, ?, ?, ?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			int parameterIndex = 1;
			ps.setInt(parameterIndex++, newId);
			ps.setInt(parameterIndex++, questionId);
			ps.setBytes(parameterIndex++, answerData);
			ps.setInt(parameterIndex++, userId);
			ps.setBoolean(parameterIndex++, false);
			ps.setTimestamp(parameterIndex++, new Timestamp(System.currentTimeMillis()));
			int result = ps.executeUpdate();
			if(result != 1) {
				ServerLogger.getInstance().warn("Insert Error");
				return -1;
			}
			return newId;
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "Insert Error");
			return -1;
		}
	}

	public static List<Map<String, Object>> getAnswerDetailData(Connection conn, int questionId, UserInfo userInfo) {
		
		Map<Integer, Map<String, List<Integer>>> actionMap = new HashMap<>(); 
		//Key=AnswerId, Value=(InnerKey1=goodPoint, InnerValue=goodActUserId InnerKey2=helpfulPoint, InnerValue=helpfulUserId
		String actionSql = "SELECT A.ANSWERID, AG.GOOD_ACTION_USER_ID, AH.HELPFUL_ACTION_USER_ID FROM ANSWERTABLE AS A"
				+ " LEFT OUTER JOIN ANSWERGOODPOINTTABLE AS AG ON A.ANSWERID = AG.ANSWERID AND AG.GOOD_ACTION = true"
				+ " LEFT OUTER JOIN ANSWERHELPFULPOINTTABLE AS AH ON A.ANSWERID = AH.ANSWERID AND AH.HELPFUL_ACTION = true"
				+ " WHERE QUESTIONID = ? AND ANSWERVALID = ? ORDER BY A.ANSWERID";
		try(PreparedStatement ps = conn.prepareStatement(actionSql)) {
			ps.setInt(1, questionId);
			ps.setBoolean(2, true);
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()) {
				int answerId = rs.getInt(1);
				if(!actionMap.containsKey(answerId)) {
					Map<String, List<Integer>> actionInnerMap = new HashMap<>();
					List<Integer> goodActionUserIdList = new ArrayList<>();
					List<Integer> helpfulActionUserList = new ArrayList<>();
					actionInnerMap.put("goodPoint", goodActionUserIdList);
					actionInnerMap.put("helpfulPoint", helpfulActionUserList);
					actionMap.put(answerId, actionInnerMap);
				}
				Map<String, List<Integer>> actionInnerMap = actionMap.get(answerId);
				if(rs.getInt(2) != 0) {
					actionInnerMap.get("goodPoint").add(rs.getInt(2));
				}
				if(rs.getInt(3) != 0) {
					actionInnerMap.get("helpfulPoint").add(rs.getInt(3));
				}
			}
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "getQuestionDetailData error. data base connection");
			return new ArrayList<>();
		} 

		List<Map<String, Object>> retList = new ArrayList<>();
		String sql = "SELECT A.ANSWERID, A.ANSWER_DETAIL_DATA, USR.USERNAME ,A.UPDATE_DATE ,USR.USERID FROM ANSWERTABLE AS A"
				+ " JOIN USERTABLE AS USR ON USR.USERID = A.USERID"
				+ " WHERE QUESTIONID = ? AND ANSWERVALID = ? ORDER BY A.ANSWERID DESC";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, questionId);
			ps.setBoolean(2, true);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				Map<String, Object> innerMap = new HashMap<>();
				int answerId = rs.getInt(1);
				innerMap.put(AnswerTableColoumn.ANSWERID.clientCommonName, answerId);
				innerMap.put(AnswerTableColoumn.ANSWER_DETAIL_DATA.clientCommonName,
						new String(rs.getBytes(2), CharUtil.getCharset()));
				innerMap.put(UserTableAccessor.UserTableColoumn.USERNAME.clientCommonName, rs.getString(3));
				innerMap.put(AnswerTableColoumn.UPDATE_DATE.clientCommonName, rs.getString(4).split("\\.")[0]);
				
				int userId = rs.getInt(5);
				innerMap.put("answeredUserId", userId);
				Map<String, List<Integer>> actionInnerMap = actionMap.get(answerId);
				List<Integer> goodActionUserIdList = actionInnerMap.get("goodPoint");
				boolean goodActionResult = false;
				if(userInfo != null && goodActionUserIdList.contains(userInfo.getUserId())) {
					goodActionResult = true;
				}
				innerMap.put("goodPointCount", goodActionUserIdList.size());
				innerMap.put("goodPointAction", goodActionResult);
				
				List<Integer> helpfulActionUserList = actionInnerMap.get("helpfulPoint");
				boolean helpfulActionResult = false;
				if(userInfo != null && helpfulActionUserList.contains(userInfo.getUserId())) {
					helpfulActionResult = true;
				}
				innerMap.put("helpfulPointCount", helpfulActionUserList.size());
				innerMap.put("helpfulPointAction", helpfulActionResult);
				retList.add(innerMap);
			}
			return retList;
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "getQuestionDetailData error. data base connection");
			return new ArrayList<>();
		}
	}

	public synchronized static int goodAction(int answerId, UserInfo userInfo, int actionResult) {

		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		String sql = "";
		if(actionResult == -1) {
			sql = "DELETE FROM ANSWERGOODPOINTTABLE WHERE ANSWERID = ? AND GOOD_ACTION_USER_ID = ?";
		} else {
			sql = "INSERT INTO ANSWERGOODPOINTTABLE (ANSWERID, GOOD_ACTION_USER_ID, GOOD_ACTION)"
					+ "VALUES(?, ?, ?)";
		}

		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			int setIndex = 1;
			ps.setInt(setIndex++, answerId);
			ps.setInt(setIndex++, userInfo.getUserId());
			if(actionResult != -1) {
				ps.setBoolean(setIndex++, true);
			}
			ps.executeUpdate();
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "good action regist error");
			return -1;
		}

		sql = "SELECT COUNT(ANSWERID) AS ANSWER_COUNT FROM ANSWERGOODPOINTTABLE WHERE ANSWERID = ? AND GOOD_ACTION = true";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, answerId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return -1;
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "good action regist error");
			return -1;
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static synchronized int helpfulAction(Connection conn, int answerId, String username, int actionResult) {
		int userId = UserTableAccessor.getUserIdByUserName(conn, username);
		if(userId == -1) {
			ServerLogger.getInstance().warn("good action userid getting error");
			return -1;
		}

		String sql = "";
		if(actionResult == -1) {
			sql = "DELETE FROM ANSWERHELPFULPOINTTABLE WHERE ANSWERID = ? AND HELPFUL_ACTION_USER_ID = ?";
		} else {
			sql = "INSERT INTO ANSWERHELPFULPOINTTABLE (ANSWERID, HELPFUL_ACTION_USER_ID, HELPFUL_ACTION)"
					+ "VALUES(?, ?, ?)";
		}

		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			int setIndex = 1;
			ps.setInt(setIndex++, answerId);
			ps.setInt(setIndex++, userId);
			if(actionResult != -1) {
				ps.setBoolean(setIndex++, true);
			}
			ps.executeUpdate();
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "good action regist error");
			return -1;
		}

		sql = "SELECT COUNT(ANSWERID) AS ANSWER_COUNT FROM ANSWERHELPFULPOINTTABLE WHERE ANSWERID = ? AND HELPFUL_ACTION = true";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, answerId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
			return -1;
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "good action regist error");
			return -1;
		}
	}

	public static synchronized int goodActionUsingCashe(Connection conn, int answerId, String username, int actionResult) {
		int userId = UserTableAccessor.getUserIdByUserName(conn, username);
		if(poinActCasheByAnswerId_.containsKey(answerId)) {
			List<Integer> tmpGoodPointList = goodPointActUserIdCasheByAnswerId_.get(answerId);
			if(actionResult > 0) {
				if(tmpGoodPointList.size() < INNER_CASHE_SIZE_MAX) {
					tmpGoodPointList.add(userId);
					return poinActCasheByAnswerId_.get(answerId).get(POINTACTCASHE_GOODPOINT_KEY).getWrapObj() 
							+ tmpGoodPointList.size();
				}
				try {
					return goodActionRegistFromCashe(conn, answerId, userId);
				} catch(SQLException e) {
					ServerLogger.getInstance().warn(e, "good action regist error");
					return -1;
				}
			} else {
				if(tmpGoodPointList.contains(userId)) {
					tmpGoodPointList.remove(userId);
					return poinActCasheByAnswerId_.get(answerId).get(POINTACTCASHE_GOODPOINT_KEY).getWrapObj() 
							+ tmpGoodPointList.size();
				}
				int result = notGoodActionRefrectDB(conn, answerId, userId);
				if(result == -1) {
					return result;
				}
				return result + goodPointActUserIdCasheByAnswerId_.size();
			}
		} else {
			if(poinActCasheByAnswerId_.size() < FRAME_CASHE_SIZE_MAX) {
				//poinActCasheByAnswerId_.put
			}
			return -1;
		}
	}

	private static int notGoodActionRefrectDB(Connection conn, int answerId, int userId) {
		String sql = "DELETE FROM ANSWERGOODPOINTTABLE WHERE ANSWERID = ? AND GOOD_ACTION_USER_ID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			int setIndex = 1;
			ps.setInt(setIndex++, answerId);
			ps.setInt(setIndex++, userId);
			ps.executeUpdate();
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "good action delete error");
			return -1;
		}

		Map<String, TimestampWrapCahse<Integer>> innerMap = poinActCasheByAnswerId_.get(answerId);
		TimestampWrapCahse<Integer> countWrap = innerMap.get(POINTACTCASHE_GOODPOINT_KEY);
		int newInt = countWrap.getWrapObj() - 1;
		innerMap.put(POINTACTCASHE_GOODPOINT_KEY, 
				new TimestampWrapCahse<Integer>(newInt, new Timestamp(System.currentTimeMillis())));
		return newInt;
	}

	private static int goodActionRegistFromCashe(Connection conn, int answerId, int userId) throws SQLException{
		conn.setAutoCommit(false);
		String sql = "INSERT INTO ANSWERGOODPOINTTABLE (ANSWERID, GOOD_ACTION_USER_ID, GOOD_ACTION)"
				+ "VALUES(?, ?, ?)";
		for(int cahsedUserId : goodPointActUserIdCasheByAnswerId_.get(answerId)) {
			try(PreparedStatement ps = conn.prepareStatement(sql)) {
				int setIndex = 1;
				ps.setInt(setIndex++, answerId);
				ps.setInt(setIndex++, cahsedUserId);
				ps.setBoolean(setIndex++, true);
				ps.executeUpdate();
			} catch(SQLException e) {
				ServerLogger.getInstance().warn(e, "good action regist error");
				conn.rollback();
				return -1;
			}
		}
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			int setIndex = 1;
			ps.setInt(setIndex++, answerId);
			ps.setInt(setIndex++, userId);
			ps.setBoolean(setIndex++, true);
			ps.executeUpdate();
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "good action regist error");
			conn.rollback();
			return -1;
		} finally {
			conn.commit();
		}
		Map<String, TimestampWrapCahse<Integer>> innerMap = poinActCasheByAnswerId_.get(answerId);
		TimestampWrapCahse<Integer> countWrap = innerMap.get(POINTACTCASHE_GOODPOINT_KEY);
		int newCount = countWrap.getWrapObj() + goodPointActUserIdCasheByAnswerId_.size() + 1;
		goodPointActUserIdCasheByAnswerId_.clear();
		innerMap.put(POINTACTCASHE_GOODPOINT_KEY,
				new TimestampWrapCahse<Integer>(newCount, new Timestamp(System.currentTimeMillis())));
		poinActCasheByAnswerId_.put(answerId, innerMap);
		return newCount;
	}

	public static boolean insertQuestionImgData(Connection conn, byte[] imgData, int answerId) {
		String sql = "INSERT INTO " + SUBTABLENAME_IMAGE + " (ANSWERID, ANSWER_IMAGEDATA) VALUES(?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, answerId);
			ps.setBytes(2, imgData);
			int result = ps.executeUpdate();
			if(result == 1) {
				return true;
			}
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "AnswerTable Insert ImgData Error.");
		}
		return false;
	}

	public static boolean isPostAnswerUser(Connection conn, int answerId, int userId) {
		String sql = "SELECT ANSWERID FROM ANSWERTABLE WHERE ANSWERID = ? AND USERID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, answerId);
			ps.setInt(2, userId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				return true;
			}
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "AnswerTable User Check Error.");
		}
		return false;
	}

	public static boolean revertPostAnswer(Connection conn, int answerId) {
		String sql = "DELETE FROM ANSWERTABLE WHERE ANSWERID = ? AND ANSWERVALID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, answerId);
			ps.setBoolean(2, false);
			int result = ps.executeUpdate();
			if(result != 1) {
				return false;
			}
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "AnswerTable revert Error.");
			return false;
		}

		sql = "DELETE FROM " + SUBTABLENAME_IMAGE + " WHERE ANSWERID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, answerId);
			int result = ps.executeUpdate();
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "ANSWERSUBTABLEIMAGE revert Error.");
			return false;
		}

		sql = "DELETE FROM " + SUBTABLENAME_LINKFILE + " WHERE ANSWERID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, answerId);
			int result = ps.executeUpdate();
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "ANSWERSUBTABLEIMAGE revert Error.");
			return false;
		}
		return true;
	}

	public static boolean commitPostAnswer(Connection conn, int answerId) {
		String sql = "UPDATE ANSWERTABLE SET ANSWERVALID = ? WHERE ANSWERID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBoolean(1, true);
			ps.setInt(2, answerId);
			int result = ps.executeUpdate();
			if(result == 1) {
				return true;
			}
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "AnswerTable User Check Error.");
		}
		return false;
	}

	public static boolean insertQuestionLinkData(Connection conn, int fileLinkId, byte[] filename, byte[] filedata, int answerId) {
		String sql = "INSERT INTO " + SUBTABLENAME_LINKFILE
				+ " (ANSWERID, ANSWER_LINKFILEID, ANSWER_LINKFILENAME, ANSWER_LINKFILEDATA)"
				+ " VALUES(?, ?, ? ,?)";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, answerId);
			ps.setInt(2, fileLinkId);
			ps.setBytes(3, filename);
			ps.setBytes(4, filedata);
			int result = ps.executeUpdate();
			if(result == 1) {
				return true;
			}
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "AnswerTable Insert LinkData Error.");
		}
		return false;
	}

	public static Map<String, Object> getAnswerImageData(Connection conn, int answerId) {
		Map<String, Object> retMap = new HashMap<>();
		String sql = "SELECT ANSWER_IMAGEDATA FROM " + SUBTABLENAME_IMAGE + " WHERE ANSWERID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, answerId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				retMap.put("answerImgData", new String(rs.getBytes(1), CharUtil.getCharset()));
			}
			return retMap;
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "getAnswerImageData error. data base connection");
			return new HashMap<>();
		}
	}

	public static Map<String, Object> getAnswerLinkFileData(Connection conn, int answerId) {
		Map<String, Object> retMap = new HashMap<>();
		String sql = "SELECT ANSWER_LINKFILENAME, ANSWER_LINKFILEDATA FROM " + SUBTABLENAME_LINKFILE + " WHERE ANSWERID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, answerId);
			ResultSet rs = ps.executeQuery();
			List<Map<String, Object>> retArrayList = new ArrayList<>();
			while(rs.next()) {
				Map<String, Object> innerMap = new HashMap<>();
				innerMap.put("linkFileName", new String(rs.getBytes(1), CharUtil.getCharset()));
				innerMap.put("linkFileData", new String(rs.getBytes(2), CharUtil.getCharset()));
				retArrayList.add(innerMap);
			}
			retMap.put("answerLinkFile", retArrayList);
			return retMap;
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "answerLinkFileData error. data base connection");
			return new HashMap<>();
		}
	}

	public static Map<Integer, Boolean> getGoodActPatternByUserId(Connection conn, int answerId) {
		String sql = "SELECT GOOD_ACTION_USER_ID, GOOD_ACTION FROM "+ POINTACTCASHE_GOODPOINT_KEY + " WHERE ANSWERID = ?";
		Map<Integer, Boolean> goodActPatternMap = new HashMap<>();
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, answerId);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				goodActPatternMap.put(rs.getInt(1), rs.getBoolean(2));
			}
			return goodActPatternMap;
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "answerLinkFileData error. data base connection");
			return new HashMap<>();
		}
	}

	private static boolean goodActDataExistCheckByAnswerId(Connection conn, int answerId) {
		String sql = "SELECT ANSWERID FROM "+ POINTACTCASHE_GOODPOINT_KEY + " WHERE ANSWERID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, answerId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				return true;
			}
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "answerLinkFileData error. data base connection");
		}
		return false;
	}

	public static void updateGoodActPatternUserInfo(Connection conn, int answerId, Map<Integer, Boolean> userMap) {
		Map<Integer, Boolean> userGoodActMap = getGoodActPatternByUserId(conn, answerId);
		
		if(!userGoodActMap.isEmpty()) {
			//Delete User
			int[] deleteUserId = userMap.entrySet().stream().filter(e -> !e.getValue()).peek(e -> userGoodActMap.remove(e.getKey())).mapToInt(e-> e.getKey()).toArray();
			String sql = "DELET FROM "+ POINTACTCASHE_GOODPOINT_KEY + 
					" WHERE ANSWERID = ? AND GOOD_ACTION_USER_ID IN (" + createSeparateLinerCharcter('?', deleteUserId.length) + ")";
			try(PreparedStatement ps = conn.prepareStatement(sql)) {
				int parameterIndex = 1;
				ps.setInt(parameterIndex++, answerId);
				for(int userId : deleteUserId) {
					ps.setInt(parameterIndex++, userId);
				}
				ps.executeUpdate();
			} catch(SQLException e) {
				ServerLogger.getInstance().warn(e, "answerLinkFileData error. data base connection");
			}
		}
		int[] insertUserIds = userMap.entrySet().stream().filter(e -> e.getValue() && !userGoodActMap.containsKey(e.getKey())).mapToInt(e-> e.getKey()).toArray();
		String insertSQL = "INSERT INTO ANSWERGOODPOINTTABLE (ANSWERID, GOOD_ACTION_USER_ID, GOOD_ACTION)"
				+ "VALUES(?, ?, ?)";
		for(int userId : insertUserIds) {
			try(PreparedStatement ps = conn.prepareStatement(insertSQL)) {
				ps.setInt(1, answerId);
				ps.setInt(2, userId);
				ps.setBoolean(3, true);
				ps.executeUpdate();
			} catch(SQLException e) {
				ServerLogger.getInstance().warn(e, "answerLinkFileData error. data base connection");
			}
		}
	}

	private static synchronized int getRegistNumber(Connection conn) {
		return NumberingTableAccessor.getMyId(conn, TABLENAME);
	}

	private static String createSeparateLinerCharcter(char c, int length) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < length; i++) {
			sb.append(c + ",");
		}
		return sb.substring(0, sb.length() - 1);
	}
}
