package qaservice.WebServer.model.userRegister;

import java.util.Base64;

import qaservice.Common.charcterutil.CharUtil;
import qaservice.WebServer.mainserver.taskhandle.http.request.RequestMessage;

public class UserRegisterInfoCollectFromRequestParameter {
	public static UserRegisterInfoSaved createUserRegisterInfo(RequestMessage requestMessage) {
		byte[] emailTextBytes = requestMessage.getRequestBodyDataByKey("mailText");
		String emailAddr = new String(emailTextBytes, CharUtil.getAjaxCharset());

		byte[] nameBytes = requestMessage.getRequestBodyDataByKey("userName");
		String usernameByBase64 = new String(nameBytes, CharUtil.getAjaxCharset());
		String username = new String(Base64.getDecoder().decode(usernameByBase64), CharUtil.getAjaxCharset());

		byte[] passwordBytes = requestMessage.getRequestBodyDataByKey("digest");
		String password = new String(passwordBytes, CharUtil.getAjaxCharset());

		return new UserRegisterInfoSaved(emailAddr, username, password);
	}
}
