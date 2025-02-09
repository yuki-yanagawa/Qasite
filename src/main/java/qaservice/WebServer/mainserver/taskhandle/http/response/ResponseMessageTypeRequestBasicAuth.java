package qaservice.WebServer.mainserver.taskhandle.http.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpRequestHandlingException;

public class ResponseMessageTypeRequestBasicAuth extends ResponseMessage {
	public ResponseMessageTypeRequestBasicAuth(String httpProtocol) {
		super(null, null, httpProtocol);
	}

	@Override
	public byte[] createResponseMessage(boolean isKeepAlive) throws HttpRequestHandlingException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		String line = ResonseStatusLine.Authorization_Required.createStatusLine(httpProtocol_);
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(line)
				.append(ResponseMessageCreateHelper.createHeaderHost())
				.append(ResponseMessageCreateHelper.createHeaderDate())
				.append(ResponseMessageCreateHelper.createHeaderWWWAuthenticate())
				.append(ResponseMessageCreateHelper.createHeaderConnectionClose())
				.append(ResponseMessageCreateHelper.END_CODE);
			byteArrayOutputStream.write(sb.toString().getBytes()); 
		} catch (IOException e) {
			QasiteLogger.warn("create response message error type basic auth", e);
			throw new HttpRequestHandlingException("create response message error");
		}
		return byteArrayOutputStream.toByteArray();
	}
}
