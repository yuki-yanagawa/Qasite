package qaservice.WebServer.model.userRegister;

import qaservice.Common.charcterutil.RandomCharcterArrayCreate;
import qaservice.Common.mailutil.MailSendUtil;
import qaservice.WebServer.model.util.CreateWebServerURI;
import qaservice.WebServer.propreader.ServerPropReader;

public class UserRegisterChekMailSend {
	private final UserRegisterInfoSaved userRegisterInfoSaved;
	private final String url;
	private final int ramdomCharcterLen;
	public UserRegisterChekMailSend(UserRegisterInfoSaved userRegisterInfoSaved, String url) {
		this.userRegisterInfoSaved = userRegisterInfoSaved;
		this.url = CreateWebServerURI.createURI(url);
		this.ramdomCharcterLen = Integer.parseInt(ServerPropReader.getProperties("userAddressCheckURLRandomSeed").toString());
	}

	public boolean sendRegisterCheckMail() {
		char[] charArray = RandomCharcterArrayCreate.createCharcterArray(this.ramdomCharcterLen);
		String tmpRequestPath = new String(charArray);
		String newURl = url + "-" + tmpRequestPath;

		UserRegisterCheckTmpURLLimit.registerUrlToCheckEnableUrl(tmpRequestPath, userRegisterInfoSaved);

		String sendMailText = "このリンクにアクセスし再度ログインをお願いします。"
				+ ""
				+ newURl;
		MailSendUtil mailSendUtil = new MailSendUtil();
		return mailSendUtil.sendMail(userRegisterInfoSaved.getEmailAddr(), sendMailText);
	}
}
