package qaservice.DBServer.database.storedProcedure.userInfo;

import java.sql.Connection;

import qaservice.Common.dbaccesor.UserTableAccessor;

public class UserInfoGetProcedure {
	public static byte[] getUserPictureData(Connection conn, int userid) {
		return UserTableAccessor.getUserPictureData(conn, userid);
	}
}
