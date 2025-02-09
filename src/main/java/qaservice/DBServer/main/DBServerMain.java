package qaservice.DBServer.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Arrays;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.Common.charcterutil.CharUtil;
import qaservice.DBServer.database.DBConnectModel;
import qaservice.DBServer.database.H2DBServer;
import qaservice.DBServer.database.IDBServer;
import qaservice.DBServer.database.worker.backup.BackUpCollectThread;
import qaservice.DBServer.database.worker.userupdate.UserPointUpdateWorker;
import qaservice.DBServer.keys.KeysOperation;
import qaservice.DBServer.keys.exception.KeySettingException;
import qaservice.DBServer.util.DBServerPropReader;
import qaservice.DBServer.worker.rankcheck.RankCheckWorker;


public class DBServerMain {
	private static final String SERVICE_NAME = "QasiteDBServer";
	public static void main(String[] args) throws Exception {
		QasiteLogger.startLogger(SERVICE_NAME);
		boolean guiStartFlg = args == null ? true : false;
		if(guiStartFlg) DBServerMainGuiStart.guiConsoleOut("DB Server Starting.....");
		//DB Server Stop Key
		try {
			KeysOperation.initialize();
		} catch(KeySettingException e) {
			QasiteLogger.warn("key operation error.", e);
			return;
		}
		if(guiStartFlg) DBServerMainGuiStart.guiConsoleOut("Key initialize end....");

		//DB Server Start
		boolean dbInitSetting = Boolean.parseBoolean(DBServerPropReader.getProperties("dbInitSetting").toString());
		int port = Integer.parseInt(DBServerPropReader.getProperties("port").toString());
		IDBServer dbServ = new H2DBServer();
		DBConnectModel dbConnectModel = null;
		try {
			dbServ.start(port, dbInitSetting);
			dbConnectModel = dbServ.getDBConnectPath();
		} catch(Throwable e) {
			QasiteLogger.warn("DB start error.DB Server can not start.", e);
			if(guiStartFlg) {
				DBServerMainGuiStart.guiConsoleOut(e.getMessage());
			} else {
				System.exit(-1);
			}
			return;
		}

		//Rank Check Worker Start
		new RankCheckWorker(dbConnectModel).start();
		if(guiStartFlg) DBServerMainGuiStart.guiConsoleOut("db server start!!!!");
		//Server Strop Operator Reciver
		boolean needServerOperationReciver = Boolean.parseBoolean(DBServerPropReader.getProperties("needStartServerOperationReciver").toString());
		if(needServerOperationReciver) {
			new DBServerOperationReciver(dbServ).start();
		}

		//DataBase Collect Thread For BackUp
		new BackUpCollectThread(dbConnectModel).start();

		//UserPoint Count Thread
		new UserPointUpdateWorker(dbConnectModel).start();
	}
	
	
	static class DBServerOperationReciver extends Thread {
		private IDBServer dbServer_;
		private boolean isRunning_;
		DBServerOperationReciver(IDBServer server) {
			dbServer_ = server;
			isRunning_ = true;
		}
		@Override
		public void run() {
			PublicKey publicKey = KeysOperation.createPublicKeyFromByteData();
			try(ServerSocket svrSock = new ServerSocket()) {
				String host = DBServerPropReader.getProperties("serverHost").toString();
				int port = Integer.parseInt(DBServerPropReader.getProperties("serverStopOpratePort").toString());
				svrSock.bind(new InetSocketAddress(host, port));
				while(isRunning_) {
					Socket clisock = svrSock.accept();
					InputStream is = clisock.getInputStream();
					OutputStream os = clisock.getOutputStream();
					byte[] buffByte = new byte[512]; 
					int readData = is.read(buffByte);
					byte[] signData = Arrays.copyOf(buffByte, readData);
					if(KeysOperation.verifiy(publicKey, signData, "CLOSE".getBytes(CharUtil.getCharset()))) {
						stopServer();
						isRunning_ = false;
						os.write("END".getBytes());
					}
					is.close();
					os.close();
				}
			} catch(IOException e) {
				DBServerMainGuiStart.guiConsoleOut(e.getMessage());
				stopServer();
				isRunning_ = false;
			}
		}
		
		private void stopServer() {
			dbServer_.dbServerShutdown();
			QasiteLogger.endLogger();
		}
	}
}
