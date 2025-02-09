package qaservice.WebServer.mainserver.taskhandle.http.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.Common.charcterutil.CharUtil;
import qaservice.Common.charcterutil.messageDigest.MessageDigestTypeSHA1;
import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpRequestHandlingException;

public class ResponseMessageTypeWebSocket extends ResponseMessage {
	private static final String MAGIC_NO = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	private String webSocketKey_;
	public ResponseMessageTypeWebSocket(String webSocketKey, String httpProtocol) {
		super(null, null, httpProtocol);
		webSocketKey_ = webSocketKey;
	}

	@Override
	public byte[] createResponseMessage(boolean isKeepAlive) throws HttpRequestHandlingException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		String line = httpProtocol_ + " 101 Switching Protocols\r\n";
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(line)
				.append(ResponseMessageCreateHelper.createHeaderHost())
				.append(ResponseMessageCreateHelper.createHeaderDate())
				.append(ResponseMessageCreateHelper.createConnectionUpgrade())
				.append(ResponseMessageCreateHelper.createUpgrade("websocket"))
				.append("Sec-WebSocket-Accept: " + createWebSocketAcceptKey());

				byteArrayOutputStream.write(sb.toString().getBytes());
				//byteArrayOutputStream.write(ResponseMessageCreateHelper.END_CODE.getBytes());
		} catch(IOException e) {
			QasiteLogger.warn("create response message error type file", e);
			throw new HttpRequestHandlingException("create response message error");
		}
		return byteArrayOutputStream.toByteArray();
	}

	private String createWebSocketAcceptKey() {
		return Base64.getEncoder().encodeToString(
				MessageDigestTypeSHA1.digest((this.webSocketKey_ + MAGIC_NO).getBytes(CharUtil.getCharset()))) + "\r\n\r\n";
	}

}
