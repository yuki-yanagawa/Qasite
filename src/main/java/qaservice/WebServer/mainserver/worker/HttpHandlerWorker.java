package qaservice.WebServer.mainserver.worker;

import java.net.Socket;

import qaservice.WebServer.logger.ServerLogger;
import qaservice.WebServer.mainserver.IServer;
import qaservice.WebServer.mainserver.taskhandle.http.HttpTaskHandle;

class HttpHandlerWorker extends Thread {
	private IServer server_;
	private WorkerStates state_;
	private boolean polling_;
	private String threadName_;
	private Thread runningThread_;
	HttpHandlerWorker(String threadName) {
		threadName_ = threadName;
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
					HttpTaskHandle.httpHandleThread(clientSocket);
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
}
