package qaservice.WebServer.model.userUpdate;

import qaservice.Common.charcterutil.CharUtil;
import qaservice.WebServer.mainserver.taskhandle.http.request.RequestMessage;

public class UserUpdateInfoCollectFromRequestParameter {
	public static UserUpdateInfoSaved createUserUpdateInfo(RequestMessage requestMessage) {
		byte[] userIdByte = requestMessage.getRequestBodyDataByKey("userId");
		int userId = Integer.parseInt(new String(userIdByte, CharUtil.getAjaxCharset()));
		byte[] usernameByte = requestMessage.getRequestBodyDataByKey("username");
		String username = null;
		if(usernameByte != null) {
			username = new String(usernameByte, CharUtil.getAjaxCharset());
		}
		byte[] introductionTextByte = requestMessage.getRequestBodyDataByKey("introductionText");
		byte[] pictureByte = requestMessage.getRequestBodyDataByKey("picture");
		return new UserUpdateInfoSaved(userId, username, introductionTextByte, pictureByte);
	}
}
