package qaservice.WebServer.mainserver.taskhandle.http.request;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.StyleContext.SmallAttributeSet;

import qaservice.Common.charcterutil.CharUtil;
import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpRequestHandlingException;

public class RequestMessage {
	private static final String HTTP_PROTOCOL_SEPARATOR = "\r\n";
	private RequestLine requestLine_;
	private RequestHeader requestHeader_;
	private RequestBody requestBody_;
	private Map<String, String> queryParameter_;
	
	public static RequestMessage analyzeRequestMesage(byte[] requsetRawData) throws HttpRequestHandlingException {
		return new RequestMessage(requsetRawData);
	}
	
	RequestMessage(byte[] requsetRawData) throws HttpRequestHandlingException {
		String requestDataStr = new String(requsetRawData, CharUtil.getCharset());
		//Delete!!!
		System.out.println(requestDataStr);
		//Delete!!!
		String[] requestLine = requestDataStr.split(HTTP_PROTOCOL_SEPARATOR);
		requestLine_ = new RequestLine(requestLine[0]);
		requestHeader_ = new RequestHeader(requestLine);
		String contentLengthStr = requestHeader_.getReuestHeaderValue(RequsetHeaderType.ContentLength);
		if("".equals(contentLengthStr)) {
			return;
		}
		int contentLength = 0;
		try {
			contentLength = Integer.parseInt(contentLengthStr);
		} catch(NumberFormatException e) {
			throw new HttpRequestHandlingException("get content length error");
		}
		if(requsetRawData.length <= contentLength) {
			return;
		}
		byte[] bodyRawData = Arrays.copyOfRange(requsetRawData, requsetRawData.length - contentLength, requsetRawData.length);
		if(!"".equals(requestHeader_.getReuestHeaderValue(RequsetHeaderType.ContentType))) {
			requestBody_ = new RequestBody(bodyRawData, requestHeader_.getReuestHeaderValue(RequsetHeaderType.ContentType));
		} else {
			throw new HttpRequestHandlingException("reques parameter does not exist content type");
		}
//		String[] tmpSplitData= requestDataStr.split(HTTP_PROTOCOL_BODY_SEPARATOR);
//		if(tmpSplitData.length < 2) {
//			requestBody_ = null;
//		} else if(tmpSplitData.length == 2) {
//			requestBody_ = new RequestBody(requestLine);
//		} else {
//			
//		}
//		requestBody_ = new RequestBody(requestLine);
	}
	
	public RequestHttpMethod getRequestMethod() {
		return requestLine_.getHttpMethod();
	}
	
	public String getRequestPath() {
		return requestLine_.getUri();
	}
	
	public String getHttpProtocol() {
		return requestLine_.getProtocol();
	}
	
	public String getRequestHeaderValue(RequsetHeaderType type) {
		return requestHeader_.getReuestHeaderValue(type);
	}
	
	public byte[] getRequestBodyDataByKey(String key) {
		if(requestBody_ == null) {
			return null;
		}
		return requestBody_.getValueByKey(key);
	}

	public byte[] getRequestBodyDataByKey(String key, boolean isBase64Decode) {
		if(requestBody_ == null) {
			return null;
		}
		return requestBody_.getValueByKey(key, isBase64Decode);
	}

	public void setQueryParameter(String key, String value) {
		if(queryParameter_ == null) {
			queryParameter_ = new HashMap<>();
		}
		queryParameter_.put(key, value);
	}

	public Map<String, String> getQueryParameter() {
		if(queryParameter_ == null) {
			return new HashMap<>();
		}
		return queryParameter_;
	}
}
