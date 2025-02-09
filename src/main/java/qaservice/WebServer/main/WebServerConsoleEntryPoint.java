package qaservice.WebServer.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.WebServer.mainserver.taskhandle.http.FileManager;
import qaservice.WebServer.propreader.ServerPropKey;
import qaservice.WebServer.propreader.ServerPropReader;

public class WebServerConsoleEntryPoint {
	private static PrintStream consoleOutStream_;
	private static FileOutputStream fileOutputStream_;
	private static final String serviceName = "QAserviceWebServer";
	public static void main(String[] args) {
		try {
			//Server properties read
			Class.forName("qaservice.WebServer.propreader.ServerPropKey");
			Class.forName("qaservice.WebServer.propreader.ServerPropReader");
			//Common util read
			Class.forName("qaservice.Common.Logger.QasiteLogger");
			Class.forName("qaservice.Common.charcterutil.CharUtil");
			Class.forName("qaservice.Common.dateutil.ServerDateUtil");
			Class.forName("qaservice.Common.prop.CommonPropReader");
			Class.forName("qaservice.Common.userPoint.UserPointDefinition");
			Class.forName("qaservice.Common.userPoint.UserPointCommnucatiReciver");
			loggerSetting();
			//Console Out Setting
			settingConsoleOut();
			//Html File ReWrite
			FileManager.getInstance();
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		//WebServerStartingOperation.serverLoggerSettingOn();
		//before Qasite Logger not setting
		WebServerStartingOperation.autoStart();
	}

	private static void settingConsoleOut() throws IOException {
		Object consoleOutObj = ServerPropReader.getProperties(ServerPropKey.ConsoleOut.getKey());
		String consoleOut = "";
		if(consoleOutObj != null) {
			consoleOut = consoleOutObj.toString();
			String path = "log" + File.separator + consoleOut;
			//System.out.println(path);
			fileOutputStream_ = new FileOutputStream(path);
			consoleOutStream_ = new PrintStream(fileOutputStream_);

			System.setOut(consoleOutStream_);
			System.setErr(consoleOutStream_);
		}
	}

	private static void loggerSetting() {
		try {
			QasiteLogger.startLogger(serviceName);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
