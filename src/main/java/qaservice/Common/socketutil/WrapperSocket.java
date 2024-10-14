package qaservice.Common.socketutil;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.net.ssl.SSLSocket;

public class WrapperSocket <T extends Socket> implements Closeable {
	private InputStream inputStream_;
	private OutputStream outputStream_;
	private DataInputStream dis_;
	private DataOutputStream dos_;
	private T socket_;
	private boolean keepConnect_;

	public static <T extends Socket> WrapperSocket<?> createInstance(Socket socket, boolean isSSL) throws IOException {
		if(isSSL) {
			return new WrapperSocket<SSLSocket>((SSLSocket)socket);
		} else {
			return new WrapperSocket<Socket>(socket);
		}
	}

	private WrapperSocket(T socket) throws IOException {
		socket_ = socket;
		inputStream_ = socket_.getInputStream();
		outputStream_ = socket_.getOutputStream();
		dis_ = new DataInputStream(inputStream_);
		dos_ = new DataOutputStream(outputStream_);
		keepConnect_ = false;
	}

	public InputStream getInputStream() {
		return inputStream_;
	}

	public OutputStream getOutputStream() {
		return outputStream_;
	}

	public void setKeepConnect(boolean keepConnect) {
		keepConnect_ = keepConnect;
	}

	public DataInputStream getDataInputStream() {
		return dis_;
	}

	public DataOutputStream getDataOutputStream() {
		return dos_;
	}

	public void finishedConnect() {
		keepConnect_ = false;
		try {
			close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws IOException {
		if(keepConnect_) {
			return;
		}

		if(socket_ != null) {
			socket_.close();
		}
		if(inputStream_ != null) {
			inputStream_.close();
		}
		if(outputStream_ != null) {
			outputStream_.close();
		}
		if(dis_ != null) {
			dis_.close();
		}
		if(dos_ != null) {
			dos_.close();
		}
	}
}
