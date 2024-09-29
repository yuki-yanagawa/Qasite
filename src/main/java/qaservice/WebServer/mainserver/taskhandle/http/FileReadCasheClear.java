package qaservice.WebServer.mainserver.taskhandle.http;

public class FileReadCasheClear {
	public static void crearFileReadCashe() {
		if(!"datasheet.hellostatistics.gui.GuiMainFrame"
				.equals(Thread.currentThread().getStackTrace()[2].getClassName())) {
			return;
		}
		HttpRoutingMethodList.fileReadCashe_.clear();
	}
}
