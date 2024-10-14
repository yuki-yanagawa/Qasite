package qaservice.WebServer.mainserver.taskhandle.http.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import qaservice.WebServer.logger.ServerLogger;
import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpRequestHandlingException;

public class ResponseMessageTypeGetFile extends ResponseMessage {
	public ResponseMessageTypeGetFile(ResponseContentType type, byte[] body, String httpProtocol) {
		super(type, body, httpProtocol);
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
