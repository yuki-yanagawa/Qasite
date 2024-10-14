package qaservice.WebServer.mainserver;

import java.io.IOException;

import qaservice.WebServer.logger.ServerLogger;
import qaservice.WebServer.mainserver.worker.HttpHandlerWorkerOperation;
import qaservice.WebServer.propreader.ServerPropKey;
import qaservice.WebServer.propreader.ServerPropReader;

public class ServerOperator {
	private static IServer httpServer_;
	private static int WORKER_THREAD_COUNT = 5;
	private static String serverName_;
	public static boolean mainServerStart(int port) {
		serverName_ = ServerPropReader.getProperties(ServerPropKey.ServerName.getKey()).toString();
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
			ServerLogger.getInstance().warn("http server setting error..");
			ServerLogger.getInstance().warn(e.getMessage());
			return false;
		} catch(Exception e) {
			ServerLogger.getInstance().warn("http server setting error..");
			ServerLogger.getInstance().warn(e.getMessage());
			return false;
		}
		return true;
	}

	public static void mainServerStop() {
		HttpHandlerWorkerOperation.deleteWorkerThreadPool();
		httpServer_.killServer();
	}
	
	public static String getServerName() {
		return serverName_;
	}
}
