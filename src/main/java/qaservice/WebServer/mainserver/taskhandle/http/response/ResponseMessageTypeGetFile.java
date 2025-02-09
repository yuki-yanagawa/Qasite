package qaservice.WebServer.mainserver.taskhandle.http.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpRequestHandlingException;

public class ResponseMessageTypeGetFile extends ResponseMessage {
	private boolean isCompressedBodyData_;
	public ResponseMessageTypeGetFile(ResponseContentType type, byte[] body, String httpProtocol) {
		super(type, body, httpProtocol);
		isCompressedBodyData_ = false;
	}

	public ResponseMessageTypeGetFile(ResponseContentType type, byte[] body, String httpProtocol, boolean compressed) {
		super(type, body, httpProtocol);
		isCompressedBodyData_ = compressed;
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
				.append(ResponseMessageCreateHelper.createAccessControllAllowOrigin())
				.append(ResponseMessageCreateHelper.createHeaderContentLength(bodyLength));
			if(isCompressedBodyData_) sb.append(ResponseMessageCreateHelper.createHeaderContentEncodingGzip());
				byteArrayOutputStream.write(sb.toString().getBytes());
				byteArrayOutputStream.write(ResponseMessageCreateHelper.RESPONSE_LINE_SEPARATOR.getBytes());
				byteArrayOutputStream.write(body_);
				byteArrayOutputStream.write(ResponseMessageCreateHelper.END_CODE.getBytes());
		} catch(IOException e) {
			QasiteLogger.warn("create response message error type file" ,e);
			throw new HttpRequestHandlingException("create response message error");
		}
		return byteArrayOutputStream.toByteArray();
	}
}
