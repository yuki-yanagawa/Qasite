package qaservice.LogCollector.main;

import java.nio.file.Path;
import java.nio.file.Paths;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.LogCollector.collect.LogCollectThread;
import qaservice.LogCollector.prop.LoggerPropertyReader;

public class LogCollectorConsoleStart {
	public static void main(String[] args) {
		try {
			Class.forName("qaservice.LogCollector.prop.LoggerPropertyReader");
			QasiteLogger.startLogger("LogCollect");
		} catch(ClassNotFoundException e) {
			System.exit(-1);
		} catch(Exception e) {
			System.exit(-1);
		}
		if(!LoggerPropertyReader.isReadPropertySuccess()) {
			System.exit(-1);
		}
		launchLogCollectThread();
	}

	private static void launchLogCollectThread() {
		Path collectDirPath = Paths.get(LoggerPropertyReader.getValue("collectFileDirPath").toString());
		int intervalSec = Integer.parseInt(LoggerPropertyReader.getValue("collectIntervalSec").toString());
		LogCollectThread logCollectThread = new LogCollectThread(intervalSec, collectDirPath);
		logCollectThread.start();
	}
}
