package qaservice.WebServer.mainserver.taskhandle.http.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import qaservice.WebServer.logger.ServerLogger;
import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpRequestHandlingException;

public class ResponseMessageTypeJson extends ResponseMessage {
	private boolean isCompressedBodyData_;
	public ResponseMessageTypeJson(ResponseContentType type, byte[] body, String httpProtocol) {
		this(type, body, httpProtocol, false);
	}

	public ResponseMessageTypeJson(ResponseContentType type, byte[] body, String httpProtocol, boolean isCompressed) {
		super(type, body, httpProtocol);
		isCompressedBodyData_ = isCompressed;
	}

	@Override
	public byte[] createResponseMessage(boolean isKeepAlive) throws HttpRequestHandlingException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		String line = ResonseStatusLine.OK.createStatusLine(httpProtocol_);
		int bodyLength = body_.length;
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(line)
				.append(ResponseMessageCreateHelper.createHeaderHost())
				.append(ResponseMessageCreateHelper.createHeaderDate())
				.append(ResponseMessageCreateHelper.createHeaderContentType(type_))
				.append(ResponseMessageCreateHelper.createHeaderContentLength(bodyLength));
			if(registSessionId_ != null) sb.append(ResponseMessageCreateHelper.createHeaderSetCookie(registSessionId_));
			if(isCompressedBodyData_) sb.append(ResponseMessageCreateHelper.createHeaderContentEncodingGzip());
			if(isKeepAlive) {
				sb.append(ResponseMessageCreateHelper.createHeaderConnectionKeepAlive());
			} else {
				sb.append(ResponseMessageCreateHelper.createHeaderConnectionClose());
			}

			byteArrayOutputStream.write(sb.toString().getBytes());
			byteArrayOutputStream.write(ResponseMessageCreateHelper.RESPONSE_LINE_SEPARATOR.getBytes());
			byteArrayOutputStream.write(body_);
			byteArrayOutputStream.write(ResponseMessageCreateHelper.END_CODE.getBytes());
		} catch(IOException e) {
			ServerLogger.getInstance().warn(e, "create response message error type file");
			throw new HttpRequestHandlingException("create response message error");
		}
		return byteArrayOutputStream.toByteArray();
	}
}
