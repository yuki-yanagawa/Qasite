package qaservice.LogCollector.prop;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class LoggerPropertyReader {
	private static final String PROP_FILE_PATH = "conf/loggercollect.properties";
	private static final Properties prop;
	static {
		prop = readPropertyFile();
	}

	public static boolean isReadPropertySuccess() {
		if(prop == null) {
			return false;
		}
		return true;
	}

	public static Object getValue(String key) {
		return prop.get(key);
	}

	private static Properties readPropertyFile() {
		File file = new File(PROP_FILE_PATH);
		Properties tmpProp = new Properties();
		try(FileInputStream fis = new FileInputStream(file);
			InputStreamReader reader = new InputStreamReader(fis)) {
			tmpProp.load(reader);
		} catch(IOException e) {
			return null;
		}
		return tmpProp;
	}
}
