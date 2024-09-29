package qaservice.WebServer.mainserver.taskhandle.http.request.exception;

import qaservice.WebServer.logger.ServerLogger;

public class HttpRequestHandlingException extends Exception {
	public HttpRequestHandlingException(String mess) {
		super(mess);
		ServerLogger.getInstance().warn("Http Request Handeling Error : " + mess);
	}
}
