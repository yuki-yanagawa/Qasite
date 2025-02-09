package qaservice.WebServer.model.userInfoGet;

import java.util.Map;

import qaservice.WebServer.mainserver.taskhandle.http.request.RequestMessage;

public class CreateUserInfoGetDataObj {
	public enum USERINFO_TYPE {
		USER_INFO,
		USER_INTRODUCTION,
		USER_PICTUREDATA
	}

	public static AbstractUserInfoGetData create(USERINFO_TYPE type, RequestMessage requestMessage, Map<String, Object> addtionalParam) {
		int userid = Integer.parseInt(addtionalParam.get("userid").toString());
		switch(type) {
		case USER_INFO:
			break;
		case USER_INTRODUCTION:
			break;
		case USER_PICTUREDATA:
			return new UserPicturDataGet(userid);
		default:
			break;
		}
		return null;
	}
}
