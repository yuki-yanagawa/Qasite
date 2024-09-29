package qaservice.WebServer.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import qaservice.Common.charcterutil.CharUtil;
import qaservice.Common.dateutil.ServerDateUtil;
import qaservice.WebServer.gui.GuiConsoleOperation;
import qaservice.WebServer.propreader.ServerPropKey;
import qaservice.WebServer.propreader.ServerPropReader;

public class ServerLogger {
	private static final String DIR = "log";
	private static final String FILENAME = "ServerLog_";
	private static final String EXT = ".log";
	
	private static final String INFO_HEADER = "[INFO : DATE]";
	private static final String WARN_HEADER = "[WARN : DATE] ";
	private static final String APPLICATIONLOG_HEADER = "[APPLICATION-LOG : DATE] ";

	private static ServerLogger logger_;
	private static File currentLogFile_;
	private static PrintWriter currentWriter_;
	private static LoggingLevel loggingLevel_;
	private static boolean isLogStackTrace_;
	private static enum LoggingLevel {
		INFO(1),
		WARN(2);
		private int loglevel_;
		private LoggingLevel(int loglevel) {
			loglevel_ = loglevel;
		}
		public int getLoglevel() {
			return loglevel_;
		}
	}

	private ServerLogger() {
		isLogStackTrace_ = Boolean.parseBoolean(ServerPropReader.getProperties(ServerPropKey.LogStackTrace.getKey()).toString());
		String logginglevelStr = ServerPropReader.getProperties(ServerPropKey.LoggingLevel.getKey()).toString().toUpperCase();
		for(LoggingLevel l : LoggingLevel.values()) {
			if(l.name().equals(logginglevelStr)) {
				loggingLevel_ = l;
				return;
			}
		}
		loggingLevel_ = LoggingLevel.WARN; 
	}

	public static synchronized ServerLogger getInstance() {
		if(logger_ == null) {
			logger_ = new ServerLogger();
			settingLogFile();
		}
		return logger_;
	}

	private static void settingLogFile() {
		Path logDirPath = Paths.get(DIR);
		File f = logDirPath.toFile();
		if(!f.exists() || !f.isDirectory()) {
			GuiConsoleOperation.writeConsoleArea("log folder is not exist!!");
			return;
		}
		if(f.listFiles().length == 0) {
			currentLogFile_ = linkLogFile(0);
			try {
				currentLogFile_.createNewFile();
				currentWriter_ = new PrintWriter(new BufferedWriter(new FileWriter(currentLogFile_)));
			} catch(IOException e) {
				GuiConsoleOperation.writeConsoleArea("log file create failed..");
				closeOpe();
				return;
			}
			return;
		}

		File latestUpdate = f.listFiles()[0];
		for(File tmpFile : f.listFiles()) {
			if(latestUpdate.lastModified() < tmpFile.lastModified()) {
				latestUpdate = tmpFile;
			}
		}
		currentLogFile_ = latestUpdate;
		try {
			currentWriter_ = new PrintWriter(new BufferedWriter(new FileWriter(currentLogFile_, true)));
		} catch(IOException e) {
			GuiConsoleOperation.writeConsoleArea("log file setting failed..");
			closeOpe();
			return;
		}
		
	}
	
	public synchronized void appLog(String mess) {
		if(isLogStackTrace_) {
			currentWriter_.append(getTraceData());
			currentWriter_.append(CharUtil.getLineSeparator());
		}
		currentWriter_.append(APPLICATIONLOG_HEADER.replace("DATE", ServerDateUtil.getDateNow()) + mess);
		currentWriter_.append(CharUtil.getLineSeparator());
		currentWriter_.flush();
	}
	
	public synchronized void info(String mess) {
		if(loggingLevel_.getLoglevel() > LoggingLevel.INFO.getLoglevel()) {
			return;
		}
		if(isLogStackTrace_) {
			currentWriter_.append(getTraceData());
			currentWriter_.append(CharUtil.getLineSeparator());
		}
		currentWriter_.append(INFO_HEADER.replace("DATE", ServerDateUtil.getDateNow()) + mess);
		currentWriter_.append(CharUtil.getLineSeparator());
		currentWriter_.flush();
	}
	
	public synchronized void warn(Throwable e, String mess) {
		if(loggingLevel_.getLoglevel() > LoggingLevel.WARN.getLoglevel()) {
			return;
		}
		if(isLogStackTrace_) {
			currentWriter_.append(getTraceData());
			currentWriter_.append(CharUtil.getLineSeparator());
		}
		String dateStr = ServerDateUtil.getDateNow();
		String header = WARN_HEADER.replace("DATE", dateStr);
		currentWriter_.append(header + mess);
		currentWriter_.append(CharUtil.getLineSeparator());
		String space = "";
		for(int i = 0; i < header.length(); i++) {
			space += " ";
		}
		header = header.replace(dateStr, space);
		currentWriter_.append(e.getMessage());
		currentWriter_.append(CharUtil.getLineSeparator());
		for(StackTraceElement elm : e.getStackTrace()) {
			currentWriter_.append(elm.toString());
			currentWriter_.append(CharUtil.getLineSeparator());
		}
		currentWriter_.flush();
	}

	public synchronized void warn(String mess) {
		if(loggingLevel_.getLoglevel() > LoggingLevel.WARN.getLoglevel()) {
			return;
		}
		if(isLogStackTrace_) {
			currentWriter_.append(getTraceData());
			currentWriter_.append(CharUtil.getLineSeparator());
		}
		currentWriter_.append(WARN_HEADER.replace("DATE", ServerDateUtil.getDateNow()));
		currentWriter_.append(CharUtil.getLineSeparator());
		currentWriter_.flush();
	}

	private static File linkLogFile(int index) {
		return Paths.get(DIR + "/" + FILENAME + String.valueOf(index) + EXT).toFile();
	}

	public static long notifylogFileSize() {
		try {
			return Files.size(currentLogFile_.toPath());
		} catch(IOException e) {
			GuiConsoleOperation.writeConsoleArea("log file size notify Error");
		}
		return 0;
	}
	
	private static void closeOpe() {
		if(currentWriter_ != null) {
			currentWriter_.close();
		}
	}
	
	public static void killLogSetting() {
		closeOpe();
		logger_ = null;
	}
	
	private static String getTraceData() {
		StackTraceElement elm = Thread.currentThread().getStackTrace()[3];
		return "++++++++++++++ " + elm.getClassName() + "." + elm.getMethodName() + " +++++++++++++++";
	}
}
