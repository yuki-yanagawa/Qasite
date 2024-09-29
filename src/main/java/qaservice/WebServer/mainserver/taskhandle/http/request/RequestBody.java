package qaservice.WebServer.mainserver.taskhandle.http.request;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import qaservice.Common.charcterutil.CharUtil;
import qaservice.WebServer.logger.ServerLogger;

class RequestBody {
	private static enum BodyType {
		JSON("application/json");
		private String contentType_;
		private BodyType(String contentType) {
			contentType_ = contentType;
		}
	}
	private byte[] bodyData_;
	private BodyType bodyType_;
	private String casheBodyData_;
	private Map<String, byte[]> casheValueMap_;
	RequestBody(byte[] bodyData, String type) {
		bodyData_ = bodyData;
		bodyType_ = createbodyType(type);
		casheValueMap_ = new HashMap<>();
	}
	
	private BodyType createbodyType(String type) {
		for(BodyType b : BodyType.values()) {
			if(b.contentType_.equals(type)) {
				return b;
			}
		}
		return BodyType.JSON;
	}
	
	byte[] getBase64DecodeValueByKey(String key) {
//		byte[] keyBytes = key.getBytes(CharUtil.getCharset());
//		for(int i = 0; i < bodyData_.length; i++) {
//			if(keyBytes[0] == bodyData_[i]) {
//				//search length check
//				if(i + keyBytes.length >= bodyData_.length) {
//					break;
//				}
//				//search logic
//				boolean isKey = true;
//				for(int j = 1; j < keyBytes.length; j++) {
//					if(keyBytes[j] != bodyData_[i + j]) {
//						isKey = false;
//						break;
//					}
//				}
//				if(isKey) {
//					if(bodyData_[i + keyBytes.length] == ) {
//						return Arrays.co
//					}
//				}
//			}
//		}
//		return null;
		
		if(casheValueMap_.containsKey(key)) {
			return casheValueMap_.get(key);
		}
		
		if(casheBodyData_ == null) {
			casheBodyData_ = new String(bodyData_, CharUtil.getCharset());
		}
		String[] tmpLine = casheBodyData_.split(key + '=');
		if(tmpLine.length != 2) {
			ServerLogger.getInstance().warn("get value error key="+key);
			return null;
		}
		int endIndex = tmpLine[1].indexOf('&');
		if(endIndex == -1) {
			endIndex = tmpLine[1].length();
		}
		
		String urlEncodeData = tmpLine[1].substring(0, endIndex);
		String base64EncodeData = null;
		byte[] retByteData = null;
		try {
			base64EncodeData = URLDecoder.decode(urlEncodeData, CharUtil.getCharset().toString());
			retByteData = Base64.getDecoder().decode(base64EncodeData);
		} catch(UnsupportedEncodingException e) {
			ServerLogger.getInstance().warn(e, "Decoder Exception");
			return null;
		}
		//System.out.println("key = " + key + " & value=" + new String(retByteData));
		casheValueMap_.put(key, retByteData);
		return retByteData;
	}

	byte[] getValueByKey(String key) {
		return getValueByKey(key, false);
	}

	byte[] getValueByKey(String key, boolean isBase64Decode) {
		if(casheValueMap_.containsKey(key)) {
			return casheValueMap_.get(key);
		}
		
		if(casheBodyData_ == null) {
			casheBodyData_ = new String(bodyData_, CharUtil.getCharset());
		}
		String[] tmpLine = casheBodyData_.split(key + '=');
		if(tmpLine.length != 2) {
			ServerLogger.getInstance().warn("get value error key="+key);
			return null;
		}
		int endIndex = tmpLine[1].indexOf('&');
		if(endIndex == -1) {
			endIndex = tmpLine[1].length();
		}
		
		String urlEncodeData = tmpLine[1].substring(0, endIndex);
		byte[] retByteData = null;
		try {
			if(isBase64Decode) {
				String base64EncodeData = URLDecoder.decode(urlEncodeData, CharUtil.getCharset().toString());
				retByteData = Base64.getDecoder().decode(base64EncodeData);
			} else {
				retByteData = URLDecoder.decode(urlEncodeData, CharUtil.getCharset().toString()).getBytes();
			}
		} catch(UnsupportedEncodingException e) {
			ServerLogger.getInstance().warn(e, "Decoder Exception");
			return null;
		}
		//System.out.println("key = " + key + " & value=" + new String(retByteData));
		casheValueMap_.put(key, retByteData);
		return retByteData;
	}
}
