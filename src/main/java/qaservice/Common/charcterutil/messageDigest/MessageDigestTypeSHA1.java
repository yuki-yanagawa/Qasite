package qaservice.Common.charcterutil.messageDigest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.WebServer.propreader.ServerPropReader;

public class MessageDigestTypeSHA1 {
	private static final MessageDigest digest_;
	static {
		String sp = ServerPropReader.getProperties("securityProvider").toString();
		Provider provider = Security.getProvider(sp);
		MessageDigest tmp = null;
		try {
			tmp = MessageDigest.getInstance("SHA-1", provider);
		} catch(NoSuchAlgorithmException e) {
			QasiteLogger.warn("MessageDigestTypeSHA256 init error", e);
			tmp = null;
		}
		digest_ = tmp;
	}

	public static byte[] digest(byte[] input) {
		if(digest_ == null) {
			return null;
		}
		digest_.update(input);
		return digest_.digest();
	}
}
