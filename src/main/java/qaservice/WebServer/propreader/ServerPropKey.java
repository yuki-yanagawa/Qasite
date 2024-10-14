package qaservice.WebServer.propreader;

public enum ServerPropKey {
	ServerWorkerThreadCount("httpWorkerThreadCount"),
	ServerHost("serverHost"),
	ServerDefaultPortSetting("serverPortDefaultSettiing"),
	ServerName("hostName"),
	ServerTLS("servertls"),
	DateFormat("dateTimeFormat"),
	TimeZone("zoneTime"),
	LoggingLevel("LoggingLevel"),
	LogStackTrace("logStackTrace"),
	ReadBuffer("httpServerReadBuffer"),
	ReadTimeOut("httpServerReadTimeOut"),
	KeepAliveTimeOut("keepAliveTimeOut"),
	KeepAliveMaxResource("keepAliveMaxResource"),
	WithDBServerStart("withDBServerStart"),
	ConnectionPoolCount("connectionPoolCount"),
	DBServerConnetionPath("dbconnetionPath"),
	ConsoleOut("consoleOut");
	
	
	private ServerPropKey(String key) {
		key_ = key;
	}
	private String key_;
	public String getKey() {
		return key_;
	}
}
