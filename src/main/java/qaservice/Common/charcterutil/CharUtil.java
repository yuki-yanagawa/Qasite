package qaservice.Common.charcterutil;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class CharUtil {
	private static final String LINE_SEPARATOR = System.lineSeparator();
	private static final Charset CHARSET = StandardCharsets.UTF_8;
	private static final Charset AJAX_PARAMETER = StandardCharsets.UTF_8;
	
	public static String getLineSeparator() {
		return LINE_SEPARATOR;
	}
	
	public static Charset getCharset() {
		return CHARSET;
	}

	public static Charset getAjaxCharset() {
		return AJAX_PARAMETER;
	}
	
	public static byte[] exchangeStrToByte(String str, String separator) {
		String[] splitData = str.split(separator);
		byte[] retBytes = new byte[splitData.length];
		for(int i = 0; i < splitData.length; i++) {
			retBytes[i] = Byte.parseByte(splitData[i]);
		}
		return retBytes;
	}
}
