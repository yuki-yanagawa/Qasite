package qaservice.Common.dbaccesor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import qaservice.WebServer.dbconnect.DBConnectionOperation;
import qaservice.WebServer.logger.ServerLogger;

public class NumberingTableAccessor {
	/**
	 * @param tablename
	 * @return newId;
	 * if newId getting failed then return is -1.
	 */
	public static int getMyId(Connection conn, String tablename) {
		int retId = -1;
		int tableId = -1;
		String sql = "SELECT N.NEXTNUMBER, N.TABLEID FROM NUMBERINGTABLE AS N "
				+ "JOIN TABLELISTMAST AS T ON T.TABLEID = N.TABLEID WHERE T.TABLENAME = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, tablename);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				retId = rs.getInt(1);
				tableId = rs.getInt(2);
			}
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "NumberingTable access getMyId Error");
			return -1;
		}
		
		if(retId == -1 || tableId == -1) {
			ServerLogger.getInstance().warn("NumberingTable access getMyId Error");
			return retId;
		}
		
		sql = "UPDATE NUMBERINGTABLE SET NEXTNUMBER = ? WHERE TABLEID = ?";
		try(PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, retId + 1);
			ps.setInt(2, tableId);
			int result = ps.executeUpdate();
			if(result != 1) {
				ServerLogger.getInstance().warn("NumberingTable update Error");
				return -1;
			}
 		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e, "NumberingTable update Error");
			return -1;
		}
		return retId;
	}
}
