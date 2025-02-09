package qaservice.WebServer.model.userRegister;

import java.util.Optional;

import qaservice.WebServer.mainserver.taskhandle.http.request.RequestMessage;

public class UserRegisterCheckAvaliableTmpURL {
	private final String url;
	public UserRegisterCheckAvaliableTmpURL(RequestMessage reqMessage) {
		byte[] tmpUrlBytes = reqMessage.getRequestBodyDataByKey("tmpUrl");
		String tmpUrl = new String(tmpUrlBytes);
		this.url = new String(tmpUrlBytes);
	}

	public Optional<UserRegisterInfoSaved> getUserRegisterInfoSaved() {
		return UserRegisterCheckTmpURLLimit.getUserInfoCashed(url);
	}
}
