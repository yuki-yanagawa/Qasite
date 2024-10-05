package qaservice.WebServer.mainserver.taskhandle.http.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import qaservice.WebServer.logger.ServerLogger;
import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpRequestHandlingException;
import qaservice.WebServer.mainserver.taskhandle.http.session.SessionRegistWrapFunc;

public class ResponseMessage {
	private ResponseContentType type_;
	private byte[] body_;
	private String httpProtocol_;
	private String locationPath_;
	private String registSessionId_;
	private ResonseStatusLine statusLine_;
	private boolean isCompressedBodyData_;
	
	public ResponseMessage(ResponseContentType type, byte[] body, String httpProtocol) {
		this(type, body, httpProtocol, false);
	}

	public ResponseMessage(ResponseContentType type, byte[] body, String httpProtocol, boolean isCompressed) {
		type_ = type;
		body_ = body;
		httpProtocol_ = httpProtocol;
		isCompressedBodyData_ = isCompressed;
	}
	
	public ResponseMessage(ResponseContentType type, byte[] body, String httpProtocol, String locationPath) {
		type_ = type;
		body_ = body;
		httpProtocol_ = httpProtocol;
		locationPath_ = locationPath;
	}

	public ResponseMessage(ResponseContentType type, byte[] body, String httpProtocol, ResonseStatusLine statusLine) {
		type_ = type;
		body_ = body;
		httpProtocol_ = httpProtocol;
		statusLine_ = statusLine;
	}
	
	public ResponseMessage setSessionRegist(SessionRegistWrapFunc wrapFunc) {
		registSessionId_ = wrapFunc.regist();
		return this;
	}
	
	public byte[] createResponseMessage(boolean isKeepAlive) throws HttpRequestHandlingException {
		if(locationPath_ != null) {
			return createResponseLocationMessage(locationPath_);
		}
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		String line;
		if(statusLine_ == null) { 
			line = ResonseStatusLine.OK.createStatusLine(httpProtocol_);
		} else {
			line = statusLine_.createStatusLine(httpProtocol_);
		}
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(line)
				.append(ResponseMessageCreator.createHeaderHost())
				.append(ResponseMessageCreator.createHeaderDate());
			if(body_ != null) {
				sb.append(ResponseMessageCreator.createHeaderContentType(type_));
			}
			if(registSessionId_ != null) {
				sb.append(ResponseMessageCreator.createHeaderSetCookie(registSessionId_));
			}
			if(isCompressedBodyData_) {
				sb.append(ResponseMessageCreator.createHeaderContentEncodingGzip());
			}
			if(isKeepAlive) {
				sb.append(ResponseMessageCreator.createHeaderConnectionKeepAlive());
			} else {
				sb.append(ResponseMessageCreator.createHeaderConnectionClose());
			}
			if(body_ == null) {
				sb.append(ResponseMessageCreator.END_CODE);
				return sb.toString().getBytes();
			}
			int bodyLength = body_.length;
			sb.append(ResponseMessageCreator.createHeaderContentLength(bodyLength));
			byteArrayOutputStream.write(sb.toString().getBytes());
			byteArrayOutputStream.write(ResponseMessageCreator.RESPONSE_LINE_SEPARATOR.getBytes());
			byteArrayOutputStream.write(body_);
			byteArrayOutputStream.write(ResponseMessageCreator.END_CODE.getBytes());
		} catch(IOException e) {
			ServerLogger.getInstance().warn(e, "create response message error");
			throw new HttpRequestHandlingException("create response message error");
		}
		return byteArrayOutputStream.toByteArray();
	}
	
	private byte[] createResponseLocationMessage(String locationPath) throws HttpRequestHandlingException {
		String line = ResonseStatusLine.See_Other.createStatusLine(httpProtocol_);
		StringBuilder sb = new StringBuilder();
		sb.append(line)
			.append(ResponseMessageCreator.createHeaderHost())
			.append(ResponseMessageCreator.createHeaderDate())
			.append(ResponseMessageCreator.createHeaderLocation(locationPath));
		if(registSessionId_ != null) {
			sb.append(ResponseMessageCreator.createHeaderSetCookie(registSessionId_));
		}
		sb.append(ResponseMessageCreator.END_CODE);
		return sb.toString().getBytes();
	}
	
	public byte[] getBody() {
		return body_;
	}
}
