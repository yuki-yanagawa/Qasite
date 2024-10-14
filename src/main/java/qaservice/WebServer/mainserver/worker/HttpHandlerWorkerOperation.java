package qaservice.WebServer.mainserver.worker;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import qaservice.WebServer.mainserver.IServer;

public class HttpHandlerWorkerOperation {
	private static List<HttpHandlerWorker> workerList_ = new CopyOnWriteArrayList<>();
	private static HttpHandlerWorker leader_ = null;
	private static int currentIndex_ = 0;
	private static IServer server_;
	private static int executingCount_ = 0;
	
	public static synchronized void createWorkerThreadPool(int workerThreadCount) {
		if(workerList_.size() >= workerThreadCount) {
			return;
		}
		for(int i = 0; i < workerThreadCount; i++) {
			workerList_.add(new HttpHandlerWorker("http-worker-thread-" + String.valueOf(i + 1)));
		}
		leader_ = workerList_.get(currentIndex_++);
	}
	
	public static void registerAwaitReciverSocket(IServer server) {
		server_ =  server;
		if(leader_ != null) {
			leader_.setDelegateAwaitProcess(server_);
		}
	}

	public static synchronized void deleteWorkerThreadPool() {
		leader_ = null;
		for(HttpHandlerWorker worker : workerList_) {
			killHttpHandleWorker(worker);
		}
		server_ = null;
		currentIndex_ = 0;
		workerList_.clear();
	}
	
	public static synchronized void incrementExecutingCount() {
		executingCount_++;
	}
	
	public static synchronized void decrementRequestCount() {
		executingCount_--;
	}

	private static void killHttpHandleWorker(HttpHandlerWorker worker) {
		switch(worker.getWorkerState()) {
			case WAITING : {
				worker.interrupt();
				if(server_ != null) {
					server_.killServer();
				}
				break;
			}
			case FAILED : {
				if(server_ != null) {
					server_.killServer();
				}
				worker.stopPolling();
				break;
			}
			case EXECUTING : {
				if(server_ != null) {
					server_.killServer();
				}
				worker.stopPolling();
				break;
			}
		}
	}
	
	static synchronized boolean promptLeaderThread() {
		if(workerList_.size() >= executingCount_) {
			promptLeaderThreadInside();
			return true;
		}
		return false;
	}
	
	static void promptLeaderThreadInside() {
		if(currentIndex_ >= workerList_.size()) {
			currentIndex_ = 0;
		}
		//debug
//		workerList_.forEach(e -> {
//			System.out.println(e.getName() + " : " + e.getWorkerState());
//		});
		HttpHandlerWorker tmpLeaderThread = workerList_.get(currentIndex_);
		switch(tmpLeaderThread.getWorkerState()) {
			case WAITING : {
				leader_ = tmpLeaderThread;
				leader_.setDelegateAwaitProcess(server_);
				currentIndex_++;
				return;
			}
			case FAILED : {
				tmpLeaderThread = new HttpHandlerWorker("http-worker-thread-" + String.valueOf(currentIndex_ + 1));
				leader_ = tmpLeaderThread;
				leader_.setDelegateAwaitProcess(server_);
				currentIndex_++;
				return;
			}
			case EXECUTING : {
				currentIndex_++;
				promptLeaderThreadInside();
				return;
			}
		}
	}

	static long getExecutingThreadCount() {
		return workerList_.stream().filter(e -> e.getWorkerState() == WorkerStates.EXECUTING).count();
	}

	static boolean isPeekExetuingThread() {
		return getExecutingThreadCount() >= workerList_.size() * 0.6;
	}
}
