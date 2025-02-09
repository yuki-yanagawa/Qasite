package qaservice.WebServer.controller;

import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessage;
import qaservice.WebServer.model.userUpdate.UserUpdateCreateMessage;
import qaservice.WebServer.model.userUpdate.UserUpdateInfoCollectFromRequestParameter;
import qaservice.WebServer.model.userUpdate.UserUpdateInfoSaved;

public class UserUpdateFacade extends CreateResponseFacade {

	@Override
	ResponseMessage createResponseMessage() {
		UserUpdateInfoSaved userUpdateInfoSaved = UserUpdateInfoCollectFromRequestParameter.createUserUpdateInfo(requestMessage);
		boolean result = userUpdateInfoSaved.updateUserIntrodution() && userUpdateInfoSaved.updateUserPictureData();
		if(result) {
			return new UserUpdateCreateMessage(requestMessage).createUserUpdateSuccessResponseMessage();
		}
		return new UserUpdateCreateMessage(requestMessage).createUserUpdateFailResponseMessage();
	}

}
