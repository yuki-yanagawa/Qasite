package qaservice.WebServer.model.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import qaservice.Common.Logger.QasiteLogger;

public class CreateJsonData {
	public static byte[] createJsonData(Object obj) {
		ObjectMapper mapper = new ObjectMapper();
		byte[] bytes = null;
		try {
			bytes = mapper.writeValueAsBytes(obj);
		} catch (JsonProcessingException e) {
			QasiteLogger.warn("cretate json error", e);
			bytes = null;
		}
		return bytes;
	}
}
