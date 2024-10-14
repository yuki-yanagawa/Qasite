package qaservice.WebServer.mainserver.taskhandle.http.response;

import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpRequestHandlingException;
import qaservice.WebServer.mainserver.taskhandle.http.session.SessionRegistWrapFunc;

public abstract class ResponseMessage {
//	private ResponseContentType type_;
//	private byte[] body_;
//	private String httpProtocol_;
//	private String locationPath_;
//	private String registSessionId_;
//	private ResonseStatusLine statusLine_;
//	private boolean isCompressedBodyData_;
	protected ResponseContentType type_;
	protected byte[] body_;
	protected String httpProtocol_;
	protected String registSessionId_;
	
	public ResponseMessage(ResponseContentType type, byte[] body, String httpProtocol) {
//		this(type, body, httpProtocol, false);
		type_ = type;
		body_ = body;
		httpProtocol_ = httpProtocol;
	}

//	public ResponseMessage(ResponseContentType type, byte[] body, String httpProtocol, boolean isCompressed) {
//		type_ = type;
//		body_ = body;
//		httpProtocol_ = httpProtocol;
//		isCompressedBodyData_ = isCompressed;
//	}
//	
//	public ResponseMessage(ResponseContentType type, byte[] body, String httpProtocol, String locationPath) {
//		type_ = type;
//		body_ = body;
//		httpProtocol_ = httpProtocol;
//		locationPath_ = locationPath;
//	}
//
//	public ResponseMessage(ResponseContentType type, byte[] body, String httpProtocol, ResonseStatusLine statusLine) {
//		type_ = type;
//		body_ = body;
//		httpProtocol_ = httpProtocol;
//		statusLine_ = statusLine;
//	}
//	
//	public ResponseMessage setSessionRegist(SessionRegistWrapFunc wrapFunc) {
//		registSessionId_ = wrapFunc.regist();
//		return this;
//	}
//	
//	public byte[] createResponseMessage(boolean isKeepAlive) throws HttpRequestHandlingException {
//		if(locationPath_ != null) {
//			return createResponseLocationMessage(locationPath_);
//		}
//		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//		String line;
//		if(statusLine_ == null) { 
//			line = ResonseStatusLine.OK.createStatusLine(httpProtocol_);
//		} else {
//			line = statusLine_.createStatusLine(httpProtocol_);
//		}
//		try {
//			StringBuilder sb = new StringBuilder();
//			sb.append(line)
//				.append(ResponseMessageCreateHelper.createHeaderHost())
//				.append(ResponseMessageCreateHelper.createHeaderDate());
//			if(body_ != null) {
//				sb.append(ResponseMessageCreateHelper.createHeaderContentType(type_));
//			}
//			if(registSessionId_ != null) {
//				sb.append(ResponseMessageCreateHelper.createHeaderSetCookie(registSessionId_));
//			}
//			if(isCompressedBodyData_) {
//				sb.append(ResponseMessageCreateHelper.createHeaderContentEncodingGzip());
//			}
//			if(isKeepAlive) {
//				sb.append(ResponseMessageCreateHelper.createHeaderConnectionKeepAlive());
//			} else {
//				sb.append(ResponseMessageCreateHelper.createHeaderConnectionClose());
//			}
//			if(body_ == null) {
//				sb.append(ResponseMessageCreateHelper.END_CODE);
//				return sb.toString().getBytes();
//			}
//			int bodyLength = body_.length;
//			sb.append(ResponseMessageCreateHelper.createHeaderContentLength(bodyLength));
//			byteArrayOutputStream.write(sb.toString().getBytes());
//			byteArrayOutputStream.write(ResponseMessageCreateHelper.RESPONSE_LINE_SEPARATOR.getBytes());
//			byteArrayOutputStream.write(body_);
//			byteArrayOutputStream.write(ResponseMessageCreateHelper.END_CODE.getBytes());
//		} catch(IOException e) {
//			ServerLogger.getInstance().warn(e, "create response message error");
//			throw new HttpRequestHandlingException("create response message error");
//		}
//		return byteArrayOutputStream.toByteArray();
//	}
//	
//	private byte[] createResponseLocationMessage(String locationPath) throws HttpRequestHandlingException {
//		String line = ResonseStatusLine.See_Other.createStatusLine(httpProtocol_);
//		StringBuilder sb = new StringBuilder();
//		sb.append(line)
//			.append(ResponseMessageCreateHelper.createHeaderHost())
//			.append(ResponseMessageCreateHelper.createHeaderDate())
//			.append(ResponseMessageCreateHelper.createHeaderLocation(locationPath));
//		if(registSessionId_ != null) {
//			sb.append(ResponseMessageCreateHelper.createHeaderSetCookie(registSessionId_));
//		}
//		sb.append(ResponseMessageCreateHelper.END_CODE);
//		return sb.toString().getBytes();
//	}
//	
//	public byte[] getBody() {
//		return body_;
//	}
	public abstract byte[] createResponseMessage(boolean isKeepAlive) throws HttpRequestHandlingException;
	
	public ResponseMessage setSessionRegist(SessionRegistWrapFunc wrapFunc) {
		registSessionId_ = wrapFunc.regist();
		return this;
	}
}
