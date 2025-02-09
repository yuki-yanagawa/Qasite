package qaservice.WebServer.mainserver.taskhandle.http.response;

import qaservice.WebServer.mainserver.taskhandle.http.session.SessionOperator;

public class ResponseMessageFactory {
	public static ResponseMessage createResponseMessageTypeJson(ResponseContentType type, byte[] body, String httpProtocol, boolean isCompressed) {
		return new ResponseMessageTypeJson(type, body, httpProtocol, isCompressed);
	}

	public static ResponseMessage createResponseMessageThenUserRegist(ResponseContentType type, byte[] body, String httpProtocol, boolean isCompressed, String username) {
		return new ResponseMessageTypeJson(type, body, httpProtocol, isCompressed).setSessionRegist(() -> SessionOperator.addSessionMap(username));
	}
}
