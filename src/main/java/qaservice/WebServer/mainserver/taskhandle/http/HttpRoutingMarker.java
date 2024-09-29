package qaservice.WebServer.mainserver.taskhandle.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import qaservice.WebServer.mainserver.taskhandle.http.request.RequestHttpMethod;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface HttpRoutingMarker {
	RequestHttpMethod method();
	String uri();
}
