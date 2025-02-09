package qaservice.WebServer.model.userRegister;

import java.io.Serializable;

import qaservice.WebServer.model.accessDBServer.UserDataLogic;

public class UserRegisterInfoSaved implements Serializable {
	private static final long serialVersionUID = 6294155313283748186L;
	private String emailAddr;
	private String username;
	private String password;
	UserRegisterInfoSaved(String emailAddr, String username, String password) {
		this.emailAddr = emailAddr;
		this.username = username;
		this.password = password;
	}

	String getEmailAddr() {
		return this.emailAddr;
	}

	String getUsername() {
		return this.username;
	}

	public boolean registFromCasheToDB() {
		return UserDataLogic.registUser(username, password, emailAddr);
	}
}
