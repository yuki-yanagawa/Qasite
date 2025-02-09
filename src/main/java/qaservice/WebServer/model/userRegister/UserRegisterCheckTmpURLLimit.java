package qaservice.WebServer.model.userRegister;

import java.util.Base64;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import qaservice.Common.charcterutil.messageDigest.MessageDigestTypeSHA256;
import qaservice.WebServer.propreader.ServerPropReader;

public class UserRegisterCheckTmpURLLimit {
	private static ConcurrentHashMap<String, Long> urlTimeLimitMap = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, UserRegisterInfoSaved> urlUserInfoMap = new ConcurrentHashMap<>();
	private static int timeOutThreshold = Integer.parseInt(ServerPropReader.getProperties("EnableUserRegisterTmpUrlTimeSecond").toString());

	static void registerUrlToCheckEnableUrl(String url, UserRegisterInfoSaved userRegisterInfoSaved) {
		unNeedMonitoringUrlCheck();
		startMonitoringToCheckEnableUrl(url, userRegisterInfoSaved);
	}

	private static void unNeedMonitoringUrlCheck() {
		Iterator<String> keys = urlTimeLimitMap.keySet().iterator();
		long currentTime = System.nanoTime();
		long checkLimitOverTime = timeOutThreshold * 1000 * 1000 * 1000;
		while(keys.hasNext()) {
			String key = keys.next();
			long monitorStartTime = urlTimeLimitMap.get(key);
			if((currentTime - monitorStartTime) > checkLimitOverTime) {
				keys.remove();
				urlUserInfoMap.remove(key);
			}
		}
	}

	private static void startMonitoringToCheckEnableUrl(String url, UserRegisterInfoSaved userRegisterInfoSaved) {
		String key = createUrlTimeLimitMapKey(url);
		long savedStartTime = System.nanoTime();
		urlTimeLimitMap.put(key, savedStartTime);
		urlUserInfoMap.put(key, userRegisterInfoSaved);
	}

	static Optional<UserRegisterInfoSaved> getUserInfoCashed(String url) {
		String key = createUrlTimeLimitMapKey(url);
		if(!urlTimeLimitMap.containsKey(key)) {
			return Optional.empty();
		}

		long currentTime = System.nanoTime();
		long checkLimitOverTime = timeOutThreshold * 1000L * 1000L * 1000L;
		long monitorStartTime = urlTimeLimitMap.remove(key);

		boolean enableUrl = true;
		if((currentTime - monitorStartTime) > checkLimitOverTime) {
			enableUrl = false;
		}
		Optional<UserRegisterInfoSaved> retTmp = Optional.of(urlUserInfoMap.remove(key));
		if(!enableUrl) {
			return Optional.empty();
		}
		return retTmp;
	}

	private static String createUrlTimeLimitMapKey(String url) {
		byte[] sha256Bytes = MessageDigestTypeSHA256.digest(url.getBytes());
		return Base64.getEncoder().encodeToString(sha256Bytes);
	}
}
