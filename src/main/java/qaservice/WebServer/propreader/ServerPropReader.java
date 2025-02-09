package qaservice.WebServer.propreader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import qaservice.Common.Logger.QasiteLogger;

public class ServerPropReader {
	private static final String SERVER_PROPERTIES_FILE = "conf/server.properties";
	
	private static Properties props_;
	private static final boolean resultReadProperites;
	static {
		resultReadProperites = readPropertiesFile();
	}

	public static boolean isReadPropertiesSuccess() {
		return resultReadProperites;
	}

	public static Object getProperties(String key) {
		Object value = props_.get(key);
		if(value == null) {
			QasiteLogger.info("server.properties key is not exisit =" + key);
		}
		return value;
	}
	
	private static boolean readPropertiesFile() {
		Path path = Paths.get(SERVER_PROPERTIES_FILE);
		props_ = new Properties();
		try(FileInputStream fis = new FileInputStream(path.toFile());
			InputStreamReader isr = new InputStreamReader(fis)){
			props_.load(isr);
		} catch(IOException e) {
			//ServerLogger.getInstance().warn("read properties error");
			return false;
		}
		return true;
	}
}
