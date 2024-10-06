package qaservice.DBServer.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.tools.Server;

import qaservice.DBServer.database.exception.DBSettingException;
import qaservice.DBServer.main.DBServerMainGuiStart;
import qaservice.DBServer.util.DBServerPropReader;

public class H2DBServer implements IDBServer {
	private static final String DATABASE_PATH = "databases";
	private static final String DATABASE_NAME = "datasheed.db";
	private static final String CREATE_INIT_DBTABLE_SQL = "createtable/initcreatetable.sql";
	
	private Server dbServer_;
	private int port_ = -1;
	private boolean isNeedInitTableCreateSQL_ = false;

	@Override
	public void start(int port, boolean initalizedb) throws DBSettingException {
		if(initalizedb) {
			Path dbDirPath = Paths.get(DATABASE_PATH);
			for(File f : dbDirPath.toFile().listFiles()) {
				if(f.isDirectory()) {
					continue;
				}
				try { 
					Files.delete(f.toPath());
				} catch(IOException e) {
					//e.printStackTrace();
					DBServerMainGuiStart.guiConsoleOut(e.getMessage());
					throw new DBSettingException(e.getMessage());
				}
			}
		}
		
		boolean createDBTableFlg = false;
		if(initalizedb || !existDBFile()) {
			createNewDB();
			createDBTableFlg = true;
		}

		try {
			Class.forName("org.h2.Driver");
			String[] tcpServeroptions = new String[]{"-tcp", "-tcpAllowOthers", "-tcpPort", String.valueOf(port)};
			dbServer_ = org.h2.tools.Server.createTcpServer(tcpServeroptions);
			dbServer_.start();
			Thread.sleep(2000);
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
			DBServerMainGuiStart.guiConsoleOut(e.getMessage());
			throw new DBSettingException(e.getMessage());
		} catch(SQLException se) {
			se.printStackTrace();
			DBServerMainGuiStart.guiConsoleOut(se.getMessage());
			throw new DBSettingException(se.getMessage());
		} catch(InterruptedException ie) {
			ie.printStackTrace();
			DBServerMainGuiStart.guiConsoleOut(ie.getMessage());
		}
		
		port_ = port;
		if(createDBTableFlg) {
			createDBTable();
		}
	}
	
	private boolean existDBFile() {
		return Files.exists(Paths.get(DATABASE_PATH + File.separator + DATABASE_NAME));
	}
	
	private void createNewDB() throws DBSettingException{
		try {
			File file = Paths.get(DATABASE_PATH  + File.separator +  DATABASE_NAME).toFile();
			file.createNewFile();
		} catch(IOException e) {
			e.printStackTrace();
			DBServerMainGuiStart.guiConsoleOut(e.getMessage());
			throw new DBSettingException(e.getMessage());
		}
	}
	
	@Override
	public void dbServerShutdown() {
		dbServer_.shutdown();
	}
	
	private void createDBTable() {
		Path path = Paths.get(DATABASE_PATH + File.separator + CREATE_INIT_DBTABLE_SQL);
		try(BufferedReader buf = new BufferedReader(new FileReader(path.toFile()))) {
			String fileLine = buf.readLine();
			try(Connection conn = DriverManager.getConnection(isNessaryInitConnectPath(), "sa", "");
				Statement st = conn.createStatement()) {
				while(fileLine != null) {
					System.out.println(fileLine);
					st.executeUpdate(fileLine);
					fileLine = buf.readLine();
				}
			} catch(SQLException e) {
				e.printStackTrace();
				DBServerMainGuiStart.guiConsoleOut(e.getMessage());
			}
		} catch(FileNotFoundException f) {
			f.printStackTrace();
			DBServerMainGuiStart.guiConsoleOut(f.getMessage());
		} catch(IOException ie) {
			ie.printStackTrace();
			DBServerMainGuiStart.guiConsoleOut(ie.getMessage());
		}
	}

	private String isNessaryInitConnectPath() {
		return "jdbc:h2:"+ "./" + DATABASE_PATH + "/" + DATABASE_NAME;
	}

	@Override
	public String getDBConnectPath() {
		if(port_ == -1) {
			return null;
		}
		String host = DBServerPropReader.getProperties("serverHost").toString();
		//System.out.println("Path : " + "jdbc:h2:tcp://" + host + ":" + String.valueOf(port_) + "/./" + DATABASE_PATH + "/" + DATABASE_NAME + ";MODE=MYSQL;");
		return "jdbc:h2:tcp://" + host + ":" + String.valueOf(port_) + "/./" + DATABASE_PATH + "/" + DATABASE_NAME + ";MODE=MYSQL;";
	}

//	private static void createMasterTable() {
//		try(Connection conn = DriverManager.getConnection("jdbc:h2:"+ DBPATH,"sa","");
//			Statement stmt = conn.createStatement()){
//			String dropSql = "DROP TABLE IF EXISTS MasterTable";
//			stmt.execute(dropSql);
//			String sql = "CREATE TABLE MasterTable " +
//	                   "(tableName VARCHAR(255) not NULL, " +
//	                   " tableDataObject BLOB not NULL, " + 
//	                   " updateTime TIMESTAMP, " + 
//	                   " PRIMARY KEY ( tableName ))";
//			stmt.execute(sql);
//		} catch(SQLException e) {
//			e.printStackTrace();
//		}
//	}
}
