package qaservice.Common.dateutil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import qaservice.WebServer.propreader.ServerPropKey;
import qaservice.WebServer.propreader.ServerPropReader;

public class ServerDateUtil {
	public static DateTimeFormatter dtf_;
	public static ZoneId zoneId_;
	static {
		initalize();
	}
	
	public static void initalize() {
		String timeZone = ServerPropReader.getProperties(ServerPropKey.TimeZone.getKey()).toString();
		String format = ServerPropReader.getProperties(ServerPropKey.DateFormat.getKey()).toString();
		zoneId_ = ZoneId.of(timeZone);
		dtf_ = DateTimeFormatter.ofPattern(format);
	}
	
	public static String getDateNow() {
		return dtf_.format(LocalDateTime.now(zoneId_));
	}
}
