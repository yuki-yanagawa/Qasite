package qaservice.WebServer.model.userUpdate;

import java.util.HashMap;
import java.util.Map;

import qaservice.WebServer.mainserver.taskhandle.http.request.RequestMessage;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseContentType;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessage;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessageFactory;
import qaservice.WebServer.model.util.CreateJsonData;

public class UserUpdateCreateMessage {
	private final RequestMessage requestMessage;
	public UserUpdateCreateMessage(RequestMessage requestMessage) {
		this.requestMessage = requestMessage;
	}

	public ResponseMessage createUserUpdateFailResponseMessage() {
		Map<String, String> retMap = new HashMap<>();
		retMap.put("result", "fail");
		byte[] retData = CreateJsonData.createJsonData(retMap);
		return ResponseMessageFactory.createResponseMessageTypeJson(ResponseContentType.JSON, retData, requestMessage.getHttpProtocol(), false);
	}

	public ResponseMessage createUserUpdateSuccessResponseMessage() {
		Map<String, String> retMap = new HashMap<>();
		retMap.put("result", "success");
		byte[] retData = CreateJsonData.createJsonData(retMap);
		return ResponseMessageFactory.createResponseMessageTypeJson(ResponseContentType.JSON, retData, requestMessage.getHttpProtocol(), false);
	}
}
