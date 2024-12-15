package datasheet.hellostatistics;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipUtil {
	public static byte[] compressed(byte[] data) {
		try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
			GZIPOutputStream gos = new GZIPOutputStream(bos)) {
			gos.write(data);
			//gos.flush();
			gos.close();
			return bos.toByteArray();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] decompressed(byte[] data) {
		try(ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			GZIPInputStream gip = new GZIPInputStream(bis)) {
			int len;
			byte[] tmpBytes = new byte[2048];
			while((len = gip.read(tmpBytes)) != -1) {
				byteArrayOut.write(tmpBytes, 0, len);
			}
			return byteArrayOut.toByteArray();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}