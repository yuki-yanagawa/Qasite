package qaservice.WebServer.mainserver.taskhandle.http.response;

public enum ResonseStatusLine {
	OK(200),
	Created(201),
	No_Content(204),
	Moved_Permanently(301),
	See_Other(303),
	Bad_Request(400),
	Unauthorized(401),
	Authorization_Required(401),
	Not_Found(404),
	Conflict(409),
	Internal_Server_Error(500);
	
	
	private int code_;
	private ResonseStatusLine(int code) {
		code_ = code;
	}
	private int getCode() {
		return code_;
	}
	
	String createStatusLine(String httpPlotocol) {
		String responseResultStr = this.name().replaceAll("_", " ");
		String code = String.valueOf(this.getCode());
		return httpPlotocol + " " + code + " " + responseResultStr + "\r\n";
	}
}
