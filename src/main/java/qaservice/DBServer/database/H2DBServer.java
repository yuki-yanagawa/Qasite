package qaservice.DBServer.database;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.tools.Server;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.Common.debug.DebugChecker;
import qaservice.DBServer.database.exception.DBSettingException;
import qaservice.DBServer.main.DBServerMainGuiStart;
import qaservice.DBServer.util.DBServerPropReader;

public class H2DBServer implements IDBServer {
	private static final String DATABASE_PATH = "databases";
	private static final String DATABASE_NAME = "datasheed.db";
	private static final String CREATE_INIT_DBTABLE_SQL = "createtable/initcreatetable.sql";
	private static final String CREATE_INIT_STOREDPROCEDURE_SQL = "createtable/initstoredProcedure.sql";
	
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
					QasiteLogger.warn("start error", e);
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
			QasiteLogger.warn("h2 driver not found error", e);
			throw new DBSettingException(e.getMessage());
		} catch(SQLException se) {
			QasiteLogger.warn("db start error", se);
			throw new DBSettingException(se.getMessage());
		} catch(InterruptedException ie) {
			QasiteLogger.warn("db start interrupt", ie);
		}
		
		port_ = port;
		if(createDBTableFlg) {
			createDBTable();
		}
		createStoredProcedure();
	}
	
	private boolean existDBFile() {
		return Files.exists(Paths.get(DATABASE_PATH + File.separator + DATABASE_NAME));
	}
	
	private void createNewDB() throws DBSettingException{
		try {
			File file = Paths.get(DATABASE_PATH  + File.separator +  DATABASE_NAME).toFile();
			file.createNewFile();
		} catch(IOException e) {
			QasiteLogger.warn("createNewDB failed.", e);
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
					if(DebugChecker.isDEBUGMode()) {
						QasiteLogger.info(fileLine);
					}
					st.executeUpdate(fileLine);
					fileLine = buf.readLine();
				}
			} catch(SQLException e) {
				QasiteLogger.warn("create table error.", e);
			}
		} catch(FileNotFoundException f) {
			QasiteLogger.warn("create table error. file", f);
		} catch(IOException ie) {
			QasiteLogger.warn("create table error. io", ie);
		}
	}

	private void createStoredProcedure() {
		Path path = Paths.get(DATABASE_PATH + File.separator + CREATE_INIT_STOREDPROCEDURE_SQL);
		try(FileInputStream fis = new FileInputStream(path.toFile());
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr)){
			try(Connection conn = DriverManager.getConnection(isNessaryInitConnectPath(), "sa", "");
				Statement st = conn.createStatement()) {
				String line;
				while((line = br.readLine()) != null) {
					if(DebugChecker.isDEBUGMode()) {
						QasiteLogger.info(line);
					}
					st.executeUpdate(line);
				}
			} catch(SQLException e) {
				QasiteLogger.warn("create stored procedure error.", e);
			}
		} catch(IOException e) {
			QasiteLogger.warn("create stored procedure error.", e);
		}
	}

	private String isNessaryInitConnectPath() {
		return "jdbc:h2:"+ "./" + DATABASE_PATH + "/" + DATABASE_NAME;
	}

	@Override
	public DBConnectModel getDBConnectPath() {
		if(port_ == -1) {
			return null;
		}
		String host = DBServerPropReader.getProperties("serverHost").toString();
		//System.out.println("Path : " + "jdbc:h2:tcp://" + host + ":" + String.valueOf(port_) + "/./" + DATABASE_PATH + "/" + DATABASE_NAME + ";MODE=MYSQL;");
		return new DBConnectModel("jdbc:h2:tcp://" + host + ":" + String.valueOf(port_) + "/./" + DATABASE_PATH + "/" + DATABASE_NAME + ";MODE=MYSQL;", "sa", "");
		
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
//
//		}
//	}
}
