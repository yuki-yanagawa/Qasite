package qaservice.WebServer.model.util;

import qaservice.WebServer.propreader.ServerPropReader;

public class CreateWebServerURI {
	public static String createURI(String requestPath) {
		StringBuilder sb = new StringBuilder();
		boolean isTls = Boolean.parseBoolean(ServerPropReader.getProperties("servertls").toString());
		if(isTls) {
			sb.append("https://");
		} else {
			sb.append("http://");
		}
		String host = ServerPropReader.getProperties("serverHost").toString();
		sb.append(host);
		int port = Integer.parseInt(ServerPropReader.getProperties("serverPortDefaultSettiing").toString());

		if(!(isTls && port == 443) && !(!isTls && port == 80)) {
			sb.append(":" + String.valueOf(port));
		}

		if(requestPath == null || "".equals(requestPath)) {
			return sb.toString();
		}

		sb.append("/" + requestPath);
		return sb.toString();
	}
}
