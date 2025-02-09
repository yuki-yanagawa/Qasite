package qaservice.WebServer.model.userInfoGet;

import qaservice.WebServer.model.accessDBServer.UserDataLogic;

public class UserPicturDataGet extends AbstractUserInfoGetData {
	UserPicturDataGet(int userid) {
		super(userid);
	}

	@SuppressWarnings("unchecked")
	@Override
	public byte[] getUserInfoTargetData() {
		return UserDataLogic.spGetUserPictureData(userid);
	}
}
