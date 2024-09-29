package qaservice.DBServer.main;

import qaservice.DBServer.gui.DBServerGuiMainFrame;

public class DBServerMainGuiStart {
	private static DBServerGuiMainFrame dbServerGuiMainFrame_;
	public static void main(String[] args) {
		dbServerGuiMainFrame_ = new DBServerGuiMainFrame();
		dbServerGuiMainFrame_.start();
	}

	public static void guiConsoleOut(String text) {
		dbServerGuiMainFrame_.guiConsoleOut(text);
	}
}
