package qaservice.Common.charcterutil.messageDigest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;

import qaservice.WebServer.logger.ServerLogger;
import qaservice.WebServer.propreader.ServerPropReader;

public class MessageDigestTypeSHA256 {
	private static final MessageDigest digest_;
	static {
		String sp = ServerPropReader.getProperties("securityProvider").toString();
		String algorithm = ServerPropReader.getProperties("messageDigestAlgorithm").toString();
		Provider provider = Security.getProvider(sp);
		MessageDigest tmp = null;
		try {
			tmp = MessageDigest.getInstance(algorithm, provider);
		} catch(NoSuchAlgorithmException e) {
			ServerLogger.getInstance().warn(e, "MessageDigestTypeSHA256 init error");
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
