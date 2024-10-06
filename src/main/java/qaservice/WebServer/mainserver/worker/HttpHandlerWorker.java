package qaservice.WebServer.mainserver.worker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import qaservice.WebServer.logger.ServerLogger;
import qaservice.WebServer.mainserver.IServer;
import qaservice.WebServer.mainserver.taskhandle.http.HttpTaskHandle;
import qaservice.WebServer.propreader.ServerPropKey;
import qaservice.WebServer.propreader.ServerPropReader;

class HttpHandlerWorker extends Thread {
	private IServer server_;
	private WorkerStates state_;
	private boolean polling_;
	private String threadName_;
	private Thread runningThread_;
	private int keepAliveTimeOutmsec_;
	HttpHandlerWorker(String threadName) {
		threadName_ = threadName;
		keepAliveTimeOutmsec_ = Integer.parseInt(ServerPropReader.getProperties(ServerPropKey.KeepAliveTimeOut.getKey()).toString());
		workStart();
	}
	
	private void workStart() {
		polling_ = true;
		state_ = WorkerStates.WAITING;
		this.start();
	}
	
	@Override
	public void run() {
		Thread.currentThread().setName(threadName_);
		runningThread_ = Thread.currentThread();
		boolean changeLeaderResult = true;
		while(polling_) {
			//this.wait();
			if(server_ == null) {
				try {
					synchronized (this) {
						//System.out.println("Wait Mode!!! : " + Thread.currentThread().getName());
						this.wait();
					}
				} catch(InterruptedException e) {
					//ignore
					polling_ = false;
				}
			}
			if(server_ == null || !polling_) {
				state_ = WorkerStates.FAILED;
				ServerLogger.getInstance().warn("Http thread Worker Failed register serverSocket : Thread=" + Thread.currentThread().getName());
				return;
			}
			//System.out.println("Executing Mode!!! : " + Thread.currentThread().getName());
			if(state_ != WorkerStates.EXECUTING) {
				setStatusExecuting();
			}
			try(Socket clientSocket = server_.awaitRequest();){
				changeLeaderResult = HttpHandlerWorkerOperation.promptLeaderThread();
				if(changeLeaderResult) {
					//HttpTaskHandle.httpHandleThread(clientSocket);
					clientSocket.setKeepAlive(true);
					clientSocket.setSoTimeout(keepAliveTimeOutmsec_ * 1000);
					httpRequestHandle(clientSocket);
				} else {
					//Handle Full Task Response
				}
			} catch(Throwable e) {
				//e.printStackTrace();
				ServerLogger.getInstance().warn(e, "svrSocket accept step failed");
			}
			if(changeLeaderResult) {
				releaseAcceptTask();
			}
		}
	}

	private void httpRequestHandle(Socket socket) {
		try(InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream()) {
			boolean keepAlive = HttpTaskHandle.httpHandleThread(is, os);
			while(keepAlive) {
				keepAlive = HttpTaskHandle.httpHandleThread(is, os);
				if(!keepAlive) {
					os.write("".getBytes());
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			closeClientSocket(socket);
		}
	}

	public WorkerStates getWorkerState() {
		return state_;
	}

	void setStatusExecuting() {
		state_ = WorkerStates.EXECUTING;
		HttpHandlerWorkerOperation.incrementExecutingCount();
	}

	void releaseAcceptTask() {
		state_ = WorkerStates.WAITING;
		HttpHandlerWorkerOperation.decrementRequestCount();
		server_ = null;
	}

	void stopRunningReciver() {
		runningThread_.interrupt();
	}
	
	void stopPolling() {
		polling_ = false;
	}
	
	public void setDelegateAwaitProcess(IServer server) {
		server_ = server;
		synchronized (this) {
			this.notify();
		}
	}
	
	String getThreadName() {
		return threadName_;
	}

	private void closeClientSocket(Socket clientSocket) {
		if(clientSocket == null) {
			return;
		}
		try {
			clientSocket.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
