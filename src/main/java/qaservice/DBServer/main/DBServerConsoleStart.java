package qaservice.DBServer.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import qaservice.DBServer.util.DBServerPropReader;

public class DBServerConsoleStart {
	private static PrintStream consoleOutStream_;
	private static FileOutputStream fileOutputStream_;
	public static void main(String[] args) {
		try {
			settingConsoleOut();
			new DBServerMain().main(new String[]{"fromConsole"});
		} catch(Exception exeception) {
			exeception.printStackTrace();
		}
	}

	private static void settingConsoleOut() throws IOException {
		Object consoleOutObj = DBServerPropReader.getProperties("consoleOut");
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
