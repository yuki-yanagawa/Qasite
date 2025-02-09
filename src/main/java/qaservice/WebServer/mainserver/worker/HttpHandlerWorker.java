package qaservice.WebServer.mainserver.worker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLSocket;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.Common.debug.DebugChecker;
import qaservice.Common.socketutil.WrapperSocket;
import qaservice.WebServer.mainserver.IServer;
import qaservice.WebServer.mainserver.taskhandle.http.HttpTaskHandle;
import qaservice.WebServer.mainserver.taskhandle.http.request.RequestMessage;
import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpNotPageException;
import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpRequestHandlingException;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessageTypeWebSocket;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResonseStatusLine;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessage;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessageCreateHelper;
import qaservice.WebServer.propreader.ServerPropKey;
import qaservice.WebServer.propreader.ServerPropReader;
import qaservice.WebServer.webSocket.WebSocketManager;

class HttpHandlerWorker extends Thread {
	private IServer server_;
	private WorkerStates state_;
	private boolean polling_;
	private String threadName_;
	private Thread runningThread_;
	private int keepAliveTimeOutmsec_;
	HttpHandlerWorker(String threadName) {
		threadName_ = threadName;
		keepAliveTimeOutmsec_ = Integer.parseInt(ServerPropReader.getProperties(ServerPropKey.KeepAliveTimeOut.getKey()).toString()) * 1000;
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
						this.wait();
					}
				} catch(InterruptedException e) {
					//ignore
					polling_ = false;
				}
			}
			if(server_ == null || !polling_) {
				state_ = WorkerStates.FAILED;
				QasiteLogger.warn("Http thread Worker Failed register serverSocket : Thread=" + getThreadName());
				return;
			}
			if(state_ != WorkerStates.EXECUTING) {
				setStatusExecuting();
			}
			// For Web Socket Connecting Change. Because socket want to connect enable keeping.
			//try(Socket clientSocket = server_.awaitRequest();){
			try {
				Socket clientSocket = server_.awaitRequest();
				//DEBUG Mode
				if(DebugChecker.isDEBUGMode()) {
					QasiteLogger.debug("connect socket / addr = " + clientSocket.getInetAddress().getHostAddress() + " port = " + clientSocket.getPort() + 
							" Thread = " + getThreadName(),
							DebugChecker.DEBUG_SOCKET_CONNECTION);
				}
				changeLeaderResult = HttpHandlerWorkerOperation.promptLeaderThread();
				if(changeLeaderResult) {
					//HttpTaskHandle.httpHandleThread(clientSocket);
					clientSocket.setKeepAlive(false);
					clientSocket.setSoTimeout(keepAliveTimeOutmsec_);
					httpRequestHandle(clientSocket);
				} else {
					//Handle Full Task Response
					QasiteLogger.warn("svrSocket accept step failed");
				}
			} catch(Throwable e) {
				if(e instanceof SocketTimeoutException) {
					QasiteLogger.info("SocketTimeout : Thread=" + getThreadName());
				} else {
					QasiteLogger.warn("svrSocket accept step failed : Thread=" + getThreadName(), e);
				}
			}
			releaseAcceptTask();
//			if(changeLeaderResult) {
//				releaseAcceptTask();
//			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void httpRequestHandle(Socket socket) {
		boolean isSSL = false;
		if(socket instanceof SSLSocket) {
			socket = (SSLSocket)socket;
			isSSL = true;
		}
		boolean isWebSocketConnet = false;
