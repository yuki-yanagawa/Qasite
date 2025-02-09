package qaservice.WebServer.mainserver.taskhandle.http.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpRequestHandlingException;

public class ResponseMessageTypeNoBodyData extends ResponseMessage{
	private ResonseStatusLine statusLine_;
	public ResponseMessageTypeNoBodyData(String httpProtocol, ResonseStatusLine statusLine) {
		super(null, null, httpProtocol);
		statusLine_ = statusLine;
	}

	@Override
	public byte[] createResponseMessage(boolean isKeepAlive) throws HttpRequestHandlingException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		String line = statusLine_.createStatusLine(httpProtocol_);
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(line)
				.append(ResponseMessageCreateHelper.createHeaderHost())
				.append(ResponseMessageCreateHelper.createHeaderDate())
				.append(ResponseMessageCreateHelper.createHeaderConnectionClose());
			if(registSessionId_ != null) sb.append(ResponseMessageCreateHelper.createHeaderSetCookie(registSessionId_));
				sb.append(ResponseMessageCreateHelper.END_CODE);
			byteArrayOutputStream.write(sb.toString().getBytes()); 
		} catch (IOException e) {
			QasiteLogger.warn("create response message error type method put", e);
			throw new HttpRequestHandlingException("create response message error");
		}
		return byteArrayOutputStream.toByteArray();
	}
}
