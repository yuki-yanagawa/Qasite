package qaservice.WebServer.main;

import qaservice.WebServer.dbconnect.DBConnectionOperation;
import qaservice.WebServer.logger.ServerLogger;
import qaservice.WebServer.mainserver.ServerOperator;
import qaservice.WebServer.propreader.ServerPropKey;
import qaservice.WebServer.propreader.ServerPropReader;

public class WebServerStartingOperation {
	public static  void serverLoggerSettingOn() {
		//Server logger create Instance
		ServerLogger.getInstance().appLog("+++++++++ Web Server Start ++++++++++");
		ServerLogger.getInstance().appLog("     *   *                 *    *          ");
		ServerLogger.getInstance().appLog("     *****                 ******          ");
		ServerLogger.getInstance().appLog("                                           ");
		ServerLogger.getInstance().appLog("           ***************                 ");
	}

	static void autoStart() {
		String portNo = ServerPropReader.getProperties(ServerPropKey.ServerDefaultPortSetting.getKey()).toString();
		int port = -1;
		try {
			port = Integer.parseInt(portNo);
		} catch(NumberFormatException e) {
			e.printStackTrace();
			port = -1;
		}
		if(port == -1) port = 9090;
		boolean serverStartResult = ServerOperator.mainServerStart(port);
		if(!serverStartResult) {
			
		}
		String dbPath = ServerPropReader.getProperties(ServerPropKey.DBServerConnetionPath.getKey()).toString();
		DBConnectionOperation.getInstance().createConnectionPool(dbPath);
		timeSleep(1000);
	}

	private static void timeSleep(int msec) {
		try {
			Thread.sleep(msec);
		} catch(InterruptedException e) {
			
		}
	}
}
