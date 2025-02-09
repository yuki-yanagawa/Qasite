package qaservice.WebServer.mainserver.taskhandle.http.request.exception;

import qaservice.Common.Logger.QasiteLogger;

public class HttpRequestHandlingException extends Exception {
	public HttpRequestHandlingException(String mess) {
		super(mess);
		QasiteLogger.warn("Http Request Handeling Error : " + mess);
	}
}
