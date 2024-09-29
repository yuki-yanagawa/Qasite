package qaservice.Common.dbaccesor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.h2.command.query.AllColumnsForPlan;

import datasheet.hellostatistics.cryptography.MessageDigestTypeSHA256;
import datasheet.hellostatistics.dbconnect.DBConnectionOperation;
import datasheet.hellostatistics.logger.ServerLogger;
import qaservice.Common.charcterutil.CharUtil;
import qaservice.Common.utiltool.GZipUtil;

public class QuestionTableAccessor {
	public static enum QuestionTableColoumn {
		QUESTIONID("questionId"),
		QUESTION_TITLE("questionTitle"),
		QUESTION_DETAIL_DATA("questionDetailData"),
		QUESTTYPE("questionType"),
		USERID("uesrId"),
		UPDATE_DATE("questionUpdateDate");
		public String clientCommonName;
		private QuestionTableColoumn(String name) {
			clientCommonName = name;
		}
	}
	private static final int QUESTIONDATA_MAX_CASHE = 200;
	private static Map<String, Integer> questionIdSearchMap_ = new HashMap<>();
	private static final String TABLENAME = "QUESTIONTABLE";
	private static final String SUBTABLENAME_IMAGE = "QUESTIONSUBTABLEIMAGE";
	private static final String SUBTABLENAME_LINKFILE = "QUESTIONSUBTABLELINKFILE";
	private static final int EXIST_AND_NO_ANSWER = 0;
	private static final int EXIST_ANSWER = 1;
	private static final int NO_ANSWER = 2;
	public static List<Map<String, Object>> getAllQuestionTitleData(Connection conn, int answerPattern) {
		//ANSWER PATTERN
		//0 is All
		//1 is Exist some answers
		//2 is Do not exist any answers
		List<Map<String, Object>> retMapList = new ArrayList<>();
		Map<Integer, Map<String, Object>> answerIdByQuestionId = new HashMap<>();
		if(answerPattern == EXIST_AND_NO_ANSWER || answerPattern == EXIST_ANSWER) {
			answerIdByQuestionId = AnswerTableAccessor.getAnswerIdAndLatestUpdateDateGroupingQuestionId(conn);
		}

		//String sql = "SELECT QUESTIONID, QUESTION_DETAIL_DATA, QUESTTYPE, UPDATE_DATE FROM QUESTIONTABLE ORDER BY UPDATE_DATE DESC";
		String sql = getMainDisplayQuestionSQL(answerPattern);
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				Map<String, Object> innerRetMap = new HashMap<>();
				int questId = rs.getInt(1);
				innerRetMap.put(QuestionTableColoumn.QUESTIONID.clientCommonName, questId);
				innerRetMap.put(QuestionTableColoumn.QUESTION_TITLE.clientCommonName,
						new String(rs.getBytes(2), CharUtil.getCharset()));
				innerRetMap.put(QuestionTableColoumn.QUESTTYPE.clientCommonName, rs.getInt(3));
				innerRetMap.put(QuestionTableColoumn.UPDATE_DATE.clientCommonName, rs.getString(4).split("\\.")[0]);
				if(answerIdByQuestionId.containsKey(questId)) {
					Map<String, Object> innerMap = answerIdByQuestionId.get(questId);
					innerRetMap.put("answerCount", innerMap.get("answerCount"));
					Timestamp latestDate = (Timestamp)innerMap.get("latestTime");
					innerRetMap.put(AnswerTableAccessor.AnswerTableColoumn.UPDATE_DATE.clientCommonName,
							latestDate.toString().split("\\.")[0]);
				} else {
					innerRetMap.put("answerCount", 0);
					innerRetMap.put(AnswerTableAccessor.AnswerTableColoumn.UPDATE_DATE.clientCommonName, "");
				}
				retMapList.add(innerRetMap);
			}
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "QuestionTable access getAllQuetionData Error");
			return new ArrayList<>();
		}
		return retMapList;
	}

	public static Map<String, Object> getQuestionDetailData(Connection conn, int questionId) {
		Map<String, Object> retMap = new HashMap<>();
		String sql = "SELECT Q.QUESTION_DETAIL_DATA, Q.QUESTTYPE, USR.USERNAME , Q.USERID, Q.UPDATE_DATE FROM QUESTIONTABLE AS Q"
				+ " JOIN USERTABLE AS USR ON USR.USERID = Q.USERID"
				+ " WHERE QUESTIONID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, questionId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				retMap.put("questionDetailData", new String(rs.getBytes(1), CharUtil.getCharset()));
				retMap.put("questionType", rs.getInt(2));
				retMap.put("questionUserName", rs.getString(3));
				retMap.put("questionUserId", rs.getInt(4));
				retMap.put("questionDate", rs.getString(5).split("\\.")[0]);
			}
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "getQuestionDetailData error. data base connection");
			return new HashMap<>();
		}

		Map<String, Object> imgMap = getQuestionImageData(conn, questionId);
		if(!imgMap.isEmpty()) {
			retMap.putAll(imgMap);
		}
		Map<String, Object> linkMap = getQuestionLinkFileData(conn, questionId);
		if(!linkMap.isEmpty()) {
			retMap.putAll(linkMap);
		}
		return retMap;
	}

	public static Map<String, Object> getQuestionImageData(Connection conn, int questionId) {
		Map<String, Object> retMap = new HashMap<>();
		String sql = "SELECT QUESTION_IMAGEDATA FROM " + SUBTABLENAME_IMAGE + " WHERE QUESTIONID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, questionId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				//adjust gzip
				byte[] decompBytes = GZipUtil.decompressed(rs.getBytes(1));
				retMap.put("questionImageData", new String(decompBytes, CharUtil.getCharset()));
			}
			return retMap;
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "getQuestionImageData error. data base connection");
			return new HashMap<>();
		}
	}

	public static Map<String, Object> getQuestionLinkFileData(Connection conn, int questionId) {
		Map<String, Object> retMap = new HashMap<>();
		String sql = "SELECT QUESTION_LINKFILENAME, QUESTION_LINKFILEDATA FROM " + SUBTABLENAME_LINKFILE + " WHERE QUESTIONID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, questionId);
			ResultSet rs = ps.executeQuery();
			List<Map<String, Object>> retArrayList = new ArrayList<>();
			while(rs.next()) {
				Map<String, Object> innerMap = new HashMap<>();
				innerMap.put("linkFileName", new String(rs.getBytes(1), CharUtil.getCharset()));
				innerMap.put("linkFileData", new String(rs.getBytes(2), CharUtil.getCharset()));
				retArrayList.add(innerMap);
			}
			retMap.put("quetionLinkFile", retArrayList);
			return retMap;
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "getQuestionImageData error. data base connection");
			return new HashMap<>();
		}
	}

	public static int insertQuestionTable(Connection conn, byte[] titleData, byte[] questionDetailData, String username, int type) {
		int newId = getRegistNumber(conn);
		if(newId == -1) {
			ServerLogger.getInstance().warn("QuestionTable Insert Error");
			return -1;
		}
		
		int userId = UserTableAccessor.getUserIdByUserName(conn, username);
		if(userId == -1) {
			ServerLogger.getInstance().warn("QuestionTable Insert Error. user id get failed");
			return -1;
		}

		try {
			conn.setAutoCommit(false);
			String sql = "INSERT INTO QUESTIONTABLE "
					+ "(QUESTIONID, QUESTION_TITLE, QUESTION_DETAIL_DATA, QUESTTYPE, USERID, QUESTVALID, UPDATE_DATE) "
					+ "VALUES(?, ?, ?, ?, ?, ?, ?)";
			try(PreparedStatement ps = conn.prepareStatement(sql)) {
				int setIndex = 1;
				ps.setInt(setIndex++, newId);
				ps.setBytes(setIndex++, titleData);
				ps.setBytes(setIndex++, questionDetailData);
				ps.setInt(setIndex++, type);
				ps.setInt(setIndex++, userId);
				ps.setBoolean(setIndex++, false);
				ps.setTimestamp(setIndex++, new Timestamp(System.currentTimeMillis()));
				int result = ps.executeUpdate();
				if(result <= 0) {
					ServerLogger.getInstance().warn("QuestionTable Insert Error. result value");
					return -1;
				}
			}
//			String searchKey = createSearchId(questionTitle);
//			if(searchKey == null) {
//				ServerLogger.getInstance().warn("QuestionTable Insert Error. result value");
//				conn.rollback();
//				return false;
//			}
//			sql = "INSERT INTO QUESTIONTABLEFORTITLESEARCH "
//					+ "(QUESTIONID, QUESTIONTITLE_SHA256) "
//					+ "VALUES(?, ?)";
//			try(PreparedStatement ps = conn.prepareStatement(sql)) {
//				int setIndex = 1;
//				ps.setInt(setIndex++, newId);
//				ps.setString(setIndex++, searchKey);
//				int result = ps.executeUpdate();
//				if(result <= 0) {
//					ServerLogger.getInstance().warn("QuestionTable Insert Error. result value");
//					conn.rollback();
//					return false;
//				}
//			}
//			questionIdSearchMap_.put(searchKey, newId);
			conn.commit();
			conn.setAutoCommit(true);
			return newId;
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "QuestionTable Insert Error.");
			return -1;
		}
	}

	public static boolean commitPostQuestion(Connection conn, int questionId) {
		String sql = "UPDATE QUESTIONTABLE SET QUESTVALID = ? WHERE QUESTIONID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBoolean(1, true);
			ps.setInt(2, questionId);
			int result = ps.executeUpdate();
			if(result == 1) {
				return true;
			}
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "QuestionTable User Check Error.");
		}
		return false;
	}

	public static boolean revertPostQuestion(Connection conn, int questionId) {
		String sql = "DELETE FROM QUESTIONTABLE WHERE QUESTIONID = ? AND QUESTVALID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, questionId);
			ps.setBoolean(2, false);
			int result = ps.executeUpdate();
			if(result != 1) {
				return false;
			}
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "QuestionTable revert Error.");
			return false;
		}

		sql = "DELETE FROM " + SUBTABLENAME_IMAGE + " WHERE QUESTIONID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, questionId);
			int result = ps.executeUpdate();
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "QUESTIONSUBTABLEIMAGE revert Error.");
			return false;
		}

		sql = "DELETE FROM " + SUBTABLENAME_LINKFILE + " WHERE QUESTIONID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, questionId);
			int result = ps.executeUpdate();
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "QUESTIONSUBTABLEIMAGE revert Error.");
			return false;
		}
		return true;
	}

	public static boolean insertQuestionImgData(Connection conn, byte[] imgData, int questionId) {
		String sql = "INSERT INTO " + SUBTABLENAME_IMAGE + " (QUESTIONID, QUESTION_IMAGEDATA) VALUES(?, ?)";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, questionId);
			ps.setBytes(2, imgData);
			int result = ps.executeUpdate();
			if(result == 1) {
				return true;
			}
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "QuestionTable Insert ImgData Error.");
		}
		return false;
	}

	public static boolean insertQuestionLinkData(Connection conn, int fileLinkId, byte[] filename, byte[] filedata, int questionId) {
		String sql = "INSERT INTO " + SUBTABLENAME_LINKFILE
				+ " (QUESTIONID, QUESTION_LINKFILEID, QUESTION_LINKFILENAME, QUESTION_LINKFILEDATA)"
				+ " VALUES(?, ?, ? ,?)";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, questionId);
			ps.setInt(2, fileLinkId);
			ps.setBytes(3, filename);
			ps.setBytes(4, filedata);
			int result = ps.executeUpdate();
			if(result == 1) {
				return true;
			}
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "QuestionTable Insert LinkData Error.");
		}
		return false;
	}

	public static boolean isPostQuestUser(Connection conn, int questionId, int userId) {
		String sql = "SELECT QUESTIONID FROM QUESTIONTABLE WHERE QUESTIONID = ? AND USERID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, questionId);
			ps.setInt(2, userId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				return true;
			}
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "QuestionTable User Check Error.");
		}
		return false;
	}

	public static boolean IsQuestionCreateUser(Connection conn, int questionId, String username) {
		int userId = UserTableAccessor.getUserIdByUserName(conn, username);
		String sql = "SELECT USERID FROM QUESTIONTABLE WHERE QUESTIONID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, questionId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				if(userId == rs.getInt(1)) return true;
			}
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "QuestionTable User Check Error.");
		}
		return false;
	}

	public static List<Map<String, Object>> searchQestionData(Connection conn, byte[] searchTextBytes, int answerPattern) {
		//ANSWER PATTERN
		//0 is All
		//1 is Exist some answers
		//2 is Do not exist any answers
		List<Map<String, Object>> retMapList = new ArrayList<>();
		Map<Integer, Map<String, Object>> answerIdByQuestionId = new HashMap<>();
		if(answerPattern == EXIST_AND_NO_ANSWER || answerPattern == EXIST_ANSWER) {
			answerIdByQuestionId = AnswerTableAccessor.getAnswerIdAndLatestUpdateDateGroupingQuestionId(conn);
		}
		String searchStr = new String(searchTextBytes, CharUtil.getCharset());

		String sql = getSearchQuestionSQL(answerPattern);
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				boolean searchFlg = false;
				rs.getInt(1);
				byte[] titleBytes = rs.getBytes(2);
				String titleStr = new String(titleBytes, CharUtil.getCharset());
				byte[] detailDataBytes = rs.getBytes(3);
				String detailDataStr = new String(detailDataBytes, CharUtil.getCharset());
				if(titleStr.indexOf(searchStr) >= 0 || detailDataStr.indexOf(searchStr) >= 0) {
					searchFlg = true;
				}
				if(!searchFlg) {
					continue;
				}
				Map<String, Object> innerRetMap = new HashMap<>();
				int questId = rs.getInt(1);
				innerRetMap.put(QuestionTableColoumn.QUESTIONID.clientCommonName, questId);
				innerRetMap.put(QuestionTableColoumn.QUESTION_TITLE.clientCommonName, titleStr);
				innerRetMap.put(QuestionTableColoumn.QUESTTYPE.clientCommonName, rs.getInt(4));
				innerRetMap.put(QuestionTableColoumn.UPDATE_DATE.clientCommonName, rs.getString(5).split("\\.")[0]);
				if(answerIdByQuestionId.containsKey(questId)) {
					Map<String, Object> innerMap = answerIdByQuestionId.get(questId);
					innerRetMap.put("answerCount", innerMap.get("answerCount"));
					Timestamp latestDate = (Timestamp)innerMap.get("latestTime");
					innerRetMap.put(AnswerTableAccessor.AnswerTableColoumn.UPDATE_DATE.clientCommonName,
							latestDate.toString().split("\\.")[0]);
				} else {
					innerRetMap.put("answerCount", 0);
					innerRetMap.put(AnswerTableAccessor.AnswerTableColoumn.UPDATE_DATE.clientCommonName, "");
				}
				retMapList.add(innerRetMap);
			}
			return retMapList;
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "searchQuestion data Error.");
			return new ArrayList<>();
		}
	}

	private static String getSearchQuestionSQL(int answerPattern) {
		//ANSWER PATTERN
		//0 is All
		//1 is Exist some answers
		//2 is Do not exist any answers
		String sql = "";
		switch(answerPattern) {
		case EXIST_AND_NO_ANSWER :
			sql = "SELECT QUESTIONID, QUESTION_TITLE, QUESTION_DETAIL_DATA, QUESTTYPE, UPDATE_DATE FROM QUESTIONTABLE WHERE QUESTVALID = true ORDER BY UPDATE_DATE DESC";
			break;
		case EXIST_ANSWER:
			sql = "SELECT QUESTIONID, QUESTION_TITLE, QUESTION_DETAIL_DATA, QUESTTYPE, UPDATE_DATE FROM QUESTIONTABLE"
				+ "WHERE QUESTIONID IN (SELECT DISTINCT QUESTIONID FROM ANSWERTABLE WHERE ANSWERVALID = true) AND QUESTVALID = true ORDER BY UPDATE_DATE DESC";
			break;
		case NO_ANSWER:
			sql = "SELECT QUESTIONID, QUESTION_TITLE, QUESTION_DETAIL_DATA, QUESTTYPE, UPDATE_DATE FROM QUESTIONTABLE"
				+ "WHERE QUESTIONID NOT IN (SELECT DISTINCT QUESTIONID FROM ANSWERTABLE WHERE ANSWERVALID = true) AND QUESTVALID = true ORDER BY UPDATE_DATE DESC";
			break;
		default:
			sql = "SELECT QUESTIONID, QUESTION_TITLE, QUESTION_DETAIL_DATA, QUESTTYPE, UPDATE_DATE FROM QUESTIONTABLE WHERE QUESTVALID = true ORDER BY UPDATE_DATE DESC";
			break;
		}
		return sql;
	}

	private static String getMainDisplayQuestionSQL(int answerPattern) {
		//ANSWER PATTERN
		//0 is All
		//1 is Exist some answers
		//2 is Do not exist any answers
		String sql = "";
		switch(answerPattern) {
		case EXIST_AND_NO_ANSWER :
			sql = "SELECT QUESTIONID, QUESTION_TITLE, QUESTTYPE, UPDATE_DATE FROM QUESTIONTABLE WHERE QUESTVALID = true ORDER BY UPDATE_DATE DESC";
			break;
		case EXIST_ANSWER:
			sql = "SELECT QUESTIONID, QUESTION_TITLE, QUESTTYPE, UPDATE_DATE FROM QUESTIONTABLE"
					+ " WHERE QUESTIONID IN (SELECT DISTINCT QUESTIONID FROM ANSWERTABLE WHERE ANSWERVALID = true) AND QUESTVALID = true ORDER BY UPDATE_DATE DESC";
			break;
		case NO_ANSWER:
			sql = "SELECT QUESTIONID, QUESTION_TITLE, QUESTTYPE, UPDATE_DATE FROM QUESTIONTABLE"
					+ " WHERE QUESTIONID NOT IN (SELECT DISTINCT QUESTIONID FROM ANSWERTABLE WHERE ANSWERVALID = true) AND QUESTVALID = true ORDER BY UPDATE_DATE DESC";
			break;
		default:
			sql = "SELECT QUESTIONID, QUESTION_TITLE, QUESTTYPE, UPDATE_DATE FROM QUESTIONTABLE WHERE QUESTVALID = true ORDER BY UPDATE_DATE DESC";
			break;
		}
		return sql;
	}

	private static int[] getQestionIdByAnswerPattern(int answerPattern) {
		Set<Integer> questionIdSet = new HashSet<>();
		//ANSWER PATTERN
		//0 is All
		//1 is Exist some answers
		//2 is Do not exist any answers
		String sql = "";
		switch(answerPattern) {
		case 0:
			sql = "SELECT QUESTIONID FROM QUESTIONTABLE WHERE QUESTVALID = true";
			break;
		case 1:
			sql = "SELECT DISTINCT Q.QUESTIONID FROM QUESTIONTABLE AS Q JOIN ANSWERTABLE AS A ON Q.QUESTIONID = A.QUESTIONID WHERE Q.QUESTVALID = true";
			break;
		case 2:
			sql = "SELECT Q.QUESTIONID, A.ANSWERID FROM QUESTIONTABLE AS Q LEFT OUTER JOIN ANSWERTABLE AS A ON Q.QUESTIONID = A.QUESTIONID WHERE Q.QUESTVALID = true AND A.ANSWERID = null";
			break;
		default:
			sql = "SELECT QUESTIONID FROM QUESTIONTABLE WHERE QUESTVALID = true";
			break;
		}
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				questionIdSet.add(rs.getInt(1));
			}
			return questionIdSet.stream().mapToInt(e -> e).toArray();
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "getQestionIdByAnswerPattern Error");
			return new int[0];
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	private static String createSearchId(byte[] titleBytes) {
		byte[] diestBytes = MessageDigestTypeSHA256.digest(titleBytes);
		if(diestBytes == null) {
			return null;
		}
		return Base64.getEncoder().encodeToString(diestBytes);
	}

	private static synchronized int getRegistNumber(Connection conn) {
		return NumberingTableAccessor.getMyId(conn, TABLENAME);
	}
}
