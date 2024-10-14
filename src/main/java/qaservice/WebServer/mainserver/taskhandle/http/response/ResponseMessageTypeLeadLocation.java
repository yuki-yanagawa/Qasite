package qaservice.WebServer.mainserver.taskhandle.http.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import qaservice.WebServer.logger.ServerLogger;
import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpRequestHandlingException;

public class ResponseMessageTypeLeadLocation extends ResponseMessage {
	private String locationPath_;
	public ResponseMessageTypeLeadLocation(String httpProtocol, String locationPath) {
		super(null, null, httpProtocol);
		locationPath_ = locationPath;
	}

	@Override
	public byte[] createResponseMessage(boolean isKeepAlive) throws HttpRequestHandlingException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		String line = ResonseStatusLine.See_Other.createStatusLine(httpProtocol_);
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(line)
				.append(ResponseMessageCreateHelper.createHeaderHost())
				.append(ResponseMessageCreateHelper.createHeaderDate())
				.append(ResponseMessageCreateHelper.createHeaderLocation(locationPath_))
				.append(ResponseMessageCreateHelper.END_CODE);

				byteArrayOutputStream.write(sb.toString().getBytes());
		} catch(IOException e) {
			ServerLogger.getInstance().warn(e, "create response message error type file");
			throw new HttpRequestHandlingException("create response message error");
		}
		return byteArrayOutputStream.toByteArray();
	}
}
