package qaservice.WebServer.model.userRegister;

import qaservice.WebServer.model.accessDBServer.UserDataLogic;

public class UserRegisterCheckUserNameExist {
	private final UserRegisterInfoSaved userRegisterInfoSaved;
	public UserRegisterCheckUserNameExist(UserRegisterInfoSaved userRegisterInfoSaved) {
		this.userRegisterInfoSaved = userRegisterInfoSaved;
	}

	public boolean isUserNameExist() {
		return UserDataLogic.exisitUserName(userRegisterInfoSaved.getUsername());
	}
}
