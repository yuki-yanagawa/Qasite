package qaservice.DBServer.database;

public class DBConnectModel {
	private final String connectPath;
	private final String name;
	private final String pass;
	public DBConnectModel(String connectPath, String name, String pass) {
		this.connectPath = connectPath;
		this.name = name;
		this.pass = pass;
	}

	public String getConnectPath() {
		return this.connectPath;
	}

	public String getName() {
		return this.name;
	}

	public String getPass() {
		return this.pass;
	}
}
