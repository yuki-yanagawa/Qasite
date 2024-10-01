package qaservice.WebServer.mainserver.taskhandle.http.request;

public enum RequsetHeaderType {
	Host("HOST"),
	Date("DATE"),
	Cookie("COOKIE"),
	ContentLength("CONTENT-LENGTH"),
	ContentType("CONTENT-TYPE"),
	AcceptEncoding("ACCEPT-ENCODING"),
	Connection("CONNECTION");
	
	private String name_;
	private RequsetHeaderType(String name) {
		name_ = name;
	}
	String getName() {
		return name_;
	}
}
