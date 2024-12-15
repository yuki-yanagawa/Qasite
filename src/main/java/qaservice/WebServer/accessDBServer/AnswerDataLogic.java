package qaservice.WebServer.accessDBServer;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import qaservice.Common.dbaccesor.AnswerTableAccessor;
import qaservice.Common.model.user.UserInfo;
import qaservice.WebServer.dbconnect.DBConnectionOperation;

public class AnswerDataLogic {
	public static Map<Integer, Map<String, Object>> getAnswerIdAndLatestUpdateDateGroupingQuestionId() {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return AnswerTableAccessor.getAnswerIdAndLatestUpdateDateGroupingQuestionId(conn);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static List<Map<String, Object>> getAnswerDetailData(int questionId, UserInfo userInfo) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return AnswerTableAccessor.getAnswerDetailData(conn, questionId, userInfo);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static int insertAnswer(byte[] answerData, int questionId, UserInfo userInfo) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return AnswerTableAccessor.insertAnswer(conn, answerData, questionId, userInfo);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static int helpfulAction(int answerId, String username, int actionResult) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return AnswerTableAccessor.helpfulAction(conn, answerId, username, actionResult);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static boolean insertAnswerImgData(byte[] imgData, int answerId) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return AnswerTableAccessor.insertAnswerImgData(conn, imgData, answerId);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static boolean isPostAnswerUser(int answerId, int userId) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return AnswerTableAccessor.isPostAnswerUser(conn, answerId, userId);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static boolean revertPostAnswer(int answerId) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return AnswerTableAccessor.revertPostAnswer(conn, answerId);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static boolean commitPostAnswer(int answerId) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return AnswerTableAccessor.commitPostAnswer(conn, answerId);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static boolean insertAnswerLinkData(int fileLinkId, byte[] filename, byte[] filedata, int answerId) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return AnswerTableAccessor.insertAnswerLinkData(conn, fileLinkId, filename, filedata, answerId);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static Map<String, Object> getAnswerImageData(int answerId) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return AnswerTableAccessor.getAnswerImageData(conn, answerId, -1);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static Map<String, Object> getAnswerImageData(int answerId, int imgResizeData) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return AnswerTableAccessor.getAnswerImageData(conn, answerId, imgResizeData);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static Map<String, Object> getAnswerLinkFileData(int answerId) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return AnswerTableAccessor.getAnswerLinkFileData(conn, answerId);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static int getAnswerId(String username) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return AnswerTableAccessor.getAnswerId(conn, username);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static boolean insertAnswerTextData(int answerId, String answerData, int questionId, int userId) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return AnswerTableAccessor.insertAnswerTextData(conn, answerId, answerData, questionId, userId);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}
}
