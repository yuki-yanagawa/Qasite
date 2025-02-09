package qaservice.WebServer.model.userInfoGet;

import java.util.HashMap;
import java.util.Map;

import qaservice.WebServer.mainserver.taskhandle.http.request.RequestMessage;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseContentType;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessage;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessageFactory;
import qaservice.WebServer.model.util.CreateJsonData;

public class UserInfoGetCreateMessage {
	private final RequestMessage requestMessage;
	public UserInfoGetCreateMessage(RequestMessage requestMessage) {
		this.requestMessage = requestMessage;
	}

	public ResponseMessage createUserInfoGetPictureResponseMessage(String pictureData) {
		Map<String, Object> retMap = new HashMap<>();
		retMap.put("result", pictureData);
		byte[] retData = CreateJsonData.createJsonData(retMap);
		return ResponseMessageFactory.createResponseMessageTypeJson(ResponseContentType.JSON, retData, requestMessage.getHttpProtocol(), false);
	}

	public ResponseMessage createUserInfoGetSuccessResponseMessage() {
		Map<String, String> retMap = new HashMap<>();
		retMap.put("result", "success");
		byte[] retData = CreateJsonData.createJsonData(retMap);
		return ResponseMessageFactory.createResponseMessageTypeJson(ResponseContentType.JSON, retData, requestMessage.getHttpProtocol(), false);
	}

	public ResponseMessage createUserInfoGetFailResponseMessage() {
		Map<String, String> retMap = new HashMap<>();
		retMap.put("result", "fail");
		byte[] retData = CreateJsonData.createJsonData(retMap);
		return ResponseMessageFactory.createResponseMessageTypeJson(ResponseContentType.JSON, retData, requestMessage.getHttpProtocol(), false);
	}

}
