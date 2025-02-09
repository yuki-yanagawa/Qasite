package qaservice.WebServer.mainserver.taskhandle.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.WebServer.mainserver.taskhandle.http.request.RequestMessage;
import qaservice.WebServer.mainserver.taskhandle.http.request.RequsetHeaderType;
import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpNotPageException;
import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpRequestHandlingException;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessage;
import qaservice.WebServer.propreader.ServerPropKey;
import qaservice.WebServer.propreader.ServerPropReader;

public class HttpTaskHandle {
	private static final int BUFFER_SIZE;
	private static final long READ_TIME_OUT;
	static {
		int buffSize = 1024;
		long readTimeOut = 50 * 1000;
		try {
			buffSize = Integer.parseInt(ServerPropReader.getProperties(ServerPropKey.ReadBuffer.getKey()).toString());
			readTimeOut = Long.parseLong(ServerPropReader.getProperties(ServerPropKey.ReadTimeOut.getKey()).toString());
			readTimeOut *= 1000;
		} catch(NumberFormatException e) {
			QasiteLogger.warn("read buffer size error from server properties", e);
			buffSize = 1024;
			readTimeOut = 50 * 1000;
		}
		BUFFER_SIZE = buffSize;
		READ_TIME_OUT = readTimeOut;
	}

	public static RequestMessage analizeRequestMessage(InputStream is, OutputStream os) throws IOException, HttpRequestHandlingException, Exception {
		byte[] requestDataRaw = readRequestData(is);
		if(requestDataRaw.length == 0) {
			return null;
		}
		RequestMessage requestMessage = RequestMessage.analyzeRequestMesage(requestDataRaw);
		String contentLength = requestMessage.getRequestHeaderValue(RequsetHeaderType.ContentLength);
		if(!"".equals(contentLength)) {
			if(Integer.parseInt(contentLength) > requestDataRaw.length) {
				byte[] requestDataRawSecond = readRequestData(is);
				byte[] newRequestDataRaw = new byte[requestDataRaw.length + requestDataRawSecond.length];
				System.arraycopy(requestDataRaw, 0, newRequestDataRaw, 0, requestDataRaw.length);
				System.arraycopy(requestDataRawSecond, 0, newRequestDataRaw, requestDataRaw.length, requestDataRawSecond.length);
				requestMessage = RequestMessage.analyzeRequestMesage(newRequestDataRaw);
			}
		}
		return requestMessage;
	}

	public static boolean checkedRequestKeepAlive(RequestMessage requestMessage) {
		// keep-alive check
		boolean usingKeepAlive = false;
		String connectionStyle = requestMessage.getRequestHeaderValue(RequsetHeaderType.Connection);
		if(connectionStyle == null) {
			return usingKeepAlive;
		}
		if("KEEP-ALIVE".equals(connectionStyle.toUpperCase())) {
			usingKeepAlive = true;
		}
		return usingKeepAlive;
	}

	public static ResponseMessage createResponseMessage(RequestMessage requestMessage) throws HttpNotPageException, HttpRequestHandlingException {
		return HttpRouter.delegateCreateResponseMethod(requestMessage);
	}

	private static byte[] readRequestData(InputStream is) throws IOException, Exception {
		int readSize = 0;
		ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
		long readStartTime = System.currentTimeMillis();
		int currentBufSize = 0;
		byte[] buffer;
		while(true) {
			buffer = new byte[BUFFER_SIZE];
			try {
				readSize = is.read(buffer);
			} catch(Exception e) {
				if(e instanceof SocketTimeoutException) {
					if(readSize == 0) {
						throw e;
					} else {
						throw new IOException("interrput read data");
					}
				}
			}
			currentBufSize += readSize;
			QasiteLogger.info("read data size: " + readSize + " thread name = " + Thread.currentThread().getName());
			if(readSize <= 0) {
				if(currentBufSize == 0) {
					throw new IOException("http task handle = read Error");
				}
				break;
			}
			if(readSize < BUFFER_SIZE) {
				byteArrayOut.write(buffer, 0, readSize);
//				if(currentBufSize == readSize) {
//					byteArrayOut.write(buffer, 0, readSize);
//				} else {
//					//byteArrayOut.write(buffer, byteArrayOut.size(), readSize);
//					byteArrayOut.write(buffer, byteArrayOut.size(), readSize);
//				}
				break;
			}
			if(readSize == BUFFER_SIZE && (isEndedCharcterCode(buffer, readSize) || isEndedCharcterCodeNull(buffer, readSize))) {
				byteArrayOut.write(buffer);
				break;
			}
			byteArrayOut.write(buffer);
			if(READ_TIME_OUT < System.currentTimeMillis() - readStartTime) {
				throw new IOException("http task handle = read Time Out");
			}
		}
		byteArrayOut.close();
		return byteArrayOut.toByteArray();
	}

	private static boolean isEndedCharcterCode(byte[] readData, int readSize) {
		if(readData[readSize - 1] == 10 && readData[readSize - 2] == 13 
				&& readData[readSize - 3] == 10 && readData[readSize - 4] == 13){
			return true;
		}
		return false;
	}
	
	private static boolean isEndedCharcterCodeNull(byte[] readData, int readSize) {
		if(readData[readSize - 1] == 0){
			return true;
		}
		return false;
	}
}
