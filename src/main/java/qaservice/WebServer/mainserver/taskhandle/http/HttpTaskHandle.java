package qaservice.WebServer.mainserver.taskhandle.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import qaservice.WebServer.logger.ServerLogger;
import qaservice.WebServer.mainserver.taskhandle.http.request.RequestMessage;
import qaservice.WebServer.mainserver.taskhandle.http.request.RequsetHeaderType;
import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpNotPageException;
import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpRequestHandlingException;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResonseStatusLine;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessage;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessageCreator;
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
			ServerLogger.getInstance().warn(e, "read buffer size error from server properties");
			buffSize = 1024;
			readTimeOut = 50 * 1000;
		}
		BUFFER_SIZE = buffSize;
		READ_TIME_OUT = readTimeOut;
	}

	public static boolean httpHandleThread(InputStream is, OutputStream os) throws IOException{
		byte[] requestDataRaw = readRequestData(is);
		if(requestDataRaw.length == 0) {
			return false;
		}
		try {
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
			// keep-alive check
			String connectionStyle = requestMessage.getRequestHeaderValue(RequsetHeaderType.Connection);
			boolean usingKeepAlive = false;
			if("KEEP-ALIVE".equals(connectionStyle.toUpperCase())) {
				usingKeepAlive = true;
			}
			ResponseMessage responseMessage = HttpRouter.delegateCreateResponseMethod(requestMessage);
			os.write(responseMessage.createResponseMessage(usingKeepAlive));
			os.flush();
			return usingKeepAlive;
		} catch(HttpRequestHandlingException he) {
			he.printStackTrace();
			//HTTP 500
			os.write(ResponseMessageCreator.createResponseMessage(ResonseStatusLine.Internal_Server_Error, "HTTP/1.1", he.getMessage()));
			os.flush();
			return false;
		} catch(HttpNotPageException ne) {
			//ne.printStackTrace();
			//HTTP 404
			os.write(ResponseMessageCreator.createResponseMessage(ResonseStatusLine.Not_Found, "HTTP/1.1", ne.getMessage()));
			os.flush();
			return false;
		}
	}

	private static byte[] readRequestData(InputStream is) throws IOException {
		int readSize = 0;
		ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
		long readStartTime = System.currentTimeMillis();
		int currentBufSize = 0;
		byte[] buffer;
		while(true) {
			buffer = new byte[BUFFER_SIZE];
			readSize = is.read(buffer);
			currentBufSize += readSize;
			ServerLogger.getInstance().info("read data size: " + readSize);
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
