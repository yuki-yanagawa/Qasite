package qaservice.WebServer.mainserver.taskhandle.http.request.exception;

import qaservice.WebServer.mainserver.taskhandle.http.response.ResonseStatusLine;

public class HttpRouterDelegateMethodCallError extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8482775290832699453L;
	private final ResonseStatusLine statusLine_;
	private Throwable throwable_;
	private String errorMessage_;
	private String callingMethod_;
	public HttpRouterDelegateMethodCallError(final ResonseStatusLine statusLine, Throwable throwable, String errorMessage, String callingMethod) {
		statusLine_ = statusLine;
		throwable_ = throwable;
		errorMessage_ = errorMessage;
		callingMethod_ = callingMethod;
	}
	
	public ResonseStatusLine getStatusLine() {
		return statusLine_;
	}
	
	@Override
	public String getMessage() {
		String message = "";
		if(throwable_ != null) {
			message = throwable_.getMessage();
		}
		message += "/" + errorMessage_ + "[ " + callingMethod_ + " ]";
		return message;
	}
}
