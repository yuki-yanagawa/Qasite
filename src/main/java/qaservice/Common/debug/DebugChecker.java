package qaservice.Common.debug;

public class DebugChecker {
	public static final String DEBUG_DB_CONNECTION = "DBCONNECTION";
	public static final String DEBUG_WORKER_THREAD = "WORKERTHREAD";
	public static final String DEBUG_SOCKET_CONNECTION = "SOCKETCONNECTION";
	private static final boolean DEBUG;
	static {
		DEBUG = Boolean.parseBoolean(String.valueOf(System.getProperty("qaservice.Common.debug.DebugChecker")));
	}

	public static boolean isDEBUGMode() {
		return DEBUG;
	}
}
