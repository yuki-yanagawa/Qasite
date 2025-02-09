package qaservice.WebServer.controller;

import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessage;
import qaservice.WebServer.model.userRegister.UserRegisterCreateMessage;
import qaservice.WebServer.model.userRegister.UserRegisterInfoSaved;
import qaservice.WebServer.model.userRegister.UserRegisterInfoCollectFromRequestParameter;
import qaservice.WebServer.model.userRegister.UserRegisterCheckMailExist;
import qaservice.WebServer.model.userRegister.UserRegisterCheckUserNameExist;
import qaservice.WebServer.model.userRegister.UserRegisterChekMailSend;
import qaservice.WebServer.propreader.ServerPropReader;

class UserRegisterFacade extends CreateResponseFacade {

	@Override
	ResponseMessage createResponseMessage() {
		UserRegisterCreateMessage userRegisterCreateMessage = new UserRegisterCreateMessage(requestMessage);
		UserRegisterInfoSaved userRegisterInfoSaved = UserRegisterInfoCollectFromRequestParameter.createUserRegisterInfo(requestMessage);

		//Mail Exist Check
		UserRegisterCheckMailExist userRegisterMailExist = new UserRegisterCheckMailExist(userRegisterInfoSaved);
		if(userRegisterMailExist.isMailExist()) {
			return userRegisterCreateMessage.createMailExistResponseMessage();
		}

		//UserName Exist Check
		UserRegisterCheckUserNameExist userNameExist = new UserRegisterCheckUserNameExist(userRegisterInfoSaved);
		if(userNameExist.isUserNameExist()) {
			return userRegisterCreateMessage.createUserNameExistResponseMessage();
		}

		//Need Mail Response Check
		boolean sendCheckMail = Boolean.parseBoolean(ServerPropReader.getProperties("userRegisterSendMail").toString());
		if(sendCheckMail) {
			UserRegisterChekMailSend userRegisterChekMailSend = new UserRegisterChekMailSend(userRegisterInfoSaved, "/userRegisterFromMail");
			boolean sendMailResult = userRegisterChekMailSend.sendRegisterCheckMail();
			return userRegisterCreateMessage.createNotifySendCheckMailResponseMessage(sendMailResult);
		}

		//Register User
		userRegisterInfoSaved.registFromCasheToDB();
		return userRegisterCreateMessage.createUserRegistSuccessMessage(userRegisterInfoSaved);
	}
}
