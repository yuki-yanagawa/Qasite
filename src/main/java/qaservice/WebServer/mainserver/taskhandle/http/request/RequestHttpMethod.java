package qaservice.WebServer.mainserver.taskhandle.http.request;

import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpRequestHandlingException;

public enum RequestHttpMethod {
	GET,
	POST,
	PUT,
	DELETE;
	
	public static RequestHttpMethod createHttpMethod(String methodStr) throws HttpRequestHandlingException {
		String methodStrUpperCase = methodStr.toUpperCase();
		for(RequestHttpMethod m : RequestHttpMethod.values()) {
			if(m.name().equals(methodStrUpperCase)) {
				return m;
			}
		}
		throw new HttpRequestHandlingException("request http method does not correspond");
	}
}
