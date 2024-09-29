package qaservice.hellostatistics.accessDBServer;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import datasheet.hellostatistics.dbconnect.DBConnectionOperation;
import qaservice.Common.dbaccesor.AnswerTableAccessor;
import qaservice.Common.model.user.UserInfo;

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

	public static boolean insertQuestionImgData(byte[] imgData, int answerId) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return AnswerTableAccessor.insertQuestionImgData(conn, imgData, answerId);
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

	public static boolean insertQuestionLinkData(int fileLinkId, byte[] filename, byte[] filedata, int answerId) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return AnswerTableAccessor.insertQuestionLinkData(conn, fileLinkId, filename, filedata, answerId);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static Map<String, Object> getAnswerImageData(int answerId) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return AnswerTableAccessor.getAnswerImageData(conn, answerId);
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
}
