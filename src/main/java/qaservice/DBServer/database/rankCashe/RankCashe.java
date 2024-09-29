package qaservice.DBServer.database.rankCashe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import qaservice.DBServer.database.storedProcedure.GoodActionProcedure;

public class RankCashe {
	private static int MAX_SAVED_ANSWER_CASHE_SIZE = 100;
	private static int MAX_SAVED_QUESTION_CASHE_SIZE = 30;
	private static Map<Integer, Integer> answerAccessRankCashe_ = new HashMap<>();
	private static Map<Integer, Integer> questionAccessRankCashe_ = new HashMap<>();

	public static synchronized void registAccessAnswerCashe(int answerId) {
		if(answerAccessRankCashe_.containsKey(answerId)) {
			answerAccessRankCashe_.put(answerId, answerAccessRankCashe_.get(answerId) + 1);
		} else {
			answerAccessRankCashe_.put(answerId, 1);
		}
	}

	public static synchronized void registAccessQuestionCashe(int questionId) {
		if(questionAccessRankCashe_.containsKey(questionId)) {
			questionAccessRankCashe_.put(questionId, questionAccessRankCashe_.get(questionId) + 1);
		} else {
			questionAccessRankCashe_.put(questionId, 1);
		}
	}

	public static synchronized void updateRankCashe(Connection conn) {
		List<Integer> answerRemoveCasheList = new ArrayList<>();
//		List<Integer> questionRemoveCasheList = new ArrayList<>();
//		if(questionAccessRankCashe_.size() > MAX_SAVED_QUESTION_CASHE_SIZE) {
//			Map<Integer, Integer> tmpMap = new HashMap<>();
//			int tmpLimit = (int)(MAX_SAVED_QUESTION_CASHE_SIZE * 0.7);
//			int currentIndex = 0;
//			for(int questionId : questionAccessRankCashe_.entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue()).mapToInt(e -> e.getKey()).toArray()) {
//				if(currentIndex >= tmpLimit) {
//					questionRemoveCasheList.add(questionId);
//					continue;
//				}
//				tmpMap.put(questionId, 0);
//			}
//			questionAccessRankCashe_.clear();
//			questionAccessRankCashe_ = tmpMap;
//		}
		if(answerAccessRankCashe_.size() > MAX_SAVED_ANSWER_CASHE_SIZE) {
			Map<Integer, Integer> tmpMap = new HashMap<>();
			int tmpLimit = (int)(MAX_SAVED_ANSWER_CASHE_SIZE * 0.7);
			int currentIndex = 0;
			for(int answerId : answerAccessRankCashe_.entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue()).mapToInt(e -> e.getKey()).toArray()) {
				if(currentIndex >= tmpLimit) {
					answerRemoveCasheList.add(answerId);
					continue;
				}
				tmpMap.put(answerId, 0);
			}
			answerAccessRankCashe_.clear();
			answerAccessRankCashe_ = tmpMap;
			GoodActionProcedure.updateGoodActMapCasheAndTable(conn, answerRemoveCasheList);
		}

	}
}
