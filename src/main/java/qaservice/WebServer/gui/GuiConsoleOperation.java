package qaservice.WebServer.gui;

import javax.swing.JTextArea;

import qaservice.Common.charcterutil.CharUtil;

public class GuiConsoleOperation {
	private static JTextArea consoleArea_;

	static void setConsoleArea(JTextArea consoleArea) {
		consoleArea_ = consoleArea;
	}

	static void resetConsoleArea() {
		consoleArea_.setText("");
	}

	public static void writeConsoleArea(String mess) {
		consoleArea_.append(mess);
		consoleArea_.append(CharUtil.getLineSeparator());
	}
}
