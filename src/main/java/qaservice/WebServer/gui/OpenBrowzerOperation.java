package qaservice.WebServer.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

public class OpenBrowzerOperation {
	public static boolean openWebBrowser(int port) {
		boolean result = false;
		try {
			result = openChromeBrowser(port);
		} catch(IOException e) {
			result = false;
			e.printStackTrace();
		}
		if(result) {
			return result;
		}
		
		if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(new URI("http://localhost:" + port));
			} catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	private static boolean openChromeBrowser(int port) throws IOException {
    	Map<String, String> env = System.getenv();
    	String osName = System.getProperty("os.name");
        if(String.valueOf(osName).toUpperCase().startsWith("WINDOWS")) {
        	String programFiles = env.get("ProgramFiles");
        	String tmpPath = "";
        	if(programFiles == null) return false;
        	File fl = new File(programFiles);
        	for(File f : fl.listFiles()) {
        		if(f.getName().toUpperCase().equals("GOOGLE")) {
        			tmpPath = f.getAbsolutePath();
        			break;
        		}
        	}
        	if("".equals(tmpPath)) return false;

        	fl = new File(tmpPath);
        	tmpPath = "";
        	for(File f : fl.listFiles()) {
        		if(f.getName().toUpperCase().equals("CHROME")) {
        			tmpPath = f.getAbsolutePath();
        			break;
        		}
        	}
        	if("".equals(tmpPath)) return false;
        	
        	fl = new File(tmpPath);
        	tmpPath = "";
        	for(File f : fl.listFiles()) {
        		if(f.getName().toUpperCase().equals("APPLICATION")) {
        			tmpPath = f.getAbsolutePath();
        			break;
        		}
        	}
        	if("".equals(tmpPath)) return false;
        	
        	fl = new File(tmpPath);
        	tmpPath = "";
        	for(File f : fl.listFiles()) {
        		if(f.getName().toUpperCase().equals("CHROME.EXE")) {
        			tmpPath = f.getAbsolutePath();
        			break;
        		}
        	}
        	if("".equals(tmpPath)) return false;
        	
            ProcessBuilder p = new ProcessBuilder(tmpPath, "http://localhost:" + port);
            p.start();
        } else if(String.valueOf(osName).toUpperCase().startsWith("MAC")) {
        	ProcessBuilder p = new ProcessBuilder("open -a \"Google Chrome\" http://localhost:" + port);
            p.start();
		} else {
        	return false;
        }
        return true;
    }
}
