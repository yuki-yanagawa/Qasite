package qaservice.DBServer.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Arrays;

import qaservice.Common.charcterutil.CharUtil;
import qaservice.DBServer.database.H2DBServer;
import qaservice.DBServer.database.IDBServer;
import qaservice.DBServer.keys.KeysOperation;
import qaservice.DBServer.keys.exception.KeySettingException;
import qaservice.DBServer.util.DBServerPropReader;
import qaservice.DBServer.worker.rankcheck.RankCheckWorker;


public class DBServerMain {
	public static void main(String[] args) throws Exception {
		boolean guiStartFlg = args == null ? true : false;
		if(guiStartFlg) DBServerMainGuiStart.guiConsoleOut("DB Server Starting.....");
		//DB Server Stop Key
		try {
			KeysOperation.initialize();
		} catch(KeySettingException e) {
			e.printStackTrace();
			return;
		}
		if(guiStartFlg) DBServerMainGuiStart.guiConsoleOut("Key initialize end....");

		//DB Server Start
		boolean keyGen = Boolean.parseBoolean(DBServerPropReader.getProperties("everyKeyGenerate").toString());
		int port = Integer.parseInt(DBServerPropReader.getProperties("port").toString());
		IDBServer dbServ = new H2DBServer();
		try {
			dbServ.start(port, keyGen);
		} catch(Throwable e) {
			//e.printStackTrace();
			if(guiStartFlg) DBServerMainGuiStart.guiConsoleOut(e.getMessage());
			return;
		}

		//Rank Check Worker Start
		new RankCheckWorker().start();
		if(guiStartFlg) DBServerMainGuiStart.guiConsoleOut("db server start!!!!");
		//Server Strop Operator Reciver
		new DBServerOperationReciver(dbServ).start();

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
				//e.printStackTrace();
				DBServerMainGuiStart.guiConsoleOut(e.getMessage());
				stopServer();
				isRunning_ = false;
			}
		}
		
		private void stopServer() {
			dbServer_.dbServerShutdown();
		}
	}
}
