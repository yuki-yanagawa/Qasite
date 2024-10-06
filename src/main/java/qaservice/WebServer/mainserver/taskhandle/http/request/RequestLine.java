package qaservice.WebServer.mainserver.taskhandle.http.request;

import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpRequestHandlingException;

class RequestLine {
	private RequestHttpMethod method_;
	private String uri_;
	private String protcolver_;
	
	RequestLine(String requestLine) throws HttpRequestHandlingException {
		String[] tmp = requestLine.split("\\s+");
		if(tmp.length != 3) {
			throw new HttpRequestHandlingException("request Line create Error");
		}
		method_ = RequestHttpMethod.createHttpMethod(tmp[0]);
		uri_ = tmp[1];
		protcolver_ = tmp[2];
	}
	
	RequestHttpMethod getHttpMethod() {
		return method_;
	}
	
	String getUri() {
		return uri_;
	}
	
	String getProtocol() {
		return protcolver_;
	}
}
