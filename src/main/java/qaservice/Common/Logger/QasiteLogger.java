package qaservice.Common.Logger;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import qaservice.Common.prop.CommonPropReader;

public class QasiteLogger {
	//Start Each Service QasiteLogger
	//private static QasiteLogger qasiteLogger = new QasiteLogger();
	private static QasiteLogger qasiteLogger = null;
	private Logger logger;
	private FileHandler fh;
	private QasiteLogger(String serviceName) throws IOException, SecurityException {
		String logLevelStr = CommonPropReader.getProperties("LOGFILE_LEVEL").toString();
		Level logLevel = Level.WARNING;
		if("INFO".equals(logLevelStr)) {
			logLevel = Level.INFO;
		}
		int limitLogFileSize = Integer.parseInt(CommonPropReader.getProperties("LOGFILE_LIMIT_SIZE_MB").toString());
		String filePattern = "log/" + serviceName + "_%g.log";
		logger = Logger.getLogger(serviceName);
		fh = new FileHandler(filePattern, limitLogFileSize * 1024 * 1024 , 10, true);
		SimpleFormatter sm = new SimpleFormatter();
		fh.setFormatter(sm);
		logger.addHandler(fh);
		logger.setLevel(logLevel);
	}

	/**
	 * this method called by each service started!!!
	 * 
	 * @param logPattern
	 */
	public static synchronized void startLogger(String serviceName) throws Exception {
		if(qasiteLogger != null) {
			return;
		}
		try {
			Class.forName("qaservice.Common.prop.CommonPropReader");
		} catch(ClassNotFoundException e) {
			throw e;
		}

		qasiteLogger = new QasiteLogger(serviceName);
	}

	public static synchronized void endLogger() {
		getInstance().logger.removeHandler(getInstance().fh);
		getInstance().fh.close();
		
	}

	private static QasiteLogger getInstance() {
		return qasiteLogger;
	}

	public static void warn(String msg) {
		getInstance().logger.warning(createMessage(msg, true));
	}

	public static void warn(String msg, Throwable e) {
		getInstance().logger.log(Level.WARNING, createMessage(msg, true), e);
	}

	public static void info(String msg) {
		getInstance().logger.info(createMessage(msg, false));
	}

	public static void info(String msg, boolean isCalledCheck) {
		getInstance().logger.info(createMessage(msg, isCalledCheck));
	}

	public static void info(String msg, Throwable e) {
		getInstance().logger.log(Level.INFO, createMessage(msg, true), e);
	}

	public static void debug(String msg) {
		debug(msg, null);
	}

	public static void debug(String msg, String separateMess) {
		getInstance().logger.info(addDebugModeComment(createMessage(msg, false), separateMess));
	}

	public static void debug(String msg ,String separateMess ,boolean isCalledCheck) {
		getInstance().logger.info(addDebugModeComment(createMessage(msg, false), separateMess));
	}

	private static String createMessage(String msg, boolean isCalledCheck) {
		if(isCalledCheck) {
			return "[ " + msg + " ]" + " at " + callMethod();
		}
		return "[ " + msg + " ]";
	}

	private static String callMethod() {
		return Thread.currentThread().getStackTrace()[4].getClassName() + "." + Thread.currentThread().getStackTrace()[4].getMethodName();
	}

	private static String addDebugModeComment(String msg, String separateMess) {
		if(separateMess == null) {
			return " ( DEBUG MODE ) " + msg;
		}
		return " ( DEBUG MODE = " + separateMess + ") " + msg;
	}
}
