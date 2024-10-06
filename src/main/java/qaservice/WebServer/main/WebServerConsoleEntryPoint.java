package qaservice.WebServer.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import qaservice.WebServer.propreader.ServerPropKey;
import qaservice.WebServer.propreader.ServerPropReader;

public class WebServerConsoleEntryPoint {
	private static PrintStream consoleOutStream_;
	private static FileOutputStream fileOutputStream_;
	public static void main(String[] args) {
		try {
			//Server properties read
			Class.forName("qaservice.WebServer.propreader.ServerPropKey");
			Class.forName("qaservice.WebServer.propreader.ServerPropReader");
			//Console Out Setting
			settingConsoleOut();
			//Common util read
			Class.forName("qaservice.Common.charcterutil.CharUtil");
			Class.forName("qaservice.Common.dateutil.ServerDateUtil");
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		WebServerStartingOperation.serverLoggerSettingOn();
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
}