//		try(InputStream is = socket.getInputStream();
//			OutputStream os = socket.getOutputStream()) {
		try(WrapperSocket wrapperSocket = WrapperSocket.createInstance(socket, isSSL)) {
			InputStream is = wrapperSocket.getInputStream();
			OutputStream os = wrapperSocket.getOutputStream();
			RequestMessage requestMessage;
			try {
				requestMessage = HttpTaskHandle.analizeRequestMessage(is, os);
			} catch(HttpRequestHandlingException he) {
				QasiteLogger.warn("HttpRequestHandlingException : Thread=" + getThreadName(), he);
				//HTTP 500
				os.write(ResponseMessageCreateHelper.createResponseMessage(ResonseStatusLine.Internal_Server_Error, "HTTP/1.1", he.getMessage()));
				os.flush();
				return;
			} catch(SocketTimeoutException e) {
				QasiteLogger.info("socket time out exeption. Thread name = : " + getThreadName(), true);
				return;
			}
			if(requestMessage == null) {
				QasiteLogger.warn("analizeRequestMessage failed");
				return;
			}
			boolean isKeepAlive = false;
			boolean isPeek = HttpHandlerWorkerOperation.isPeekExetuingThread();
			if(!isPeek) {
				isKeepAlive = HttpTaskHandle.checkedRequestKeepAlive(requestMessage);
			}
			ResponseMessage responseMessage;
			try {
				responseMessage = HttpTaskHandle.createResponseMessage(requestMessage);
			} catch(HttpRequestHandlingException he) {
				QasiteLogger.warn("HttpRequestHandlingException : Thread=" + getThreadName(), he);
				//HTTP 500
				os.write(ResponseMessageCreateHelper.createResponseMessage(ResonseStatusLine.Internal_Server_Error, "HTTP/1.1", he.getMessage()));
				os.flush();
				return;
			} catch(HttpNotPageException ne) {
				QasiteLogger.warn("HttpNotPageException exeption.", ne);
				//HTTP 404
				os.write(ResponseMessageCreateHelper.createResponseMessage(ResonseStatusLine.Not_Found, "HTTP/1.1", ne.getMessage()));
				os.flush();
				return;
			}
			if(responseMessage == null) {
				QasiteLogger.warn("createResponse failed");
				return;
			}
			os.write(responseMessage.createResponseMessage(isKeepAlive));
			os.flush();
			isWebSocketConnet = responseMessage instanceof ResponseMessageTypeWebSocket ? true : false;
			if(isWebSocketConnet) {
				wrapperSocket.setKeepConnect(true);
			}
			long keepAliveTimeStart = System.currentTimeMillis();
			while(isKeepAlive) {
				if(System.currentTimeMillis() - keepAliveTimeStart >= keepAliveTimeOutmsec_) {
					break;
				}
				RequestMessage requestMessageKeep;
				try {
					requestMessageKeep = HttpTaskHandle.analizeRequestMessage(is, os);
				} catch(HttpRequestHandlingException he) {
					QasiteLogger.warn("HttpRequestHandlingException : Thread=" + getThreadName(), he);
					//HTTP 500
					os.write(ResponseMessageCreateHelper.createResponseMessage(ResonseStatusLine.Internal_Server_Error, "HTTP/1.1", he.getMessage()));
					os.flush();
					return;
				}
				if(requestMessageKeep == null) {
					if(!isWebSocketConnet) {
						QasiteLogger.info("KILL KEEP ALIVE thread = " + getThreadName());
						break;
					}
					//check connected
					try { 
						os.write("".getBytes());
						os.flush();
					} catch(SocketTimeoutException e) {
						QasiteLogger.info("SocketTimeout : Thread=" + getThreadName());
						return;
					}
					isPeek = HttpHandlerWorkerOperation.isPeekExetuingThread();
					if(isPeek) {
						break;
					}
					continue;
				}
				isKeepAlive = HttpTaskHandle.checkedRequestKeepAlive(requestMessageKeep);
				ResponseMessage responseMessageKeepAlive;
				try {
					responseMessageKeepAlive = HttpTaskHandle.createResponseMessage(requestMessageKeep);
				} catch(HttpRequestHandlingException he) {
					QasiteLogger.warn("HttpRequestHandlingException : Thread=" + getThreadName(), he);
					//HTTP 500
					os.write(ResponseMessageCreateHelper.createResponseMessage(ResonseStatusLine.Internal_Server_Error, "HTTP/1.1", he.getMessage()));
					os.flush();
					return;
				} catch(HttpNotPageException ne) {
					QasiteLogger.warn("HttpNotPageException", ne);
					//HTTP 404
					os.write(ResponseMessageCreateHelper.createResponseMessage(ResonseStatusLine.Not_Found, "HTTP/1.1", ne.getMessage()));
					os.flush();
					return;
				}
				if(responseMessageKeepAlive == null) {
					QasiteLogger.warn("createResponse failed");
					return;
				}
				os.write(responseMessageKeepAlive.createResponseMessage(isKeepAlive));
				os.flush();
			}
			if(isWebSocketConnet) {
				boolean registBool = WebSocketManager.registSocket(wrapperSocket);
				if(!registBool) {
					wrapperSocket.setKeepConnect(false);
				}
			}
		} catch(IOException e) {
			QasiteLogger.warn("IOException. ThreadName = " + getThreadName(), e);
		} catch(Exception e) {
			QasiteLogger.warn("Exception. ThreadName = " + getThreadName(), e);
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
		//DEBUG Mode
		if(DebugChecker.isDEBUGMode()) {
			QasiteLogger.debug("worker thread = " + getThreadName() + " setting serverSocket.", DebugChecker.DEBUG_WORKER_THREAD);
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
			QasiteLogger.warn("closeClientSocket", e);
		}
	}

	private void timeSleep(int mtime) {
		try {
			Thread.sleep(mtime);
		} catch(InterruptedException e) {
			
		}
	}
}
