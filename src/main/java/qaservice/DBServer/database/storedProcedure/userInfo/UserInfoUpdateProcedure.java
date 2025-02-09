package qaservice.DBServer.database.storedProcedure.userInfo;

import java.sql.Connection;

import qaservice.Common.dbaccesor.UserTableAccessor;

public class UserInfoUpdateProcedure {
	public static boolean updateUserIntroduction(Connection conn, int userid, byte[] userIntroductionText) {
		return UserTableAccessor.updateUserIntroduction(conn, userid, userIntroductionText);
	}

	public static boolean updateUserPictureData(Connection conn, int userid, byte[] picutreData) {
		return UserTableAccessor.updateUserPictureData(conn, userid, picutreData);
	}
}
