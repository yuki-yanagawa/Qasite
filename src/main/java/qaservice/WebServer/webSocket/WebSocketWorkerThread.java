package qaservice.WebServer.webSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

import qaservice.Common.charcterutil.CharUtil;
import qaservice.Common.socketutil.WrapperSocket;

public class WebSocketWorkerThread extends Thread {
	private boolean keepRun_;
	@SuppressWarnings("rawtypes")
	private WrapperSocket wrapperSocket_;
	private WebSocketWorkerState webSocketWorkerState_;
	private Object key_ = new Object();
	WebSocketWorkerThread() {
		keepRun_ = true;
		webSocketWorkerState_ = WebSocketWorkerState.WAITING;
		this.start();
	}

	@SuppressWarnings("rawtypes")
	void setWrapperSocket(WrapperSocket wrapperSocket) {
		wrapperSocket_ = wrapperSocket;
		synchronized(key_) {
			key_.notify();
		}
	}
	@Override
	public void run(){
		boolean keepConnect = false;
		while(keepRun_) {
			if(!keepConnect) waitMode();
			keepConnect = readData();
			if(!keepConnect) freeWrapperSocket();
		}
	}

	private void waitMode() {
		webSocketWorkerState_ = WebSocketWorkerState.WAITING;
		try {
			synchronized(key_) {
				key_.wait();
			}
		} catch(InterruptedException e) {
			
		}
	}

	private void freeWrapperSocket() {
		try {
			wrapperSocket_.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	//Operate From WebSocketManager
	void workerStateExcuting() {
		webSocketWorkerState_ = WebSocketWorkerState.EXECUTING;
	}
	
	void workerStateChange(WebSocketWorkerState state) {
		webSocketWorkerState_ = state;
	}

	WebSocketWorkerState getWorkerState() {
		return webSocketWorkerState_;
	}

	void sendMessage(byte[] sendData) throws IOException {
		DataOutputStream dos = wrapperSocket_.getDataOutputStream();
		dos.write(sendData);
		dos.flush();
	}

	private boolean readData() {
		DataInputStream dis = wrapperSocket_.getDataInputStream();
		try {
			byte[] recvData = WebSocketMessageUtil.messageDecoder(dis);
			if(recvData == null) {
				System.err.println("recv data null...");
			} else {
				WebSocketManager.notifyCathedData(recvData, this.currentThread().getName());
			}
			return true;
		} catch(IOException e) {
			if(e instanceof SocketTimeoutException) {
				System.out.println("socket time out..");
				return true;
			}
			e.printStackTrace();
			return false;
		}
	}
}
