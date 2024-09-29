package qaservice.DBServer.database.storedProcedure;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import qaservice.Common.dbaccesor.AnswerTableAccessor;
import qaservice.DBServer.database.rankCashe.RankCashe;

public class GoodActionProcedure {
	private static Map<Integer, Map<Integer, Boolean>> goodActMapCashe_ = new HashMap<>();

	public static synchronized Map<Integer, Boolean> updateActionAndGetUserActionData(Connection conn, int answerId, int userId, int action) {
		RankCashe.registAccessAnswerCashe(answerId);
		if(!goodActMapCashe_.containsKey(answerId)) {
			Map<Integer, Boolean> innerCashe = AnswerTableAccessor.getGoodActPatternByUserId(conn, answerId);
			goodActMapCashe_.put(answerId, innerCashe);
		}
		Map<Integer, Boolean> userIdMap = goodActMapCashe_.get(answerId);
		boolean actResult = false;
		if(action == 1) {
			actResult = true;
		}
		userIdMap.put(userId, actResult);
		return userIdMap;
	}

	public static Map<Integer, Boolean> getGoodActionData(Connection conn, int answerId, int userId, int action) {
		return new HashMap<>();
	}

	public static synchronized void updateGoodActMapCasheAndTable(Connection conn, List<Integer> toDBAnswerIdList) {
		for(int answerId : toDBAnswerIdList) {
			Map<Integer, Boolean> userMap = goodActMapCashe_.remove(answerId);
			if(userMap.isEmpty()) {
				continue;
			}
			AnswerTableAccessor.updateGoodActPatternUserInfo(conn, answerId, userMap);
		}
	}
}
