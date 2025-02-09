package qaservice.DBServer.database;

public interface IDBServer {
	public void start(int port, boolean initalizedb) throws Throwable;
	public DBConnectModel getDBConnectPath();
	public void dbServerShutdown();
}
