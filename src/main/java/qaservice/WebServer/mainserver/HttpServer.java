package qaservice.WebServer.mainserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.WebServer.propreader.ServerPropKey;
import qaservice.WebServer.propreader.ServerPropReader;

class HttpServer implements IServer {
	private int port_;
	private ServerSocket svrSock_;
	private boolean acceptExec_;
	HttpServer(int port) throws IOException {
		port_ = port;
		svrSock_ = new ServerSocket();
		acceptExec_ = true;
		String hostMode = ServerPropReader.getProperties(ServerPropKey.ServerHost.getKey()).toString();
		boolean isLocalHost = true;
		if(!"LOCALHOST".equals(hostMode.toUpperCase())) {
			isLocalHost = false;
		}
		if(isLocalHost) {
			svrSock_.setReuseAddress(true);
			svrSock_.bind(new InetSocketAddress("localhost", port_));
		} else {
			svrSock_.setReuseAddress(true);
			svrSock_.bind(new InetSocketAddress(port_));
		}
	}
	
	private void closeOpe() {
		if(svrSock_ != null) {
			try {
				svrSock_.close();
			} catch(IOException e) {
				QasiteLogger.warn("ServerSocket close operatione error", e);
			}
		}
	}

	@Override
	public Socket awaitRequest() throws IOException {
		synchronized (svrSock_) {
			return svrSock_.accept();
		}
	}

	@Override
	public void killServer() {
		this.closeOpe();
	}
}
