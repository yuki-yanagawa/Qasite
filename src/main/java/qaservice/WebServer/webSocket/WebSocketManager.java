package qaservice.WebServer.webSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import qaservice.Common.socketutil.WrapperSocket;

public class WebSocketManager {
	private static final int MAX_ENABLE_REGIST_SIZE = 10;
	private static List<WebSocketWorkerThread> workerThreadList_ = new ArrayList<>();
	static {
		createWorkerThread();
	}

	@SuppressWarnings("rawtypes")
	public synchronized static boolean registSocket(WrapperSocket wrapperSocket) {
		for(WebSocketWorkerThread w : workerThreadList_) {
			if(w.getWorkerState() == WebSocketWorkerState.WAITING) {
				w.workerStateExcuting();
				w.setWrapperSocket(wrapperSocket);
				return true;
			}
		}
		return false;
	}
	
	private static void createWorkerThread() {
		for(int i = 0 ; i < MAX_ENABLE_REGIST_SIZE; i++) {
			workerThreadList_.add(new WebSocketWorkerThread());
		}
	}

	static void notifyCathedData(byte[] data, String threadName) {
		byte[] sendData = WebSocketMessageUtil.createSendMessage(data);
		for(WebSocketWorkerThread w : workerThreadList_) {
			if(w.getWorkerState() == WebSocketWorkerState.EXECUTING) {
				try {
					w.sendMessage(sendData);
				} catch(IOException e) {
					System.err.println(w.currentThread().getName() + " : send message error");
				}
			}
		}
	}
}
