package qaservice.WebServer.mainserver.taskhandle.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import qaservice.Common.charcterutil.CharUtil;

public class FileManager {
	private static class FileCasheHolder {
		private byte[] fileData;
		private FileTime fileTime;
		FileCasheHolder(byte[] fileData, FileTime fileTime) {
			this.fileData = fileData;
			this.fileTime = fileTime;
		}
		
		byte[] getFileData() {
			return fileData;
		}
		
		FileTime getFileTime() {
			return fileTime;
		}
	}

	private static final String SETTING_PROPERTIES = "conf/app.properties";
	private static final String GLOBAL_VALIABLE_JS = "javascript/globalDef.js";
	private static Map<String, FileCasheHolder> fileReadCashe_ = new HashMap<>();
	private static Properties prop_;
	private static FileManager fileReadManager_ = new FileManager();

	private FileManager() {
		refreshProperties();
		htmlFileSettingCash();
	}
	
	public static synchronized FileManager getInstance() {
		if(fileReadManager_ == null) {
			fileReadManager_ = new FileManager();
		}
		return fileReadManager_;
	}

	private synchronized void refreshProperties() {
		if(prop_ == null) {
			prop_ = new Properties();
		}
		Path path = Paths.get(SETTING_PROPERTIES);
		try(FileInputStream fis = new FileInputStream(path.toFile());
			InputStreamReader isr = new InputStreamReader(fis)) {
			prop_.load(isr);
		} catch(IOException e) {
			e.printStackTrace();
		}
		writeGolbalDefnitionJs();
	}

	public void updateProperties(String key, String value) {
		if(prop_ == null) {
			return;
		}
		prop_.put(key, value);
	}

	public boolean overwritePropertiesFile() {
		if(prop_ == null) {
			return false;
		}
		Path path = Paths.get(SETTING_PROPERTIES);
		String separator = System.lineSeparator();
		try(FileOutputStream fos = new FileOutputStream(path.toFile());
			OutputStreamWriter osw = new OutputStreamWriter(fos)){
			for(Entry<Object, Object> entry : prop_.entrySet()) {
				osw.write(entry.getKey() + "=" + entry.getValue() + separator);
			}
			osw.flush();
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}
		htmlFileSettingCash();
		writeGolbalDefnitionJs();
		globalJsSettingCash();
		return true;
	}

	private void htmlFileSettingCash() {
		Path path = Paths.get("html");
		for(File f : path.toFile().listFiles()) {
			String filePath = "html/" + f.getName();
			try(FileInputStream fis = new FileInputStream(f)) {
				long size = Files.size(f.toPath());
				byte[] fileByteData = new byte[(int)size];
				fis.read(fileByteData);
				byte[] replaceFD = replaceFileByteData(fileByteData);
				setCashReqdingFileData(filePath, replaceFD);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void globalJsSettingCash() {
		Path path = Paths.get(GLOBAL_VALIABLE_JS);
		try(FileInputStream fis = new FileInputStream(path.toFile())) {
			long size = Files.size(path);
			byte[] fileByteData = new byte[(int)size];
			fis.read(fileByteData);
			setCashReqdingFileData(GLOBAL_VALIABLE_JS, fileByteData);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private byte[] replaceFileByteData(byte[] fileData) {
		String tmp = new String(fileData, CharUtil.getCharset());
		String companyNameURLDecode = "QASite";
		String siteNameURLDecode = "SiteName";
		try {
			companyNameURLDecode = URLDecoder.decode((String)prop_.getProperty("companyName"), CharUtil.getCharset().toString());
			siteNameURLDecode = URLDecoder.decode((String)prop_.getProperty("siteName"), CharUtil.getCharset().toString());
		}catch(UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String replaceStr = tmp.replaceAll("\\(@companyName\\)", companyNameURLDecode);
		replaceStr = replaceStr.replaceAll("\\(@siteName\\)", siteNameURLDecode);
		return replaceStr.getBytes(CharUtil.getCharset());
	}

	private void setCashReqdingFileData(String filePath, byte[] readData) {
		try {
			FileTime fileTime = Files.getLastModifiedTime(Paths.get(filePath));
			fileReadCashe_.put(filePath, new FileCasheHolder(readData, fileTime));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	byte[] fileRead(String filePath) {
		Path path = Paths.get(filePath);
		if(!Files.exists(path)) {
			return null;
		}

		byte[] cashedData = getFileDataFromCashe(filePath);
		if(cashedData != null) {
			return cashedData;
		}

		try(FileInputStream fis = new FileInputStream(path.toFile())){
			long size = Files.size(path);
			byte[] fileByteData = new byte[(int)size];
			fis.read(fileByteData);
			if(path.toString().endsWith("html")) {
				fileByteData = replaceFileByteData(fileByteData);
			}
			setCashReqdingFileData(filePath, fileByteData);
			return fileByteData;
		} catch(IOException e) {
			return null;
		}
	}

	private byte[] getFileDataFromCashe(String key) {
		FileCasheHolder fileCasheHolder = fileReadCashe_.get(key);
		if(fileCasheHolder == null) {
			return null;
		}
		try {
			if(fileCasheHolder.getFileTime().compareTo(Files.getLastModifiedTime(Paths.get(key))) >= 0) {
				return fileCasheHolder.getFileData();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void writeGolbalDefnitionJs() {
		if(prop_ == null) {
			return;
		}
		Path path = Paths.get(GLOBAL_VALIABLE_JS);
		String separator = System.lineSeparator();
		try(FileOutputStream fos = new FileOutputStream(path.toFile());
			OutputStreamWriter osw = new OutputStreamWriter(fos)) {
			osw.write("var categoRizeObj = new Object();" + separator);
			for(Entry<Object, Object> entry : prop_.entrySet()) {
				String key = entry.getKey().toString();
				Object value = entry.getValue();
				if(!isOverriteData(key, value)) {
					continue;
				}
				if(key.matches("^categoRize.*")) {
					osw.write("categoRizeObj[\'" + entry.getKey() + "\'] = window.decodeURI(\'" + entry.getValue() + "\');" + separator);
				}
				if("cmdLineAddStr".equals(key)) {
					osw.write("var cmdLineAddStr = window.decodeURI(\'" + entry.getValue() + "\');" + separator);
				}
			}
			osw.flush();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isOverriteData(String key, Object value) {
		if(key.matches("^categoRize.*") && !"".equals(value)) {
			return true;
		}
		if("cmdLineAddStr".equals(key)) {
			return true;
		}
		return false;
	}
}
