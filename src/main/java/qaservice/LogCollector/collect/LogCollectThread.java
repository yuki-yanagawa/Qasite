package qaservice.LogCollector.collect;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import qaservice.Common.Logger.QasiteLogger;
import qaservice.LogCollector.prop.LoggerPropertyReader;

public class LogCollectThread extends Thread {
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmss");
	private static final String TMP_SAVED_DIR_NAME = "tmp";
	private static final String LOG_DIR = "log";
	private static final String SEPARATOR = File.separator;
	private int intervalSec;
	private Path collectFileDirPath;
	private int savedDay;
	public LogCollectThread(int intervalSec, Path collectFileDirPath) {
		this.intervalSec = intervalSec;
		this.collectFileDirPath = collectFileDirPath;
		this.savedDay = Integer.parseInt(LoggerPropertyReader.getValue("savedZipFileDay").toString());
	}

	@Override
	public void run(){
		QasiteLogger.info("Log collec start. interval second = " + String.valueOf(intervalSec));
		while(true) {
			collectLogFile();
			inervalWait();
		}
	}

	private void collectLogFile() {
		File file = collectFileDirPath.toFile();
		List<File> backUpList = new ArrayList<>();
		long checkStartTime = System.currentTimeMillis();
		for(File f : file.listFiles()) {
			if(f.isDirectory()) {
				continue;
			}
			if(!f.getName().endsWith("lck") && !f.getName().endsWith("zip") && checkStartTime > f.lastModified()) {
				backUpList.add(f);
			}
		}

		// tmp dir create
		File tmpDir = new File(LOG_DIR + SEPARATOR + TMP_SAVED_DIR_NAME);

		// copy stage To tmp dir
		try {
			copyTargetLogFile(tmpDir, backUpList);
		} catch(IOException e) {
			QasiteLogger.warn("copy stage failed.", e);
			deleteCompleteDir(tmpDir);
			return;
		}

		// zip file create from copy data
		Date date = new Date(checkStartTime);
		String dateStr = SDF.format(date);
		try {
			zipFileDataCreate(tmpDir, dateStr);
		} catch(IOException e) {
			QasiteLogger.warn("copy stage failed.", e);
			deleteCompleteDir(tmpDir);
			return;
		}

		// delete tmp dir
		deleteCompleteDir(tmpDir);

		// delete zip file
		deleteOldZipFile();
	}

	private boolean copyTargetLogFile(File tmpDir, List<File> backUpList) throws IOException {
		if(tmpDir.exists()) {
			QasiteLogger.warn("copy stage failed. tmp dir exist");
			return false;
		}

		if(!tmpDir.mkdir()) {
			QasiteLogger.warn("copy stage failed. tmp mkdir failed");
			return false;
		}

		for(File f : backUpList) {
			Path targetPath = Paths.get(tmpDir.getPath() + SEPARATOR + f.getName());
			Files.copy(f.toPath(), targetPath);
		}
		return true;
	}

	private void zipFileDataCreate(File srcDir, String dateStr) throws IOException {
		File zipFile = new File(LOG_DIR + SEPARATOR + "log_" + dateStr + ".zip");
		try(FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos)) {
			fileWriteingToZip(zos, srcDir);
		}
	}

	private void fileWriteingToZip(ZipOutputStream zos, File f) throws IOException {
		if(f.isDirectory()) {
			QasiteLogger.info("dir contain = " + f.getName(), true);
			// tmp dir is not need
			//zos.putNextEntry(new ZipEntry(f.getName() + SEPARATOR));
			for(File inf : f.listFiles()) {
				fileWriteingToZip(zos, inf);
			}
			// tmp dir is not need
			//zos.closeEntry();
		} else {
			byte[] buf = new byte[1024];
			int len;
			zos.putNextEntry(new ZipEntry(f.getName()));
			try(FileInputStream fis = new FileInputStream(f);
				BufferedInputStream bis = new BufferedInputStream(fis)) {
				while((len = bis.read(buf, 0, buf.length)) != -1) {
					zos.write(buf, 0, len);
				}
				zos.flush();
			}
			zos.closeEntry();
		}
	}

	private void deleteCompleteDir(File delFile) {
		if(delFile.isDirectory()) {
			for(File f : delFile.listFiles()) {
				deleteCompleteDir(f);
			}
			delFile.delete();
		} else {
			delFile.delete();
		}
	}

	private void deleteOldZipFile() {
		long limitCheck = this.savedDay * 24L * 60L * 60L;
		File collectDir = collectFileDirPath.toFile();
		long nowDate = System.currentTimeMillis();
		for(File f : collectDir.listFiles()) {
			if(!f.getName().endsWith("zip")) {
				continue;
			}
			if((nowDate - f.lastModified()) > limitCheck) {
				f.delete();
			}
		}
	}

	private void inervalWait() {
		try {
			Thread.sleep(intervalSec * 1000);
		} catch(InterruptedException e) {
			
		}
	}
}
