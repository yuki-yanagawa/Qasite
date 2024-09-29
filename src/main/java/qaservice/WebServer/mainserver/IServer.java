package qaservice.WebServer.mainserver;

import java.net.Socket;

public interface IServer {
	public Socket awaitRequest() throws Throwable;
	public void killServer();
}
