package qaservice.Common.prop;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import qaservice.Common.Logger.QasiteLogger;

public class CommonPropReader {
private static final String COMMON_PROPERTIES_FILE = "conf/common.properties";
	
	private static Properties commpnprops;
	static {
		readPropertiesFile();
	}
	
	private static void readPropertiesFile() {
		Path path = Paths.get(COMMON_PROPERTIES_FILE);
		commpnprops = new Properties();
		try(FileInputStream fis = new FileInputStream(path.toFile());
			InputStreamReader isr = new InputStreamReader(fis)){
			commpnprops.load(isr);
		} catch(IOException e) {
			QasiteLogger.warn("common.properties file read error.", e);
		}
	}
	
	public static Object getProperties(String key) {
		return commpnprops.get(key);
	}
}
