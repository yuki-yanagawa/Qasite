package qaservice.WebServer.controller;

import java.util.HashMap;
import java.util.Map;

import qaservice.WebServer.mainserver.taskhandle.http.request.RequestMessage;
import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpRouterDelegateMethodCallError;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessage;

public abstract class CreateResponseFacade {
	public static class NotUriControllerExecption extends RuntimeException {
		private static final long serialVersionUID = 44440126089706821L;
	}
	private static Map<String, CreateResponseFacade> conrollerFacadeMap = new HashMap<>();
	static {
		conrollerFacadeMap.put("/userRegister", new UserRegisterFacade());
		conrollerFacadeMap.put("/userRegisterFromMail", new UserRegisterFacadeFromMail());
		conrollerFacadeMap.put("/updateUserInfo", new UserUpdateFacade());
		conrollerFacadeMap.put("/getUserPictureData", new UserInfoGetPictureData());
	}
	protected RequestMessage requestMessage;
	protected Map<String, Object> addtionalParam;

	public static ResponseMessage response(String urlKey, RequestMessage requestMessage) throws NotUriControllerExecption {
		return response(urlKey, requestMessage, new HashMap<>());
	}
	public static ResponseMessage response(String urlKey, RequestMessage requestMessage, Map<String, Object> additonalParam) throws NotUriControllerExecption {
		CreateResponseFacade controllerObj = conrollerFacadeMap.get(urlKey);
		if(controllerObj == null) {
			throw new NotUriControllerExecption();
		}
		controllerObj.setRequestMessage(requestMessage);
		controllerObj.setAddtionalParam(additonalParam);
		return controllerObj.createResponseMessage();
	}

	private void setRequestMessage(RequestMessage requestMessage) {
		this.requestMessage = requestMessage;
	}

	private void setAddtionalParam(Map<String, Object> addtionalParam) {
		this.addtionalParam = addtionalParam;
	}

	abstract ResponseMessage createResponseMessage();
}
