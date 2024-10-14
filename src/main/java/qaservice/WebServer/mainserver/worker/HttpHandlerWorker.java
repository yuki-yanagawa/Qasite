package qaservice.WebServer.mainserver.worker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLSocket;

import qaservice.Common.socketutil.WrapperSocket;
import qaservice.WebServer.logger.ServerLogger;
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
			// For Web Socket Connecting Change. Because socket want to connect enable keeping.
			//try(Socket clientSocket = server_.awaitRequest();){
			try {
				Socket clientSocket = server_.awaitRequest();
				changeLeaderResult = HttpHandlerWorkerOperation.promptLeaderThread();
				if(changeLeaderResult) {
					//HttpTaskHandle.httpHandleThread(clientSocket);
					clientSocket.setKeepAlive(false);
					clientSocket.setSoTimeout(keepAliveTimeOutmsec_);
					httpRequestHandle(clientSocket);
				} else {
					//Handle Full Task Response
					System.err.println("promptthread failed");
				}
			} catch(Throwable e) {
				//e.printStackTrace();
				ServerLogger.getInstance().warn(e, "svrSocket accept step failed");
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
				//HTTP 500
				os.write(ResponseMessageCreateHelper.createResponseMessage(ResonseStatusLine.Internal_Server_Error, "HTTP/1.1", he.getMessage()));
				os.flush();
				return;
			}
			if(requestMessage == null) {
				System.err.println("analizeRequestMessage failed");
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
				he.printStackTrace();
				//HTTP 500
				os.write(ResponseMessageCreateHelper.createResponseMessage(ResonseStatusLine.Internal_Server_Error, "HTTP/1.1", he.getMessage()));
				os.flush();
				return;
			} catch(HttpNotPageException ne) {
				ne.printStackTrace();
				//HTTP 404
				os.write(ResponseMessageCreateHelper.createResponseMessage(ResonseStatusLine.Not_Found, "HTTP/1.1", ne.getMessage()));
				os.flush();
				return;
			}
			if(responseMessage == null) {
				System.err.println("createResponse failed");
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
					//HTTP 500
					os.write(ResponseMessageCreateHelper.createResponseMessage(ResonseStatusLine.Internal_Server_Error, "HTTP/1.1", he.getMessage()));
					os.flush();
					return;
				}
				if(requestMessageKeep == null) {
					//check connected
					try { 
						os.write("".getBytes());
						os.flush();
					} catch(SocketTimeoutException e) {
						e.printStackTrace();
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
					he.printStackTrace();
					//HTTP 500
					os.write(ResponseMessageCreateHelper.createResponseMessage(ResonseStatusLine.Internal_Server_Error, "HTTP/1.1", he.getMessage()));
					os.flush();
					return;
				} catch(HttpNotPageException ne) {
					ne.printStackTrace();
					//HTTP 404
					os.write(ResponseMessageCreateHelper.createResponseMessage(ResonseStatusLine.Not_Found, "HTTP/1.1", ne.getMessage()));
					os.flush();
					return;
				}
				if(responseMessageKeepAlive == null) {
					System.err.println("createResponse failed");
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
			e.printStackTrace();
//			String[] data = ((SSLSocket)socket).getSupportedCipherSuites();
//			for(String d : data) {
//				System.out.println(d);
//			}
			
		} catch(Exception e) {
			e.printStackTrace();
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

	private void timeSleep(int mtime) {
		try {
			Thread.sleep(mtime);
		} catch(InterruptedException e) {
			
		}
	}
}
