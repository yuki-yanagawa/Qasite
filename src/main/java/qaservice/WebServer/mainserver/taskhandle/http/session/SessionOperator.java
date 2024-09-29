package qaservice.WebServer.mainserver.taskhandle.http.session;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import qaservice.Common.model.user.UserInfo;
import qaservice.WebServer.logger.ServerLogger;

public class SessionOperator {
	private static Map<String, String> sessionUserMap_ = new HashMap<>();
	//private static Map<String, UserInfo> sessionUserMap_ = new HashMap<>();
	private static Map<String, String> userExistChecker_ = new HashMap<>();
	
	public static synchronized String addSessionMap(String userName) {
		if(userExistChecker_.containsKey(userName)) {
			String sessionKey = userExistChecker_.get(userName);
			userExistChecker_.remove(userName);
			sessionUserMap_.remove(sessionKey);
		}
		String sessionId = createSessionId(userName);
		userExistChecker_.put(userName, sessionId);
		sessionUserMap_.put(sessionId, userName);
		return sessionId;
	}
	
	private static String createSessionId(String userName) {
		byte[] data;
		long randomData = (long)(Math.random() * 1000);
		userName = userName + String.valueOf(System.currentTimeMillis() + randomData);
		try {
			MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
			data = sha256.digest(userName.getBytes());
		} catch(NoSuchAlgorithmException ne) {
			ServerLogger.getInstance().warn(ne, "create session id error");
			data = userName.getBytes();
		}
		//double rondomData = Math.random();
		return Base64.getEncoder().encodeToString(data);
	}

	public static String getUserDataFromSession(String cookie) {
		sessionUserMap_.entrySet().forEach(e -> {
			System.out.println(e.getKey() + " : " + e.getValue());
		});
		String sessionId = getSessionIdFromCookie(cookie);
		return sessionUserMap_.get(sessionId);
	}

	public static String getSessionIdFromCookie(String cookie) {
		String keySessionId = "sessionid=";
		String[] cookies = cookie.split(";");
		if(cookies.length == 1) {
			if(cookies[0].indexOf(keySessionId) < 0) {
				return "";
			}
			return cookies[0].split(keySessionId)[1].trim();
		}
		for(String tmp : cookies) {
			if(tmp.indexOf(keySessionId) >= 0) {
				return tmp.split(keySessionId)[1].trim();
			}
		}
		return "";
	}
}
