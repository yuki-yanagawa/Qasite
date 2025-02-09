package qaservice.WebServer.controller;

import qaservice.Common.charcterutil.CharUtil;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessage;
import qaservice.WebServer.model.userInfoGet.AbstractUserInfoGetData;
import qaservice.WebServer.model.userInfoGet.CreateUserInfoGetDataObj;
import qaservice.WebServer.model.userInfoGet.CreateUserInfoGetDataObj.USERINFO_TYPE;
import qaservice.WebServer.model.userInfoGet.UserInfoGetCreateMessage;

public class UserInfoGetPictureData extends CreateResponseFacade{

	@Override
	ResponseMessage createResponseMessage() {
		UserInfoGetCreateMessage userInfoGetCreateMessage = new UserInfoGetCreateMessage(requestMessage);
		AbstractUserInfoGetData userInfoGetData = CreateUserInfoGetDataObj.create(USERINFO_TYPE.USER_PICTUREDATA, requestMessage, addtionalParam);
		byte[] pictureData = userInfoGetData.getUserInfoTargetData();
		if(pictureData == null || pictureData.length == 0) {
			return userInfoGetCreateMessage.createUserInfoGetSuccessResponseMessage();
		}
		String pictureDataStr = new String(pictureData, CharUtil.getCharset());
		return userInfoGetCreateMessage.createUserInfoGetPictureResponseMessage(pictureDataStr);
	}

}
