package qaservice.WebServer.mainserver.taskhandle.http;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.WebServer.mainserver.taskhandle.http.request.RequestHttpMethod;
import qaservice.WebServer.mainserver.taskhandle.http.request.RequestMessage;
import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpNotPageException;
import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpRequestHandlingException;
import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpRouterDelegateMethodCallError;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessage;

public class HttpRouter {
	private static Map<RequestHttpMethod, Map<Pattern, Method>> routerMap_ = new HashMap<>();
	static {
		createRouterMap();
	}

	public static ResponseMessage delegateCreateResponseMethod(RequestMessage requestMessage) throws HttpNotPageException, HttpRequestHandlingException {
		RequestHttpMethod method = requestMessage.getRequestMethod();
		String uri = requestMessage.getRequestPath();
		if(!routerMap_.containsKey(method)) {
			QasiteLogger.warn("Not page URI = " + uri + " method = " + method.toString());
			throw new HttpNotPageException(method.name() + " can not use this web page.");
		}
		Map<Pattern, Method> routerMapInside = routerMap_.get(method);
		if(uri.indexOf("?") > 0) {
			int tmpIndex = uri.indexOf("?");
			String queryParamRaw = uri.substring(tmpIndex + 1, uri.length());
			for(String queryParam : queryParamRaw.split("\\&")) {
				String[] tmp = queryParam.split("\\=");
				if(tmp.length == 2) {
					requestMessage.setQueryParameter(tmp[0], tmp[1]);
				}
			}
			uri = uri.substring(0, tmpIndex);
		}
		Pattern insideKey = null;
		for(Pattern p : routerMapInside.keySet()) {
			if(p.toString().equals(uri)) {
				insideKey = p;
				break;
			}
			if(!p.toString().contains("*")) {
				continue;
			}
			if(p.matcher(uri).find()) {
				insideKey = p;
				break;
			}
		}
		if(insideKey == null) {
			throw new HttpNotPageException(uri + " is not found page.");
		}

		Method callerMethod = routerMapInside.get(insideKey);
		ResponseMessage result = null;
		try {
			result = callCreateResponseDataMethod(callerMethod, requestMessage);
		} catch(InvocationTargetException ie) {
			if(ie.getTargetException() instanceof HttpRouterDelegateMethodCallError) {
				HttpRouterDelegateMethodCallError e = (HttpRouterDelegateMethodCallError)ie.getTargetException();
				switch(e.getStatusLine()){
					case Not_Found: {
						throw new HttpNotPageException(e.getMessage());
					}
					case Internal_Server_Error : {
						throw new HttpRequestHandlingException(e.getMessage());
					}
					default: {
						throw new HttpRequestHandlingException("Internal Server Error");
					}
				}
			}
			QasiteLogger.warn("delegateCreateResponseMethod error", ie);
			throw new HttpRequestHandlingException(ie.getMessage());
		} catch(IllegalAccessException ae) {
			QasiteLogger.warn("delegateCreateResponseMethod IllegalAccess error", ae);
			throw new HttpRequestHandlingException(ae.getMessage());
		}
		return result;
	}
	
	private static void createRouterMap() {
		Class<HttpRoutingMethodList> clazz = HttpRoutingMethodList.class;
		Method[] methods = clazz.getDeclaredMethods();
		for(Method method : methods) {
			for(Annotation ano : method.getAnnotations()) {
				if(!(ano instanceof HttpRoutingMarker)) {
					continue;
				}
				HttpRoutingMarker marker = (HttpRoutingMarker)ano;
				RequestHttpMethod httpMethod = marker.method();
				if(routerMap_.containsKey(httpMethod)) {
					Map<Pattern, Method> tmpMap = routerMap_.get(httpMethod);
					tmpMap.put(createPattern(marker.uri()), method);
				} else {
					Map<Pattern, Method> routerMapInside = new HashMap<>();
					routerMapInside.put(createPattern(marker.uri()), method);
					routerMap_.put(httpMethod, routerMapInside);
				}
			}
		}
	}
	
	private static ResponseMessage callCreateResponseDataMethod(Method callerMethod, RequestMessage requestMessage) 
			throws InvocationTargetException, IllegalAccessException, HttpRequestHandlingException {
		Object result = callerMethod.invoke(HttpRoutingMethodList.class, requestMessage);
		if(!(result instanceof ResponseMessage)) {
			throw new HttpRequestHandlingException("create response message error");
		}
		return (ResponseMessage)result;
	}
	
	private static Pattern createPattern(String str) {
		if(str.indexOf("*") < 0) {
			return Pattern.compile(str);
		}
		int index = str.indexOf("*");
		str = str + "$";
		if(str.split("\\.").length > 1) {
			str = str.split("\\.")[0] + "\\." + str.split("\\.")[1];
		}
		str = str.substring(0, index) + "." + str.substring(index, str.length());
		return Pattern.compile(str);
	}
}
