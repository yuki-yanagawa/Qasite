package qaservice.WebServer.controller;

import java.util.Optional;

import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessage;
import qaservice.WebServer.model.userRegister.UserRegisterCheckAvaliableTmpURL;
import qaservice.WebServer.model.userRegister.UserRegisterCreateMessage;
import qaservice.WebServer.model.userRegister.UserRegisterInfoSaved;

public class UserRegisterFacadeFromMail extends CreateResponseFacade {

	@Override
	ResponseMessage createResponseMessage() {
		UserRegisterCreateMessage userRegisterCreateMessage = new UserRegisterCreateMessage(requestMessage);
		Optional<UserRegisterInfoSaved> userInfoOpt = new UserRegisterCheckAvaliableTmpURL(requestMessage).getUserRegisterInfoSaved();
		if(!userInfoOpt.isPresent()) {
			return userRegisterCreateMessage.createUserRegisterFailResponseMessage();
		}

		UserRegisterInfoSaved userRegisterInfoSaved = userInfoOpt.get();
		userRegisterInfoSaved.registFromCasheToDB();
		return userRegisterCreateMessage.createUserRegistSuccessMessage(userRegisterInfoSaved);
	}

}
