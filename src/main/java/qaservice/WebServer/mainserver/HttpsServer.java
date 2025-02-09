package qaservice.WebServer.mainserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.WebServer.propreader.ServerPropKey;
import qaservice.WebServer.propreader.ServerPropReader;

public class HttpsServer implements IServer {
	private int port_;
	private SSLServerSocket svrSock_;
	private boolean acceptExec_;
	HttpsServer(int port) throws Exception {
		port_ = port;
		svrSock_ = createServerSocket();
		//svrSock_.setWantClientAuth(false);
		svrSock_.setNeedClientAuth(false);
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

	private SSLServerSocket createServerSocket() throws Exception {
		String pass = (String)ServerPropReader.getProperties("serverKeyPass");
		KeyStore ks = KeyStore.getInstance("PKCS12");
		try(FileInputStream fis = new FileInputStream("keydir/server.p12")) {
			ks.load(fis, pass.toCharArray());
		}
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks, pass.toCharArray());

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(kmf.getKeyManagers(), null, null);

		return (SSLServerSocket)sslContext.getServerSocketFactory().createServerSocket();
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
