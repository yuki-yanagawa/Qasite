package qaservice.WebServer.mainserver;

import java.io.IOException;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.WebServer.mainserver.worker.HttpHandlerWorkerOperation;
import qaservice.WebServer.propreader.ServerPropKey;
import qaservice.WebServer.propreader.ServerPropReader;

public class ServerOperator {
	private static IServer httpServer_;
	private static int WORKER_THREAD_COUNT = 5;
	private static String serverName_;
	public static boolean mainServerStart(int port) {
		if(!ServerPropReader.isReadPropertiesSuccess()) {
			System.out.println("properties read error. system exit.");
			System.exit(-1);
		}
		serverName_ = ServerPropReader.getProperties(ServerPropKey.ServerName.getKey()).toString();
//		try {
//			QasiteLogger.startLogger(serverName_);
//		} catch(Exception e) {
//			e.printStackTrace();
//			return false;
//		}
		WORKER_THREAD_COUNT = Integer.parseInt(ServerPropReader.getProperties(ServerPropKey.ServerWorkerThreadCount.getKey()).toString());
		HttpHandlerWorkerOperation.createWorkerThreadPool(WORKER_THREAD_COUNT);
		boolean isTlsMode = Boolean.parseBoolean(ServerPropReader.getProperties(ServerPropKey.ServerTLS.getKey()).toString());
		try {
			if(isTlsMode) {
				httpServer_ = new HttpsServer(port);
			} else {
				httpServer_ = new HttpServer(port);
			}
			HttpHandlerWorkerOperation.registerAwaitReciverSocket(httpServer_);
		} catch(IOException e) {
			QasiteLogger.warn("main server Start error", e);
			return false;
		} catch(Exception e) {
			QasiteLogger.warn("main server Start error", e);
			return false;
		}
		return true;
	}

	public static void mainServerStop() {
		QasiteLogger.endLogger();
		HttpHandlerWorkerOperation.deleteWorkerThreadPool();
		httpServer_.killServer();
	}
	
	public static String getServerName() {
		return serverName_;
	}
}
