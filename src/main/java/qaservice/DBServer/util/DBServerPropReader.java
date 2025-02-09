package qaservice.DBServer.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import qaservice.Common.Logger.QasiteLogger;

public class DBServerPropReader {
	private static final String DBSERVER_PROPERTIES_FILE = "conf/dbserver.properties";
	
	private static Properties dbprops_;
	static {
		readPropertiesFile();
	}
	
	private static void readPropertiesFile() {
		Path path = Paths.get(DBSERVER_PROPERTIES_FILE);
		dbprops_ = new Properties();
		try(FileInputStream fis = new FileInputStream(path.toFile());
			InputStreamReader isr = new InputStreamReader(fis)){
			dbprops_.load(isr);
		} catch(IOException e) {
			QasiteLogger.warn("dbserver.properties read error.", e);
		}
	}
	
	public static Object getProperties(String key) {
		return dbprops_.get(key);
	}
}
