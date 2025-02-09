package qaservice.WebServer.model.userRegister;

import java.sql.SQLException;

import qaservice.WebServer.model.accessDBServer.UserDataLogic;

public class UserRegisterCheckMailExist {
	private final UserRegisterInfoSaved userRegisterInfoSaved;
	public UserRegisterCheckMailExist(UserRegisterInfoSaved userRegisterInfoSaved) {
		this.userRegisterInfoSaved = userRegisterInfoSaved;
	}

	public boolean isMailExist() {
		boolean isMailExist = false;
		try {
			isMailExist = UserDataLogic.existEmailAddress(userRegisterInfoSaved.getEmailAddr());
		} catch(SQLException e) {
			isMailExist = false;
		}
		return isMailExist;
	}
}
