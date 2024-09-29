package qaservice.WebServer.mainserver.taskhandle.http.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import qaservice.Common.dateutil.ServerDateUtil;
import qaservice.WebServer.mainserver.ServerOperator;

public class ResponseMessageCreator {
	static final String RESPONSE_LINE_SEPARATOR = "\r\n";
	static final String END_CODE = "\r\n\r\n";
	public static byte[] createResponseMessage(ResonseStatusLine statusLine, String httpPlotocol) {
		return createResponseMessage(statusLine, httpPlotocol, null);
//		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//		String line = statusLine.createStatusLine(httpPlotocol);
//		byte[] responseHeader = createGeneralHeaderByStatusCode(statusLine);
//		byte[] responseBody = createGeneralBodyByStatusCode(statusLine);
//		try {
//			byteArrayOutputStream.write(line.getBytes());
//			byteArrayOutputStream.write(responseHeader);
//			byteArrayOutputStream.write(responseBody);
//			byteArrayOutputStream.write(END_CODE.getBytes());
//		} catch(IOException e) {
//			e.printStackTrace();
//			return null;
//		}
//		return byteArrayOutputStream.toByteArray();
	}
	
	public static byte[] createResponseMessage(ResonseStatusLine statusLine, String httpPlotocol, String exceptionMessage) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		String line = statusLine.createStatusLine(httpPlotocol);
		byte[] responseHeader = createGeneralHeaderByStatusCode(statusLine);
		byte[] responseBody = createGeneralBodyByStatusCode(statusLine);
		try {
			byteArrayOutputStream.write(line.getBytes());
			byteArrayOutputStream.write(responseHeader);
			byteArrayOutputStream.write(responseBody);
			if(exceptionMessage != null) {
				byteArrayOutputStream.write(errorMessageInsert(exceptionMessage));
			}
			byteArrayOutputStream.write(END_CODE.getBytes());
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
		return byteArrayOutputStream.toByteArray();
	}
	
	private static byte[] createGeneralHeaderByStatusCode(ResonseStatusLine statusLine) {
		switch(statusLine) {
		case Bad_Request:
			return (createHeaderHost() + 
					createHeaderContentType(ResponseContentType.HTML) + 
					RESPONSE_LINE_SEPARATOR
					).getBytes();
		case Not_Found:
			return (createHeaderHost() + 
					createHeaderContentType(ResponseContentType.HTML) + 
					RESPONSE_LINE_SEPARATOR
					).getBytes();
		case Internal_Server_Error:
			return (createHeaderHost() + 
					createHeaderContentType(ResponseContentType.HTML) + 
					RESPONSE_LINE_SEPARATOR
					).getBytes();
		default:
			return (createHeaderHost() + 
					createHeaderContentType(ResponseContentType.HTML) + 
					RESPONSE_LINE_SEPARATOR
					).getBytes();
		}
	}
	
	private static byte[] createGeneralBodyByStatusCode(ResonseStatusLine statusLine) {
		switch(statusLine) {
		case Bad_Request:
			return ("<h1> Bad Request </h1>").getBytes();
		case Not_Found:
			return ("<h1> Not page </h1>").getBytes();
		case Internal_Server_Error:
			return ("<h1> Internal Server Error</h1>").getBytes();
		default:
			return ("").getBytes();
		}
	}
	
	private static byte[] errorMessageInsert(String errorMessage) {
		return ("<h5>" + errorMessage + "<h5>").getBytes();
	}
	
	static String createHeaderHost() {
		return "Host: " + ServerOperator.getServerName() + RESPONSE_LINE_SEPARATOR;
	}
	
	static String createHeaderDate() {
		return "Date: " + ServerDateUtil.getDateNow() + RESPONSE_LINE_SEPARATOR;
	}
	
	static String createHeaderLocation(String path) {
		return "Location: " + path + RESPONSE_LINE_SEPARATOR;
	}
	
	static String createHeaderContentType(ResponseContentType type) {
		return "Content-Type: " + type.createContentTypeValue() + RESPONSE_LINE_SEPARATOR;
	}
	
	static String createHeaderContentLength(int length) {
		return "Content-Length: " + String.valueOf(length) + RESPONSE_LINE_SEPARATOR;
	}
	
	static String createHeaderSetCookie(String cookie) {
		//return "Set-Cookie: " + cookie + RESPONSE_LINE_SEPARATOR;
		return "Set-Cookie: sessionid=" + cookie + RESPONSE_LINE_SEPARATOR;
	}
}
