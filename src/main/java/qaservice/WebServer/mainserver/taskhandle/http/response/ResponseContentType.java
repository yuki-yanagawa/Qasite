package qaservice.WebServer.mainserver.taskhandle.http.response;

import qaservice.Common.charcterutil.CharUtil;

public enum ResponseContentType {
	PLAIN("text/plain", true),
	CSV("text/csv", true),
	CSS("text/css", true),
	HTML("text/html", true),
	JPEG("image/jpeg", false),
	JAVASCRIPT("application/javascript", true),
	JSON("application/json", true);
	
	private String type_;
	private boolean isCharSetting_;
	private ResponseContentType(String type, boolean isCharSetting) {
		type_ = type;
		isCharSetting_ = isCharSetting;
	}
	
	String createContentTypeValue() {
		if(isCharSetting_) {
			return type_ + ";" + " charset=" + CharUtil.getCharset().toString();
		}
		return type_;
	}
}
