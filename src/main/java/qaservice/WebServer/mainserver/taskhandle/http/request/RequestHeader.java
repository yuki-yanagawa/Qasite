package qaservice.WebServer.mainserver.taskhandle.http.request;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpRequestHandlingException;

class RequestHeader {

	private Map<RequsetHeaderType, String> requestHeaderData_ = new HashMap<>();
	private Pattern separaterPattern = Pattern.compile("(.*?)\\s*\\:\\s*(.*)");
	RequestHeader(String[] requestHeaders) throws HttpRequestHandlingException {
		Map<String, RequsetHeaderType> keyMap = getKeyResponser();
 		for(int i = 1; i < requestHeaders.length; i++) {
			if("".equals(requestHeaders[i])) {
				break;
			}
			String[] keyAndValue = separateKeyAndValue(requestHeaders[i]);
			
			if(!keyMap.containsKey(keyAndValue[0].toUpperCase())) {
				continue;
			}
			requestHeaderData_.put(keyMap.get(keyAndValue[0].toUpperCase()), keyAndValue[1]);
		}
	}
	
	private Map<String, RequsetHeaderType> getKeyResponser() {
		Map<String, RequsetHeaderType>  retMap = new HashMap<>();
		for(RequsetHeaderType r : RequsetHeaderType.values()) {
			retMap.put(r.getName().toUpperCase(), r);
		}
		return retMap;
	}
	
	private String[] separateKeyAndValue(String requestHeader) throws HttpRequestHandlingException {
		Matcher m = separaterPattern.matcher(requestHeader);
		if(m.find() && m.groupCount() == 2) {
			return new String[] {m.group(1), m.group(2)};
		}
		throw new HttpRequestHandlingException("requset header analyze error");
	}
	
	String getReuestHeaderValue(RequsetHeaderType type) {
		String retData = requestHeaderData_.get(type);
		if(retData == null) {
			retData = "";
		}
		return retData;
	}
	
//	private RequsetHeaderType getRequsetHeaderType(String key) {
//		String keyUpper = key.toUpperCase();
//		for(RequsetHeaderType r : RequsetHeaderType.values()) {
//			if(r.getName().equals(keyUpper)) {
//				return r;
//			}
//		}
//		return null;
//	}
}
