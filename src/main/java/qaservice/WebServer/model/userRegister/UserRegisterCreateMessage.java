package qaservice.WebServer.model.userRegister;

import java.util.HashMap;
import java.util.Map;

import qaservice.WebServer.mainserver.taskhandle.http.request.RequestMessage;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseContentType;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessage;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessageFactory;
import qaservice.WebServer.model.util.CreateJsonData;

public class UserRegisterCreateMessage {
	private final RequestMessage requestMessage;
	public UserRegisterCreateMessage(RequestMessage requestMessage) {
		this.requestMessage = requestMessage;
	}
	public ResponseMessage createMailExistResponseMessage() {
		Map<String, String> retMap = new HashMap<>();
		retMap.put("result", "mailExist");
		byte[] retData = CreateJsonData.createJsonData(retMap);
		return ResponseMessageFactory.createResponseMessageTypeJson(ResponseContentType.JSON, retData, requestMessage.getHttpProtocol(), false);
	}

	public ResponseMessage createUserNameExistResponseMessage() {
		Map<String, String> retMap = new HashMap<>();
		retMap.put("result", "usernameExist");
		byte[] retData = CreateJsonData.createJsonData(retMap);
		return ResponseMessageFactory.createResponseMessageTypeJson(ResponseContentType.JSON, retData, requestMessage.getHttpProtocol(), false);
	}

	public ResponseMessage createNotifySendCheckMailResponseMessage(boolean result) {
		Map<String, String> retMap = new HashMap<>();
		retMap.put("result", "sendCheckMail");
		if(result) {
			retMap.put("sendCheckMail", "success");
		} else {
			retMap.put("sendCheckMail", "fail");
		}
		byte[] retData = CreateJsonData.createJsonData(retMap);
		return ResponseMessageFactory.createResponseMessageTypeJson(ResponseContentType.JSON, retData, requestMessage.getHttpProtocol(), false);
	}

	public ResponseMessage createUserRegisterFailResponseMessage() {
		Map<String, String> retMap = new HashMap<>();
		retMap.put("result", "fail");
		byte[] retData = CreateJsonData.createJsonData(retMap);
		return ResponseMessageFactory.createResponseMessageTypeJson(ResponseContentType.JSON, retData, requestMessage.getHttpProtocol(), false);
	}

	public ResponseMessage createUserRegistSuccessMessage(UserRegisterInfoSaved userRegisterInfoSaved) {
		Map<String, String> retMap = new HashMap<>();
		retMap.put("result", "success");
		byte[] retData = CreateJsonData.createJsonData(retMap);
		return ResponseMessageFactory.createResponseMessageThenUserRegist(
				ResponseContentType.JSON, retData, requestMessage.getHttpProtocol(), false, userRegisterInfoSaved.getUsername());
	}
}
