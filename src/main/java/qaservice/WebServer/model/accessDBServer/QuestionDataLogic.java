package qaservice.WebServer.model.accessDBServer;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import qaservice.Common.dbaccesor.QuestionTableAccessor;
import qaservice.Common.model.user.UserInfo;
import qaservice.WebServer.dbconnect.DBConnectionOperation;

public class QuestionDataLogic {
	public static List<Map<String, Object>> getAllQuestionTitleData(int answerPattern) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return QuestionTableAccessor.getAllQuestionTitleData(conn, answerPattern);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static Map<String, Object> getQuestionDetailData(int questionId, int textInImgSize) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return QuestionTableAccessor.getQuestionDetailData(conn, questionId, textInImgSize);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static int getQuestionId(String username) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return QuestionTableAccessor.getQuestionId(conn, username);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static int insertQuestionTable(byte[] titleData, byte[] questionDetailData, String username, int type) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return QuestionTableAccessor.insertQuestionTable(conn, titleData, questionDetailData, username, type);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static boolean insertQuestionTextData(String textTitle, String textDetail, int type, 
			UserInfo userInfo, int questionId) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return QuestionTableAccessor.insertQuestionTextData(
					conn, textTitle, textDetail, type, userInfo, questionId);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static boolean commitPostQuestion(int questionId) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return QuestionTableAccessor.commitPostQuestion(conn, questionId);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static boolean revertPostQuestion(int questionId) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return QuestionTableAccessor.revertPostQuestion(conn, questionId);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static boolean insertQuestionImgData(byte[] imgData, int questionId) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return QuestionTableAccessor.insertQuestionImgData(conn, imgData, questionId);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static boolean insertQuestionLinkData(int fileLinkId, byte[] filename, byte[] filedata, int questionId) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return QuestionTableAccessor.insertQuestionLinkData(conn, fileLinkId, filename, filedata, questionId);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static boolean isPostQuestUser(int questionId, int userId) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return QuestionTableAccessor.isPostQuestUser(conn, questionId, userId);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static List<Map<String, Object>> searchQestionData(byte[] searchTextBytes, int answerPattern) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return QuestionTableAccessor.searchQestionData(conn, searchTextBytes, answerPattern);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}

	public static Map<String, Object> getQutestionImgRawData(int questionId) {
		DBConnectionOperation dbConnOpe = DBConnectionOperation.getInstance();
		Connection conn = dbConnOpe.getConnetion();
		try {
			return QuestionTableAccessor.getQuestionImageData(conn, questionId);
		} finally {
			dbConnOpe.endUsedConnctionNotify(conn);
		}
	}
}
