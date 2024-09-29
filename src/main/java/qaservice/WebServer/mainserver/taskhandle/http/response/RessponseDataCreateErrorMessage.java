package qaservice.WebServer.mainserver.taskhandle.http.response;

public class RessponseDataCreateErrorMessage {
	private Throwable throwable_;
	private String errorMessage_;
	private String callingMethod_;
	public RessponseDataCreateErrorMessage(Throwable throwable, String errorMessage, String callingMethod) {
		throwable_ = throwable;
		errorMessage_ = errorMessage;
		callingMethod_ = callingMethod;
	}
	
	public String message() {
		String message = "";
		if(throwable_ != null) {
			message = throwable_.getMessage();
		}
		message += "/" + errorMessage_ + "[ " + callingMethod_ + " ]";
		return message;
	}
}
