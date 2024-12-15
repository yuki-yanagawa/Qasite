package qaservice.WebServer.mainserver.taskhandle.http;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import qaservice.Common.charcterutil.CharUtil;
import qaservice.Common.dbaccesor.AnswerTableAccessor;
import qaservice.Common.img.ImageUtil;
import qaservice.Common.mailutil.MailSendUtil;
import qaservice.Common.model.user.UserInfo;
import qaservice.Common.utiltool.GZipUtil;
import qaservice.WebServer.accessDBServer.AnswerDataLogic;
import qaservice.WebServer.accessDBServer.QuestionDataLogic;
import qaservice.WebServer.accessDBServer.UserDataLogic;
import qaservice.WebServer.logger.ServerLogger;
import qaservice.WebServer.mainserver.taskhandle.http.request.RequestHttpMethod;
import qaservice.WebServer.mainserver.taskhandle.http.request.RequestMessage;
import qaservice.WebServer.mainserver.taskhandle.http.request.RequsetHeaderType;
import qaservice.WebServer.mainserver.taskhandle.http.request.exception.HttpRouterDelegateMethodCallError;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResonseStatusLine;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseContentType;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessage;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessageTypeGetFile;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessageTypeJson;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessageTypeLeadLocation;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessageTypeNoBodyData;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessageTypeRequestBasicAuth;
import qaservice.WebServer.mainserver.taskhandle.http.response.ResponseMessageTypeWebSocket;
import qaservice.WebServer.mainserver.taskhandle.http.session.SessionOperator;

class HttpRoutingMethodList {
	
	private static enum JsonResponseCommonProtocol {
		KEY("result"),
		VALUE_SESSION_EXPIRER("sessionIdExpired"),
		VALUE_COOKIE_ISNEED("cookieIsNeed"),
		VALUE_REQUESTDATA_GETTINGERROR("failedGetRequestData"),
		VALUE_DATABASE_UPDATEERROR("failedDataBaseUpdate"),
		VALUE_DATABASE_UPDATE_SUCESS("sucsess"),
		VALUE_DATABASE_UPDATE_FAILED("failed");
		
		String commonProtocolName;
		private JsonResponseCommonProtocol(String name) {
			commonProtocolName = name;
		}
	}
//	private static final String HTMLFILE_FOLDER = "html";
	private static final String CSSFILE_FOLDER = "css";
	private static final String JSFILE_FOLDER = "javascript";
	static Map<String, byte[]> fileReadCashe_ = new HashMap<>();
	
	@HttpRoutingMarker(method=RequestHttpMethod.GET, uri="/")
	static ResponseMessage indexResponse(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		byte[] fileByteData = FileManager.getInstance().fileRead("html/index.html");
		if(fileByteData == null) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, "/ is not Found.", nowActiveMethod());
		}
		return new ResponseMessageTypeGetFile(ResponseContentType.HTML, fileByteData, requestMess.getHttpProtocol());
	}

	@HttpRoutingMarker(method=RequestHttpMethod.GET, uri="/*.js")
	static ResponseMessage javascriptResponse(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String jsPath = requestMess.getRequestPath();
		if(!jsPath.startsWith("/")) {
			jsPath = "/" + jsPath;
		}
		jsPath = JSFILE_FOLDER + jsPath;
		byte[] fileByteData = FileManager.getInstance().fileRead(jsPath);
		if(fileByteData == null) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, jsPath + "is not Found.", nowActiveMethod());
		}
		return new ResponseMessageTypeGetFile(ResponseContentType.JAVASCRIPT, fileByteData, requestMess.getHttpProtocol());
	}

	@HttpRoutingMarker(method=RequestHttpMethod.GET, uri="/images/*")
	static ResponseMessage imagesFileResponse(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String imgPath = requestMess.getRequestPath();
		if(imgPath.startsWith("/")) {
			imgPath = imgPath.substring(1);
		}
		byte[] fileByteData = FileManager.getInstance().fileRead(imgPath);
		if(fileByteData == null) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, imgPath + "is not Found.", nowActiveMethod());
		}
		ResponseContentType contentType;
		if(imgPath.toUpperCase().endsWith("PNG")) {
			contentType = ResponseContentType.PNG;
		} else if(imgPath.toUpperCase().endsWith("JPEG")) {
			contentType = ResponseContentType.JPEG; 
		} else if(imgPath.toUpperCase().endsWith("GIF")) {
			contentType = ResponseContentType.GIF;
		} else {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, imgPath + "is not Found.", nowActiveMethod());
		}
		return new ResponseMessageTypeGetFile(contentType, fileByteData, requestMess.getHttpProtocol());
	}

	@HttpRoutingMarker(method=RequestHttpMethod.GET, uri="/*.css")
	static ResponseMessage cssResponse(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String cssPath = requestMess.getRequestPath();
		if(!cssPath.startsWith("/")) {
			cssPath = "/" + cssPath;
		}
		cssPath = CSSFILE_FOLDER + cssPath;
		byte[] fileByteData = FileManager.getInstance().fileRead(cssPath);
		if(fileByteData == null) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, cssPath + "is not Found.", nowActiveMethod());
		}
		return new ResponseMessageTypeGetFile(ResponseContentType.CSS, fileByteData, requestMess.getHttpProtocol());
	}

	@HttpRoutingMarker(method=RequestHttpMethod.GET, uri="/questPostPageRequest")
	static ResponseMessage questPostPageResponse(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String cookie = requestMess.getRequestHeaderValue(RequsetHeaderType.Cookie);
		if("".equals(cookie)) {
			return new ResponseMessageTypeLeadLocation(requestMess.getHttpProtocol(), "/login");
		} else {
			String userName = SessionOperator.getUserDataFromSession(cookie);
			if(userName == null) {
				ServerLogger.getInstance().info("sessionid is " + cookie + ". this is not enable");
				return new ResponseMessageTypeLeadLocation(requestMess.getHttpProtocol(), "/login");
			}
			byte[] fileByteData = FileManager.getInstance().fileRead("html/requestPostPage.html");
			if(fileByteData == null) {
				throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, "requestPostPage is not Found.", nowActiveMethod());
			}
			return new ResponseMessageTypeGetFile(ResponseContentType.HTML, fileByteData, requestMess.getHttpProtocol());
		}
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/getQuestionAllData")
	static ResponseMessage getQuestionAllData(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		boolean enabledGzipCompressed = acceptableGZIPcompressedBodyData(requestMess);
		//DB ACCSSEOR
		byte[] answerPatternBytes = requestMess.getRequestBodyDataByKey("answerPattern", false);
		int answerPattern = 0; //ALL
		if(answerPatternBytes != null) {
			try {
				answerPattern = Integer.parseInt(new String(answerPatternBytes, CharUtil.getAjaxCharset()));
			} catch(NumberFormatException e) {
				answerPattern = 0;
			}
		}
		List<Map<String, Object>> retMapList = QuestionDataLogic.getAllQuestionTitleData(answerPattern);
		byte[] bytes = createJsonData(retMapList);
		if(enabledGzipCompressed) {
			bytes = GZipUtil.compressed(bytes);
		}
		if(bytes == null) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Internal_Server_Error, null, "response data create error.", nowActiveMethod());
		}
		return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol(), enabledGzipCompressed);
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/searchQuestionData")
	static ResponseMessage searchQuestionData(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		//DB ACCSSEOR
		byte[] answerPatternBytes = requestMess.getRequestBodyDataByKey("answerPattern", false);
		int answerPattern = 0; //ALL
		if(answerPatternBytes != null) {
			try {
				answerPattern = Integer.parseInt(new String(answerPatternBytes, CharUtil.getAjaxCharset()));
			} catch(NumberFormatException e) {
				answerPattern = 0;
			}
		}
		byte[] searchTextBytes = requestMess.getRequestBodyDataByKey("searchText", false);
		List<Map<String, Object>> retMapList = QuestionDataLogic.searchQestionData(searchTextBytes, answerPattern);
		byte[] bytes = createJsonData(retMapList);
		if(bytes == null) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Internal_Server_Error, null, "response data create error.", nowActiveMethod());
		}
		return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/requestQuestionId")
	static ResponseMessage requestQuestionId(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		UserInfo userInfo = getUserInfoFromCookie(requestMess);
		int result = QuestionDataLogic.getQuestionId(userInfo.getUserName());
		Map<String, String> postQuestionResult = new HashMap<>();
		postQuestionResult.put("questionId", String.valueOf(result));
		postQuestionResult.put("userId", String.valueOf(userInfo.getUserId()));
		byte[] bytes = createJsonData(postQuestionResult);
		return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/postQuestTextData/*")
	static ResponseMessage postQuestTextData(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		UserInfo userInfo = getUserInfoFromCookie(requestMess);
		String requestPath = requestMess.getRequestPath();
		int questionId = getIdFromRequestPath(requestPath, "/postQuestTextData/([0-9]+)");
		if(userInfo == null) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
		byte[] titleBytes = requestMess.getRequestBodyDataByKey("title", false);
		byte[] typeBytes = requestMess.getRequestBodyDataByKey("type", false);
		byte[] questionBytes = requestMess.getRequestBodyDataByKey("text", false);
		byte[] userIdBytes = requestMess.getRequestBodyDataByKey("userId", false);
		
		//title is encodeURI --- UTF-8
		String title = new String(titleBytes, CharUtil.getAjaxCharset());
		//question is encodeURI --- UTF-8
		String question = new String(questionBytes, CharUtil.getAjaxCharset());
		int type = -1;
		try {
			type = Integer.parseInt(new String(typeBytes, CharUtil.getAjaxCharset()));
		} catch(NumberFormatException e) {
			ServerLogger.getInstance().warn("questionPost error type no get error : " + new String(typeBytes, CharUtil.getAjaxCharset()));
			type = -1;
		}
		int userId = -1;
		try {
			userId = Integer.parseInt(new String(userIdBytes, CharUtil.getAjaxCharset()));
		} catch(NumberFormatException e) {
			ServerLogger.getInstance().warn("questionPost error type no get error : " + new String(userIdBytes, CharUtil.getAjaxCharset()));
			userId = -1;
		}
		if(type == -1 || userId == -1) {
			ServerLogger.getInstance().warn("userId or type getting Error");
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
		boolean result = QuestionDataLogic.insertQuestionTextData(title, question, type, userInfo, questionId);
		if(result) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.No_Content);
		} else {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/postQuestion")
	static ResponseMessage questionPost(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String cookie = requestMess.getRequestHeaderValue(RequsetHeaderType.Cookie);
		if("".equals(cookie)) {
			//Response Json
			Map<String, String> postQuestionResult = new HashMap<>();
			postQuestionResult.put(
					JsonResponseCommonProtocol.KEY.commonProtocolName, JsonResponseCommonProtocol.VALUE_COOKIE_ISNEED.commonProtocolName);
			byte[] bytes = createJsonData(postQuestionResult);
			return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
		} else {
			String userName = SessionOperator.getUserDataFromSession(cookie);
			if(userName == null) {
				ServerLogger.getInstance().info("sessionid is " + cookie + ". this is not enable");
				Map<String, String> postQuestionResult = new HashMap<>();
				postQuestionResult.put(
						JsonResponseCommonProtocol.KEY.commonProtocolName, JsonResponseCommonProtocol.VALUE_SESSION_EXPIRER.commonProtocolName);
				byte[] bytes = createJsonData(postQuestionResult);
				return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
			}
			
			byte[] titleBytes = requestMess.getRequestBodyDataByKey("title", false);
			byte[] typeBytes = requestMess.getRequestBodyDataByKey("type", false);
			byte[] questionBytes = requestMess.getRequestBodyDataByKey("text", false);
			if(questionBytes == null) {
				throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Internal_Server_Error, null, "post data getting error from requestparamters!!!!", nowActiveMethod());
			}
			int type = -1;
			try {
				type = Integer.parseInt(new String(typeBytes, CharUtil.getAjaxCharset()));
			} catch(NumberFormatException e) {
				ServerLogger.getInstance().warn("questionPost error type no get error : " + new String(typeBytes, CharUtil.getAjaxCharset()));
				type = -1;
			}
			if(type == -1) {
				ServerLogger.getInstance().warn("questionPost error type no get error");
			}
			int result = QuestionDataLogic.insertQuestionTable(titleBytes, questionBytes, userName, type);
			Map<String, String> postQuestionResult = new HashMap<>();
			postQuestionResult.put(JsonResponseCommonProtocol.KEY.commonProtocolName, String.valueOf(result));
			byte[] bytes = createJsonData(postQuestionResult);
			return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
		}
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/postQuestionImgData/*")
	static ResponseMessage postQuestionImgData(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String requestPath = requestMess.getRequestPath();
		int questionId = getIdFromRequestPath(requestPath, "/postQuestionImgData/([0-9]+)");
//		UserInfo userInfo = getUserInfoFromCookie(requestMess);
//		if(userInfo == null) {
//			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
//		}
//		boolean autentiacte = QuestionDataLogic.isPostQuestUser(questionId, userInfo.getUserId());
//		if(!autentiacte) {
//			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
//		}
		byte[] imgData = requestMess.getRequestBodyDataByKey("textImage", false);

		//Data compressed
		//imgData = GZipUtil.compressed(imgData);
		boolean result = QuestionDataLogic.insertQuestionImgData(imgData, questionId);
		if(result) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.No_Content);
		} else {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/postQuestionLinkData/*")
	static ResponseMessage postQuestionLinkData(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String requestPath = requestMess.getRequestPath();
		int questionId = getIdFromRequestPath(requestPath, "/postQuestionLinkData/([0-9]+)");
//		UserInfo userInfo = getUserInfoFromCookie(requestMess);
//		if(userInfo == null) {
//			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
//		}
//		boolean autentiacte = QuestionDataLogic.isPostQuestUser(questionId, userInfo.getUserId());
//		if(!autentiacte) {
//			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
//		}
		byte[] filenameBytes = requestMess.getRequestBodyDataByKey("linkFilename", false);
		byte[] filedataBytes = requestMess.getRequestBodyDataByKey("linkFileData", false);
		byte[] fileIdBytes = requestMess.getRequestBodyDataByKey("linkFileId", false);
		int fileId = -1;
		try {
			fileId = Integer.parseInt(new String(fileIdBytes, CharUtil.getAjaxCharset()));
		} catch(NumberFormatException e) {
			fileId = -1;
		}
		if(fileId == -1) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
		boolean result = QuestionDataLogic.insertQuestionLinkData(fileId, filenameBytes, filedataBytes, questionId);
		if(result) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.No_Content);
		} else {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
	}

	@HttpRoutingMarker(method=RequestHttpMethod.PUT, uri="/commitQuestion/*")
	static ResponseMessage commitQuestion(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String requestPath = requestMess.getRequestPath();
		int questionId = getIdFromRequestPath(requestPath, "/commitQuestion/([0-9]+)");
		UserInfo userInfo = getUserInfoFromCookie(requestMess);
		if(userInfo == null) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
		boolean autentiacte = QuestionDataLogic.isPostQuestUser(questionId, userInfo.getUserId());
		if(!autentiacte) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
		boolean result = QuestionDataLogic.commitPostQuestion(questionId);
		if(result) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.No_Content);
		} else {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
	}

	@HttpRoutingMarker(method=RequestHttpMethod.PUT, uri="/revertQuestion/*")
	static ResponseMessage revertQuestion(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String requestPath = requestMess.getRequestPath();
		int questionId = getIdFromRequestPath(requestPath, "/revertQuestion/([0-9]+)");
		UserInfo userInfo = getUserInfoFromCookie(requestMess);
		if(userInfo == null) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
		boolean autentiacte = QuestionDataLogic.isPostQuestUser(questionId, userInfo.getUserId());
		if(!autentiacte) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
		boolean result = QuestionDataLogic.revertPostQuestion(questionId);
		if(result) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.No_Content);
		} else {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
	}

	@HttpRoutingMarker(method=RequestHttpMethod.GET, uri="/getQuestionDetail-*")
	static ResponseMessage getQuestionDetailDataFormat(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String requestPath = requestMess.getRequestPath();
		int questionId = -1;
		Pattern pattern = Pattern.compile("/getQuestionDetail-([0-9]+)");
		Matcher m = pattern.matcher(requestPath);
		if(m.find()) {
			try {
				questionId = Integer.parseInt(m.group(1));
			} catch(NumberFormatException e) {
				ServerLogger.getInstance().warn(e, "get questionId error from request Param");
				questionId = -1;
			}
		}
		if(questionId < 0) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Internal_Server_Error, null, "question id getting error from requestparameter", nowActiveMethod());
		}
//		String cookie = requestMess.getRequestHeaderValue(RequsetHeaderType.Cookie);
//		byte[] fileByteData = null;
//		if(!"".equals(cookie)) {
//			String username = SessionOperator.getUserDataFromSession(cookie);
//			boolean result = QuestionTableAccessor.IsQuestionCreateUser(questionId, username);
//			if(result) {
//				fileByteData = fileRead("html/questionDetailPageForMyQuestion.html");
//			}
//		}
//		if(fileByteData == null) {
//			fileByteData = fileRead("html/questionDetailPage.html");
//		}
		byte[] fileByteData = FileManager.getInstance().fileRead("html/questionDetailPage.html");
		if(fileByteData == null) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, "questionDetailPage is not found.", nowActiveMethod());
		}
		return new ResponseMessageTypeGetFile(ResponseContentType.HTML, fileByteData, requestMess.getHttpProtocol());
	}
	
	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/getQuestionDetail/*")
	static ResponseMessage getQuestionDetailData(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		boolean enabledGzipCompressed = acceptableGZIPcompressedBodyData(requestMess);
		String requestPath = requestMess.getRequestPath();
		int questionId = -1;
		Pattern pattern = Pattern.compile("/getQuestionDetail/([0-9]+)");
		Matcher m = pattern.matcher(requestPath);
		if(m.find()) {
			try {
				questionId = Integer.parseInt(m.group(1));
			} catch(NumberFormatException e) {
				ServerLogger.getInstance().warn(e, "get questionId error from request Param");
				questionId = -1;
			}
		}
		if(questionId < 0) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Internal_Server_Error, null, "question id getting error from requestparameter", nowActiveMethod());
		}

		byte[] intextImgSizeBytes = requestMess.getRequestBodyDataByKey("intext_imgsize");
		int intextImgSize = -1;
		if(intextImgSizeBytes != null) {
			intextImgSize = Integer.parseInt(new String(intextImgSizeBytes, CharUtil.getAjaxCharset()));
		}

		Map<String, Object> detailDataMap = QuestionDataLogic.getQuestionDetailData(questionId, intextImgSize);
		byte[] bytes = createJsonData(detailDataMap);
		if(enabledGzipCompressed) {
			bytes = GZipUtil.compressed(bytes);
		}
		return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol(), enabledGzipCompressed);
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/requestAnswerId")
	static ResponseMessage requestAnswerId(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		UserInfo userInfo = getUserInfoFromCookie(requestMess);
		if(userInfo == null) {
			Map<String, String> postAnswerResult = new HashMap<>();
			postAnswerResult.put("answerId", String.valueOf(-1));
			postAnswerResult.put("userId", String.valueOf(-1));
			byte[] bytes = createJsonData(postAnswerResult);
			return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
		}
		int result = AnswerDataLogic.getAnswerId(userInfo.getUserName());
		Map<String, String> postAnswerResult = new HashMap<>();
		postAnswerResult.put("answerId", String.valueOf(result));
		postAnswerResult.put("userId", String.valueOf(userInfo.getUserId()));
		byte[] bytes = createJsonData(postAnswerResult);
		return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/postAnswerTextData/*")
	static ResponseMessage postAnswerTextData(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		UserInfo userInfo = getUserInfoFromCookie(requestMess);
		String requestPath = requestMess.getRequestPath();
		int answerId = getIdFromRequestPath(requestPath, "/postAnswerTextData/([0-9]+)");
		if(userInfo == null) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
	
		byte[] answerBytes = requestMess.getRequestBodyDataByKey("text", false);
		byte[] userIdBytes = requestMess.getRequestBodyDataByKey("userId", false);
		byte[] questionIdBytes= requestMess.getRequestBodyDataByKey("questionId", false);

		//answer is encodeURI --- UTF-8
		String answer = new String(answerBytes, CharUtil.getAjaxCharset());
		int userId = -1;
		try {
			userId = Integer.parseInt(new String(userIdBytes, CharUtil.getAjaxCharset()));
		} catch(NumberFormatException e) {
			ServerLogger.getInstance().warn("answer post error type no get error : " + new String(userIdBytes, CharUtil.getAjaxCharset()));
			userId = -1;
		}
		int questionId = -1;
		try {
			questionId = Integer.parseInt(new String(questionIdBytes, CharUtil.getAjaxCharset()));
		} catch(NumberFormatException e) {
			ServerLogger.getInstance().warn("answer post error type no get error : " + new String(questionIdBytes, CharUtil.getAjaxCharset()));
			questionId = -1;
		}
		if(userId == -1 || questionId == -1) {
			ServerLogger.getInstance().warn("userId or type getting Error");
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
		boolean result = AnswerDataLogic.insertAnswerTextData(answerId, answer, questionId, userId);
		if(result) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.No_Content);
		} else {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/postAnswer/*")
	static ResponseMessage postAnswer(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String requestPath = requestMess.getRequestPath();
		int questionId = getIdFromRequestPath(requestPath, "/postAnswer/([0-9]+)");
		if(questionId < 0) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Internal_Server_Error, null, "question id getting error", nowActiveMethod());
		}
		
		byte[] answerBytes = requestMess.getRequestBodyDataByKey("text", false);
		if(answerBytes == null) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Internal_Server_Error, null, "request data getting error", nowActiveMethod());
		}

		UserInfo userInfo = getUserInfoFromCookie(requestMess);
		if(userInfo == null) {
			Map<String, String> retMap = new HashMap<>();
			retMap.put(JsonResponseCommonProtocol.KEY.commonProtocolName
					,JsonResponseCommonProtocol.VALUE_SESSION_EXPIRER.commonProtocolName);
			byte[] bytes = createJsonData(retMap);
			return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
		}

		Map<String, String> retMap = new HashMap<>();
		
		int resultAnswerId = AnswerDataLogic.insertAnswer(answerBytes, questionId, userInfo);
		retMap.put(JsonResponseCommonProtocol.KEY.commonProtocolName, String.valueOf(resultAnswerId));
		byte[] bytes = createJsonData(retMap);
		return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/postAnswerImgData/*")
	static ResponseMessage postAnswerImgData(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String requestPath = requestMess.getRequestPath();
		int answerId = getIdFromRequestPath(requestPath, "/postAnswerImgData/([0-9]+)");
		UserInfo userInfo = getUserInfoFromCookie(requestMess);
		if(userInfo == null) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
		boolean autentiacte = AnswerDataLogic.isPostAnswerUser(answerId, userInfo.getUserId());
		if(!autentiacte) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
		byte[] imgData = requestMess.getRequestBodyDataByKey("textImage", false);
		boolean result = AnswerDataLogic.insertAnswerImgData(imgData, answerId);
		if(result) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.No_Content);
		} else {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/postAnswerLinkData/*")
	static ResponseMessage postAnswerLinkData(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String requestPath = requestMess.getRequestPath();
		int answerId = getIdFromRequestPath(requestPath, "/postAnswerLinkData/([0-9]+)");
		UserInfo userInfo = getUserInfoFromCookie(requestMess);
		if(userInfo == null) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
		boolean autentiacte = AnswerDataLogic.isPostAnswerUser(answerId, userInfo.getUserId());
		if(!autentiacte) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
		byte[] filenameBytes = requestMess.getRequestBodyDataByKey("linkFilename", false);
		byte[] filedataBytes = requestMess.getRequestBodyDataByKey("linkFileData", false);
		byte[] fileIdBytes = requestMess.getRequestBodyDataByKey("linkFileId", false);
		int fileId = -1;
		try {
			fileId = Integer.parseInt(new String(fileIdBytes, CharUtil.getAjaxCharset()));
		} catch(NumberFormatException e) {
			fileId = -1;
		}
		if(fileId == -1) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}

		boolean result = AnswerDataLogic.insertAnswerLinkData(fileId, filenameBytes, filedataBytes, answerId);
		if(result) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.No_Content);
		} else {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
	}

	@HttpRoutingMarker(method=RequestHttpMethod.PUT, uri="/commitAnswer/*")
	static ResponseMessage commitAnswer(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String requestPath = requestMess.getRequestPath();
		int answerId = getIdFromRequestPath(requestPath, "/commitAnswer/([0-9]+)");
		UserInfo userInfo = getUserInfoFromCookie(requestMess);
		if(userInfo == null) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
		boolean autentiacte = AnswerDataLogic.isPostAnswerUser(answerId, userInfo.getUserId());
		if(!autentiacte) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
		boolean result = AnswerDataLogic.commitPostAnswer(answerId);
		if(result) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.No_Content);
		} else {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
	}

	@HttpRoutingMarker(method=RequestHttpMethod.PUT, uri="/revertAnswer/*")
	static ResponseMessage revertAnswer(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String requestPath = requestMess.getRequestPath();
		int questionId = getIdFromRequestPath(requestPath, "/revertAnswer/([0-9]+)");
		UserInfo userInfo = getUserInfoFromCookie(requestMess);
		if(userInfo == null) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
		boolean autentiacte = AnswerDataLogic.isPostAnswerUser(questionId, userInfo.getUserId());
		if(!autentiacte) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
		boolean result = AnswerDataLogic.revertPostAnswer(questionId);
		if(result) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.No_Content);
		} else {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/getAnswerDetail/*")
	static ResponseMessage getAnswerDetail(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		boolean enabledGzipCompressed = acceptableGZIPcompressedBodyData(requestMess);
		String requestPath = requestMess.getRequestPath();
		int questionId = -1;
		Pattern pattern = Pattern.compile("/getAnswerDetail/([0-9]+)");
		Matcher m = pattern.matcher(requestPath);
		if(m.find()) {
			try {
				questionId = Integer.parseInt(m.group(1));
			} catch(NumberFormatException e) {
				ServerLogger.getInstance().warn(e, "get questionId error from request Param");
				questionId = -1;
			}
		}
		if(questionId < 0) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Internal_Server_Error, null, "question id getting error", nowActiveMethod());
		}
		UserInfo userInfo = null;
		try {
			userInfo = getUserInfoFromCookie(requestMess);
		} catch(HttpRouterDelegateMethodCallError e) {
			userInfo = null;
		}
		List<Map<String, Object>> retList = AnswerDataLogic.getAnswerDetailData(questionId, userInfo);
		byte[] bytes = createJsonData(retList);
		if(enabledGzipCompressed) {
			bytes = GZipUtil.compressed(bytes);
		}
		return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol(), enabledGzipCompressed);
	}
	
	@HttpRoutingMarker(method=RequestHttpMethod.GET, uri="/login")
	static ResponseMessage login(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		byte[] fileByteData = FileManager.getInstance().fileRead("html/login.html");
		if(fileByteData == null) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, "login page is not found.", nowActiveMethod());
		}
		return new ResponseMessageTypeGetFile(ResponseContentType.HTML, fileByteData, requestMess.getHttpProtocol());
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/loginInfo")
	static ResponseMessage loginInfo(RequestMessage requestMess) {
		byte[] nameBytes = requestMess.getRequestBodyDataByKey("name", true);
//		byte[] checkedBytes = requestMess.getRequestBodyDataByKey("checked");
//		boolean checked = Boolean.parseBoolean(new String(checkedBytes, CharUtil.getCharset()));
		String username = new String(nameBytes, CharUtil.getAjaxCharset());
		byte[] passwordBytes = requestMess.getRequestBodyDataByKey("password");
		String password = new String(passwordBytes, CharUtil.getAjaxCharset());
//		String password;
//		if(checked) {
//			byte[] passwordBytes = requestMess.getRequestBodyDataByKey("password", true);
//			String passwordStr = new String(passwordBytes, CharUtil.getCharset());
//			byte[] sha256Bytes = CharUtil.exchangeStrToByte(passwordStr, ",");
//			password = Base64.getEncoder().encodeToString(sha256Bytes);
//		} else {
//			byte[] passwordBytes = requestMess.getRequestBodyDataByKey("password", false);
//			password = new String(passwordBytes, CharUtil.getCharset());
//		}
		
		//DB search logic
		boolean loginResult = UserDataLogic.existUserData(username, password);
		
		//Response Json
		Map<String, Boolean> loginResultMap = new HashMap<>();
		loginResultMap.put("loginResult", loginResult);
		byte[] bytes = createJsonData(loginResultMap);
		if(!loginResult) {
			return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
		}
		return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol())
				.setSessionRegist(() -> {
					return SessionOperator.addSessionMap(username);
				});
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/actionGood/*")
	static ResponseMessage actionGood(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		UserInfo useInfo = getUserInfoFromCookie(requestMess);
		if(useInfo == null) {
			Map<String, String> retMap = new HashMap<>();
			retMap.put(JsonResponseCommonProtocol.KEY.commonProtocolName ,JsonResponseCommonProtocol.VALUE_DATABASE_UPDATE_FAILED.commonProtocolName);
			byte[] bytes = createJsonData(retMap);
			return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
		}

		String requestPath = requestMess.getRequestPath();
		int answerId = getIdFromRequestPath(requestPath, "/actionGood/([0-9]+)");
		if(answerId == -1) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Internal_Server_Error, null, "answer id getting error", nowActiveMethod());
		}

		byte[] actionBytes = requestMess.getRequestBodyDataByKey("action", false);
		int actionResult = 0;
		try {
			actionResult = Integer.parseInt(new String(actionBytes, CharUtil.getAjaxCharset()));
		} catch(NumberFormatException e) {
			ServerLogger.getInstance().warn(e, "get action result error from request Param");
			answerId = 0;
		}

		if(actionResult == 0) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Internal_Server_Error, null, "action result getting error", nowActiveMethod());
		}

		int result = AnswerTableAccessor.goodAction(answerId, useInfo, actionResult);
		Map<String, String> retMap = new HashMap<>();
		retMap.put(JsonResponseCommonProtocol.KEY.commonProtocolName ,String.valueOf(result));
		byte[] bytes = createJsonData(retMap);
		return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/actionHelpful/*")
	static ResponseMessage actionHelpful(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String cookie = requestMess.getRequestHeaderValue(RequsetHeaderType.Cookie);
		if("".equals(cookie)) {
			Map<String, String> retMap = new HashMap<>();
			retMap.put(JsonResponseCommonProtocol.KEY.commonProtocolName,
					JsonResponseCommonProtocol.VALUE_COOKIE_ISNEED.commonProtocolName);
			byte[] bytes = createJsonData(retMap);
			return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
		}
		String username = SessionOperator.getUserDataFromSession(cookie);
		if(username == null) {
			Map<String, String> retMap = new HashMap<>();
			retMap.put(JsonResponseCommonProtocol.KEY.commonProtocolName
					,JsonResponseCommonProtocol.VALUE_SESSION_EXPIRER.commonProtocolName);
			byte[] bytes = createJsonData(retMap);
			return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
		}

		String requestPath = requestMess.getRequestPath();
		int answerId = -1;
		Pattern pattern = Pattern.compile("/actionHelpful/([0-9]+)");
		Matcher m = pattern.matcher(requestPath);
		if(m.find()) {
			try {
				answerId = Integer.parseInt(m.group(1));
			} catch(NumberFormatException e) {
				ServerLogger.getInstance().warn(e, "get answer id error from request Param");
				answerId = -1;
			}
		}

		if(answerId == -1) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Internal_Server_Error, null, "answer id getting error", nowActiveMethod());
		}

		byte[] actionBytes = requestMess.getRequestBodyDataByKey("action", false);
		int actionResult = 0;
		try {
			actionResult = Integer.parseInt(new String(actionBytes, CharUtil.getAjaxCharset()));
		} catch(NumberFormatException e) {
			ServerLogger.getInstance().warn(e, "get action result error from request Param");
			answerId = 0;
		}

		if(actionResult == 0) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Internal_Server_Error, null, "action result getting error", nowActiveMethod());
		}

		int result = AnswerDataLogic.helpfulAction(answerId, username, actionResult);
		Map<String, String> retMap = new HashMap<>();
		retMap.put(JsonResponseCommonProtocol.KEY.commonProtocolName ,String.valueOf(result));
		byte[] bytes = createJsonData(retMap);
		return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
	}

	@HttpRoutingMarker(method=RequestHttpMethod.GET, uri="/newUserRegisterPage")
	static ResponseMessage newUserRegisterPage(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		byte[] fileByteData = FileManager.getInstance().fileRead("html/userRegister.html");
		if(fileByteData == null) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, "/ is not Found.", nowActiveMethod());
		}
		return new ResponseMessageTypeGetFile(ResponseContentType.HTML, fileByteData, requestMess.getHttpProtocol());
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/userRegister")
	static ResponseMessage userRegister(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		byte[] nameBytes = requestMess.getRequestBodyDataByKey("userName");
		byte[] passwordBytes = requestMess.getRequestBodyDataByKey("digest");
		byte[] emailTextBytes = requestMess.getRequestBodyDataByKey("mailText");
		String usernameByBase64 = new String(nameBytes, CharUtil.getAjaxCharset());
		String username = new String(Base64.getDecoder().decode(usernameByBase64), CharUtil.getAjaxCharset());
		String passwordStr = new String(passwordBytes, CharUtil.getAjaxCharset());
		String emailText = new String(emailTextBytes, CharUtil.getAjaxCharset());

		//exist email Address
		boolean isExistEmailAddress = false;
		try {
			isExistEmailAddress = UserDataLogic.existEmailAddress(emailText);
		} catch(SQLException e) {
			ServerLogger.getInstance().warn(e ,"user table accsess error to get email address");
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Internal_Server_Error, null, "check exist address", nowActiveMethod());
		}
		if(isExistEmailAddress) {
			Map<String, String> retMap = new HashMap<>();
			retMap.put(JsonResponseCommonProtocol.KEY.commonProtocolName, "mailExist");
			byte[] bytes = createJsonData(retMap);
			return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
		}
		boolean checkUserNameExsist = UserDataLogic.exisitUserName(username);
		if(checkUserNameExsist) {
			Map<String, String> retMap = new HashMap<>();
			retMap.put(JsonResponseCommonProtocol.KEY.commonProtocolName, "usernameExist");
			byte[] bytes = createJsonData(retMap);
			return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
		}

		boolean registResult = UserDataLogic.registUser(username, passwordStr, emailText);
		if(!registResult) {
			Map<String, String> retMap = new HashMap<>();
			retMap.put(JsonResponseCommonProtocol.KEY.commonProtocolName
					,JsonResponseCommonProtocol.VALUE_DATABASE_UPDATE_FAILED.commonProtocolName);
			byte[] bytes = createJsonData(retMap);
			return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
		}
		Map<String, String> registUserResult = new HashMap<>();
		registUserResult.put(
				JsonResponseCommonProtocol.KEY.commonProtocolName, JsonResponseCommonProtocol.VALUE_DATABASE_UPDATE_SUCESS.commonProtocolName);
		byte[] bytes = createJsonData(registUserResult);
		return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol())
				.setSessionRegist(() -> {
					return SessionOperator.addSessionMap(username);
				});
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/getLoginUserInfo")
	static ResponseMessage getLoginUserInfo(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String cookie = requestMess.getRequestHeaderValue(RequsetHeaderType.Cookie);
		if("".equals(cookie)) {
			Map<String, String> retMap = new HashMap<>();
			retMap.put(JsonResponseCommonProtocol.KEY.commonProtocolName, "");
			byte[] bytes = createJsonData(retMap);
			return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
		}
		String userName = SessionOperator.getUserDataFromSession(cookie);
		if(userName == null) {
			Map<String, String> retMap = new HashMap<>();
			retMap.put(JsonResponseCommonProtocol.KEY.commonProtocolName, "");
			byte[] bytes = createJsonData(retMap);
			return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
		}
		UserInfo userInfo = UserDataLogic.getUserInfoDataByUsername(userName);
		if(userInfo == null) {
			Map<String, String> retMap = new HashMap<>();
			retMap.put(JsonResponseCommonProtocol.KEY.commonProtocolName, "");
			byte[] bytes = createJsonData(retMap);
			return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
		}
		Map<String, Object> retMap = new HashMap<>();
		Map<String, Object> innerMap = new HashMap<>();
		innerMap.put("username", userInfo.getUserName());
		innerMap.put("userId", userInfo.getUserId());
		innerMap.put("userPicture", userInfo.getUserPicture());
		retMap.put(JsonResponseCommonProtocol.KEY.commonProtocolName, innerMap);
		byte[] bytes = createJsonData(retMap);
		return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
	}

	@HttpRoutingMarker(method=RequestHttpMethod.GET, uri="/userInfoPage-*")
	static ResponseMessage userInfoPage(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String requestPath = requestMess.getRequestPath();
		int userId = -1;
		Pattern pattern = Pattern.compile("/userInfoPage-([0-9]+)");
		Matcher m = pattern.matcher(requestPath);
		if(m.find()) {
			try {
				userId = Integer.parseInt(m.group(1));
			} catch(NumberFormatException e) {
				ServerLogger.getInstance().warn(e, "get questionId error from request Param");
				userId = -1;
			}
		}
		if(userId < 0) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Internal_Server_Error, null, "user id getting error from requestparameter", nowActiveMethod());
		}

		byte[] fileByteData = FileManager.getInstance().fileRead("html/userInfoPage.html");
		if(fileByteData == null) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, "questionDetailPage is not found.", nowActiveMethod());
		}
		return new ResponseMessageTypeGetFile(ResponseContentType.HTML, fileByteData, requestMess.getHttpProtocol());
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/getUserInfo/*")
	static ResponseMessage getUserInfo(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String requestPath = requestMess.getRequestPath();
		int userId = -1;
		Pattern pattern = Pattern.compile("/getUserInfo/([0-9]+)");
		Matcher m = pattern.matcher(requestPath);
		if(m.find()) {
			try {
				userId = Integer.parseInt(m.group(1));
			} catch(NumberFormatException e) {
				ServerLogger.getInstance().warn(e, "get user id error from request Param");
				userId = -1;
			}
		}
		if(userId < 0) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Internal_Server_Error, null, "user id getting error from requestparameter", nowActiveMethod());
		}
		UserInfo userInfo = UserDataLogic.getUserInfoDataByUserId(userId);
		if(userInfo == null) {
			Map<String, String> retMap = new HashMap<>();
			retMap.put(JsonResponseCommonProtocol.KEY.commonProtocolName, "");
			byte[] bytes = createJsonData(retMap);
			return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
		}
		boolean myInfoPage = false;
		String cookie = requestMess.getRequestHeaderValue(RequsetHeaderType.Cookie);
		if(!"".equals(cookie)) {
			String userName = SessionOperator.getUserDataFromSession(cookie);
			if(userName != null && userName.equals(userInfo.getUserName())) {
				myInfoPage = true;
			}
		}
		Map<String, Object> retMap = new HashMap<>();
		Map<String, Object> innerMap = new HashMap<>();
		innerMap.put("username", userInfo.getUserName());
		innerMap.put("userId", userInfo.getUserId());
		innerMap.put("userPicture", userInfo.getUserPicture());
		innerMap.put("userLevel", userInfo.getUserLevelValue());
		innerMap.put("myInfoPage", myInfoPage);
		retMap.put(JsonResponseCommonProtocol.KEY.commonProtocolName, innerMap);
		byte[] bytes = createJsonData(retMap);
		return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/getUserIntroduction/*")
	static ResponseMessage getUserIntroduction(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String requestPath = requestMess.getRequestPath();
		int userId = getIdFromRequestPath(requestPath, "/getUserIntroduction/([0-9]+)");
		if(userId < 0) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Internal_Server_Error, null, "user id getting error from requestparameter", nowActiveMethod());
		}
		String userIntroductionData = UserDataLogic.getUserIntroductionData(userId);
		if(userIntroductionData == null) {
			Map<String, String> retMap = new HashMap<>();
			retMap.put(JsonResponseCommonProtocol.KEY.commonProtocolName, "");
			byte[] bytes = createJsonData(retMap);
			return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
		}
		Map<String, Object> retMap = new HashMap<>();
		Map<String, Object> innerMap = new HashMap<>();
		innerMap.put("introduction", userIntroductionData);
		retMap.put(JsonResponseCommonProtocol.KEY.commonProtocolName, innerMap);
		byte[] bytes = createJsonData(retMap);
		return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
	}

	@HttpRoutingMarker(method=RequestHttpMethod.GET, uri="/userInfoRepairPage")
	static ResponseMessage userInfoRepairPage(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		//String requestPath = requestMess.getRequestPath();
		int userId = -1;
		try {
			userId = Integer.parseInt(requestMess.getQueryParameter().get("userId"));
		} catch(NumberFormatException e) {
			userId = -1;
		}
		if(userId < 0) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, "this page is not opened.", nowActiveMethod());
		}

		String cookie = requestMess.getRequestHeaderValue(RequsetHeaderType.Cookie);
		if("".equals(cookie)) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, "this page is not opened.", nowActiveMethod());
		} 
		String userName = SessionOperator.getUserDataFromSession(cookie);
		UserInfo userInfo = UserDataLogic.getUserInfoDataByUserId(userId);
		if(userName == null || !userName.equals(userInfo.getUserName())) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, "this page is not opened.", nowActiveMethod());
		}
		byte[] fileByteData = FileManager.getInstance().fileRead("html/userInfoRepairPage.html");
		if(fileByteData == null) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, "userInfoRepairPage is not found.", nowActiveMethod());
		}
		return new ResponseMessageTypeGetFile(ResponseContentType.HTML, fileByteData, requestMess.getHttpProtocol());
	}

//	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/updateUserInfo")
//	static ResponseMessage updateUserInfo(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
//		byte[] userIdByte = requestMess.getRequestBodyDataByKey("userId", false);
//		byte[] usernameByte = requestMess.getRequestBodyDataByKey("username", false);
//		byte[] introductionTextByte = requestMess.getRequestBodyDataByKey("introductionText", false);
//		byte[] pictureByte = requestMess.getRequestBodyDataByKey("picture", false);
//		int userId = -1;
//		try {
//			userId = Integer.parseInt(new String(userIdByte, CharUtil.getCharset()));
//		} catch(NumberFormatException e) {
//			userId = -1;
//		}
//		if(userId < 0) {
//			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, "user infomation can not update.", nowActiveMethod());
//		}
//
//		String cookie = requestMess.getRequestHeaderValue(RequsetHeaderType.Cookie);
//		if("".equals(cookie)) {
//			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, "user infomation can not update.", nowActiveMethod());
//		} 
//		String userName = SessionOperator.getUserDataFromSession(cookie);
//		UserInfo userInfo = UserTableAccessor.getUserInfoDataByUserId(userId);
//		if(userName == null || !userName.equals(userInfo.getUserName())) {
//			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, "user infomation can not update.", nowActiveMethod());
//		}
//
//		String username = new String(usernameByte, CharUtil.getCharset());
//		String picture = new String(pictureByte, CharUtil.getCharset());
//		Map<String, Object> retMap = new HashMap<>();
//		byte[] bytes = createJsonData(retMap);
//		return new ResponseMessage(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
//	}

	@HttpRoutingMarker(method=RequestHttpMethod.PUT, uri="/updateUserName")
	static ResponseMessage updateUserName(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		int userId = getUserIdFromRequestMessage(requestMess);
		boolean identifyResult = identificationFromRequestMessage(requestMess, userId);
		if(!identifyResult) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Bad_Request, null, "identification failed.", nowActiveMethod());
		}

		byte[] usernameByte = requestMess.getRequestBodyDataByKey("username", false);
		String updateUserName = new String(usernameByte, CharUtil.getAjaxCharset());

		boolean result = UserDataLogic.updateUserName(updateUserName, userId);
		if(result) {
			//Session data UPDATE !!!!!
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.No_Content)
					.setSessionRegist(() -> {
						return SessionOperator.addSessionMap(updateUserName);
					});
		} else {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
	}

	@HttpRoutingMarker(method=RequestHttpMethod.PUT, uri="/updateUserIntroduction")
	static ResponseMessage updateUserIntroduction(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		int userId = getUserIdFromRequestMessage(requestMess);
		boolean identifyResult = identificationFromRequestMessage(requestMess, userId);
		if(!identifyResult) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Bad_Request, null, "identification failed.", nowActiveMethod());
		}
		byte[] introductionByte = requestMess.getRequestBodyDataByKey("userIntroduction", false);
		boolean result = UserDataLogic.updateUserIntroduction(userId, introductionByte);
		if(result) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.No_Content);
		} else {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
	}

	@HttpRoutingMarker(method=RequestHttpMethod.GET, uri="/inlineAnswerFrame")
	static ResponseMessage inlineAnswerFramePageResponse(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		byte[] fileByteData = FileManager.getInstance().fileRead("html/inlineAnswerFrame.html");
		if(fileByteData == null) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, "/ is not Found.", nowActiveMethod());
		}
		return new ResponseMessageTypeGetFile(ResponseContentType.HTML, fileByteData, requestMess.getHttpProtocol());
	}

	@HttpRoutingMarker(method=RequestHttpMethod.GET, uri="/inlineEndDummy")
	static ResponseMessage inlineEndDummy(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		return new ResponseMessageTypeGetFile(ResponseContentType.HTML, null, requestMess.getHttpProtocol());
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/getAnswerImgAndLinkData/*")
	static ResponseMessage getAnswerImgAndLinkData(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		boolean enabledGzipCompressed = acceptableGZIPcompressedBodyData(requestMess);
		String requestPath = requestMess.getRequestPath();
		int answerId = getIdFromRequestPath(requestPath, "/getAnswerImgAndLinkData/([0-9]+)");
		if(answerId < 0) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Internal_Server_Error, null, "user id getting error from requestparameter", nowActiveMethod());
		}
		byte[] intextImgSizeBytes = requestMess.getRequestBodyDataByKey("intext_imgsize");
		int intextImgSize = -1;
		if(intextImgSizeBytes != null) {
			intextImgSize = Integer.parseInt(new String(intextImgSizeBytes, CharUtil.getAjaxCharset()));
		}
		Map<String, Object> allRetData = new HashMap<>();
		allRetData.putAll(AnswerDataLogic.getAnswerImageData(answerId, intextImgSize));
		allRetData.putAll(AnswerDataLogic.getAnswerLinkFileData(answerId));
		byte[] bytes = createJsonData(allRetData);
		if(enabledGzipCompressed) {
			bytes = GZipUtil.compressed(bytes);
		}
		return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol(), enabledGzipCompressed);
	}

	@HttpRoutingMarker(method=RequestHttpMethod.GET, uri="/admin")
	static ResponseMessage adminPage(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String autorization = requestMess.getRequestHeaderValue(RequsetHeaderType.Authorization);
		if("".equals(autorization)) {
			return new ResponseMessageTypeRequestBasicAuth(requestMess.getHttpProtocol());
		}
		int index = autorization.toUpperCase().indexOf("BASIC");
		if(index < 0) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Bad_Request, null, "basic relm failed", nowActiveMethod());
		}
		String base64Format = autorization.substring(index + 5).trim();
		byte[] tmp = Base64.getDecoder().decode(base64Format);
		String strFormat = new String(tmp, CharUtil.getCharset());
		String userName = strFormat.split(":")[0].trim();
		String passWord = strFormat.split(":")[1].trim();
		boolean adminFlg = false;
		if("qasiteAdmin".equals(userName) && "qasiteAdmin".equals(passWord)) {
			adminFlg = true;
		}
		if(!adminFlg) {
			return new ResponseMessageTypeRequestBasicAuth(requestMess.getHttpProtocol());
		}
		byte[] fileByteData = FileManager.getInstance().fileRead("html/adminpage.html");
		if(fileByteData == null) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, "/ is not Found.", nowActiveMethod());
		}
		return new ResponseMessageTypeGetFile(ResponseContentType.HTML, fileByteData, requestMess.getHttpProtocol());
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/getAdminSettingFiled")
	static ResponseMessage getAdminSettingFiled(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		Path path = Paths.get("conf/app.properties");
		Properties prop = new Properties();
		try(FileInputStream fis = new FileInputStream(path.toFile());
			InputStreamReader isr = new InputStreamReader(fis)) {
			prop.load(isr);
		} catch(IOException e) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Internal_Server_Error, null, "properties file read error", nowActiveMethod());
		}
		Map<String, String> propertiesMap = new HashMap<>();
		prop.keySet().forEach(e -> {
			propertiesMap.put(String.valueOf(e), String.valueOf(prop.get(e)));
		});
		byte[] bytes = createJsonData(propertiesMap);
		return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
	}

	@HttpRoutingMarker(method=RequestHttpMethod.PUT, uri="/updateFieldData")
	static ResponseMessage updateFieldData(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String autorization = requestMess.getRequestHeaderValue(RequsetHeaderType.Authorization);
		if("".equals(autorization)) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Bad_Request, null, "basic relm failed", nowActiveMethod());
		}
		int index = autorization.toUpperCase().indexOf("BASIC");
		if(index < 0) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Bad_Request, null, "basic relm failed", nowActiveMethod());
		}
		String base64Format = autorization.substring(index + 5).trim();
		byte[] tmp = Base64.getDecoder().decode(base64Format);
		String strFormat = new String(tmp, CharUtil.getCharset());
		String userName = strFormat.split(":")[0].trim();
		String passWord = strFormat.split(":")[1].trim();
		boolean adminFlg = false;
		if("qasiteAdmin".equals(userName) && "qasiteAdmin".equals(passWord)) {
			adminFlg = true;
		}
		if(!adminFlg) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Bad_Request, null, "basic relm failed", nowActiveMethod());
		}

		Path path = Paths.get("conf/app.properties");
		Properties prop = new Properties();
		try(FileInputStream fis = new FileInputStream(path.toFile());
			InputStreamReader isr = new InputStreamReader(fis)) {
			prop.load(isr);
		} catch(IOException e) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Internal_Server_Error, null, "properties file read error", nowActiveMethod());
		}
		for(Object key : prop.keySet().toArray()) {
			String keyStr = (String)key;
			byte[] bytesData = requestMess.getRequestBodyDataByKey(keyStr);
			if(bytesData == null || bytesData.length == 0) {
				continue;
			}
			FileManager.getInstance().updateProperties(keyStr, new String(bytesData, CharUtil.getAjaxCharset()));
		}
		boolean result = FileManager.getInstance().overwritePropertiesFile();
		if(result) {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.No_Content);
		} else {
			return new ResponseMessageTypeNoBodyData(requestMess.getHttpProtocol(), ResonseStatusLine.Conflict);
		}
	}

	@HttpRoutingMarker(method=RequestHttpMethod.GET, uri="/getQuestionImgRawData/*")
	static ResponseMessage getQuestionImgRawData(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String requestPath = requestMess.getRequestPath();
		int questionId = getIdFromRequestPath(requestPath, "/getQuestionImgRawData/([0-9]+)");

		boolean enabledGzipCompressed = acceptableGZIPcompressedBodyData(requestMess);
		Map<String, Object> retMap = QuestionDataLogic.getQutestionImgRawData(questionId);
		String base64ContainsHeaderStr = (String)retMap.get("questionImageData");
		ResponseContentType responseContents = createImgResponseContentsByBase64ImgHeader(base64ContainsHeaderStr);
		String base64ImgBodyData = base64ContainsHeaderStr.split(",")[1].trim();
		byte[] imgByteData = Base64.getDecoder().decode(base64ImgBodyData);
		if(enabledGzipCompressed) {
			imgByteData = GZipUtil.compressed(imgByteData);
		}
		return new ResponseMessageTypeGetFile(responseContents, imgByteData, requestMess.getHttpProtocol(), enabledGzipCompressed);
	}

	@HttpRoutingMarker(method=RequestHttpMethod.GET, uri="/getAnswerImgRawData/*")
	static ResponseMessage getAnswerImgRawData(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String requestPath = requestMess.getRequestPath();
		int answerId = getIdFromRequestPath(requestPath, "/getAnswerImgRawData/([0-9]+)");

		boolean enabledGzipCompressed = acceptableGZIPcompressedBodyData(requestMess);
		Map<String, Object> retMap = AnswerDataLogic.getAnswerImageData(answerId);
		String base64ContainsHeaderStr = (String)retMap.get("answerImgData");
		ResponseContentType responseContents = createImgResponseContentsByBase64ImgHeader(base64ContainsHeaderStr);
		String base64ImgBodyData = base64ContainsHeaderStr.split(",")[1].trim();
		byte[] imgByteData = Base64.getDecoder().decode(base64ImgBodyData);
		if(enabledGzipCompressed) {
			imgByteData = GZipUtil.compressed(imgByteData);
		}
		return new ResponseMessageTypeGetFile(responseContents, imgByteData, requestMess.getHttpProtocol(), enabledGzipCompressed);
	}

	@HttpRoutingMarker(method=RequestHttpMethod.GET, uri="/upgradeWebSocketConneting")
	static ResponseMessage upgradeWebSocketConneting(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
//		UserInfo userInfo = getUserInfoFromCookie(requestMess);
//		if(userInfo == null) {
//			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Bad_Request, null, "websocket is need login", nowActiveMethod());
//		}
		boolean isWebSocket = checkWebSocketConnectRequest(requestMess);
		String webSocketKey = requestMess.getRequestHeaderValue(RequsetHeaderType.SecWebSocketKey);
		if(webSocketKey == null) {
			isWebSocket = false;
		}
		if(!isWebSocket) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Bad_Request, null, "websocket parameter is not correct", nowActiveMethod());
		}
		
		return new ResponseMessageTypeWebSocket(webSocketKey, requestMess.getHttpProtocol());
	}

	/* This is Test Code = tabaccoRoom */
	@HttpRoutingMarker(method=RequestHttpMethod.GET, uri="/tobaccoRoom")
	static ResponseMessage tobaccoRoom(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		byte[] fileByteData = FileManager.getInstance().fileRead("html/tabaccoRoom.html");
		if(fileByteData == null) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, "/ is not Found.", nowActiveMethod());
		}
		return new ResponseMessageTypeGetFile(ResponseContentType.HTML, fileByteData, requestMess.getHttpProtocol());
	}

	/* This is Test Code = mailtestpage */
	@HttpRoutingMarker(method=RequestHttpMethod.GET, uri="/mailtestpage")
	static ResponseMessage mailtestpage(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		byte[] fileByteData = FileManager.getInstance().fileRead("html/mailtestpage.html");
		if(fileByteData == null) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, "/ is not Found.", nowActiveMethod());
		}
		return new ResponseMessageTypeGetFile(ResponseContentType.HTML, fileByteData, requestMess.getHttpProtocol());
	}

	/* This is Test Code = maittestpost */
	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/maittestpost")
	static ResponseMessage maittestpost(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		byte[] mailaddressBytes = requestMess.getRequestBodyDataByKey("mailaddress", false);
		byte[] textBytes = requestMess.getRequestBodyDataByKey("text", false);

		String mailaddress = new String(mailaddressBytes, CharUtil.getAjaxCharset());
		String text = new String(textBytes, CharUtil.getAjaxCharset());

		MailSendUtil mailSendUtil = new MailSendUtil();
		boolean result = mailSendUtil.sendMail(mailaddress, text);

		byte[] retData;
		if(result) {
			retData = "<h1>MailSendOK!!</h1>".getBytes();
		} else {
			retData = "<h1>MailSendNG...</h1>".getBytes();
		}
		return new ResponseMessageTypeGetFile(ResponseContentType.HTML, retData, requestMess.getHttpProtocol());
	}

	/* This is Test Code = imgtestpage */
	@HttpRoutingMarker(method=RequestHttpMethod.GET, uri="/imgtestpage")
	static ResponseMessage imgtestpage(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		byte[] fileByteData = FileManager.getInstance().fileRead("html/imgfileTest.html");
		if(fileByteData == null) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Not_Found, null, "/ is not Found.", nowActiveMethod());
		}
		return new ResponseMessageTypeGetFile(ResponseContentType.HTML, fileByteData, requestMess.getHttpProtocol());
	}

	/* This is Test Code = imgtestpage */
	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/imgtestpost")
	static ResponseMessage imgtestpost(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		byte[] filenameBytes = requestMess.getRequestBodyDataByKey("text");
		String filename = new String(filenameBytes, CharUtil.getAjaxCharset());
		byte[] imgBytes = requestMess.getRequestBodyDataByKey("url");
		String imgRawStr = new String(imgBytes, CharUtil.getAjaxCharset());
		System.out.println(filename);
		String[] imgRawStrLine = imgRawStr.split(",");
		String tmpHead = imgRawStrLine[0];
		String tmpBase64ImgData = imgRawStrLine[1];
		byte[] decodeData = Base64.getDecoder().decode(tmpBase64ImgData);
		Map<String, Object> retData = new HashMap<>();
		try {
			byte[] resizeData = ImageUtil.resizeSquare(decodeData, 200, ImageUtil.FileType.PNG);
			String retBase64 = Base64.getEncoder().encodeToString(resizeData);
			retData.put("resizeData", tmpHead + "," + retBase64);
		} catch(IOException e) {
			e.printStackTrace();
			retData = new HashMap<>();
		}
		byte[] bytes = createJsonData(retData);
		return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
	}

	@HttpRoutingMarker(method=RequestHttpMethod.POST, uri="/requestResizeDataImg")
	static ResponseMessage requestResizeDataImg(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		byte[] imgBytes = requestMess.getRequestBodyDataByKey("imgData");
		byte[] dataSizeBytes = requestMess.getRequestBodyDataByKey("dataSize");
		String imgRawStr = new String(imgBytes, CharUtil.getAjaxCharset());
		String dataSizeStr = new String(dataSizeBytes, CharUtil.getAjaxCharset());
		int dataSize = Integer.parseInt(dataSizeStr);
		String[] imgRawStrLine = imgRawStr.split(",");
		String tmpHead = imgRawStrLine[0];
		String tmpBase64ImgData = imgRawStrLine[1];
		byte[] decodeData = Base64.getDecoder().decode(tmpBase64ImgData);
		Map<String, Object> retData = new HashMap<>();
		try {
			byte[] resizeData = ImageUtil.resizeSquare(decodeData, dataSize, ImageUtil.getFileTypeFromImgHeader(tmpHead));
			String retBase64 = Base64.getEncoder().encodeToString(resizeData);
			retData.put("resizeData", tmpHead + "," + retBase64);
		} catch(IOException e) {
			e.printStackTrace();
			retData = new HashMap<>();
		}
		byte[] bytes = createJsonData(retData);
		return new ResponseMessageTypeJson(ResponseContentType.JSON, bytes, requestMess.getHttpProtocol());
		
	}

	private static byte[] createJsonData(Object obj) {
		ObjectMapper mapper = new ObjectMapper();
		byte[] bytes = null;
		try {
			bytes = mapper.writeValueAsBytes(obj);
		} catch (JsonProcessingException e) {
			ServerLogger.getInstance().warn("cretate json error");
			bytes = null;
		}
		return bytes;
	}
	
	private static String nowActiveMethod() {
		StackTraceElement se = Thread.currentThread().getStackTrace()[2];
		return se.getClassName() + "." + se.getMethodName();
	}

	private static int getUserIdFromRequestMessage(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		byte[] userIdByte = requestMess.getRequestBodyDataByKey("userId", false);
		int userId = -1;
		try {
			userId = Integer.parseInt(new String(userIdByte, CharUtil.getCharset()));
		} catch(NumberFormatException e) {
			userId = -1;
		}
		if(userId < 0) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Bad_Request, null, "user id can not get from requestmessage.", nowActiveMethod());
		}
		return userId;
	}

	private static boolean identificationFromRequestMessage(RequestMessage requestMess, int userId) throws HttpRouterDelegateMethodCallError {
		String cookie = requestMess.getRequestHeaderValue(RequsetHeaderType.Cookie);
		if("".equals(cookie)) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Bad_Request, null, "requset message does not include cookie parameter", nowActiveMethod());
		}
		String usernameFromSession = SessionOperator.getUserDataFromSession(cookie);

		UserInfo userInfo = UserDataLogic.getUserInfoDataByUserId(userId);
		if(usernameFromSession == null || userInfo == null || !usernameFromSession.equals(userInfo.getUserName())) {
			return false;
		}
		return true;
	}

	private static int getIdFromRequestPath(String requestPath, String matchPattern) {
		int id = -1;
		Pattern pattern = Pattern.compile(matchPattern);
		Matcher m = pattern.matcher(requestPath);
		if(m.find()) {
			try {
				id = Integer.parseInt(m.group(1));
			} catch(NumberFormatException e) {
				ServerLogger.getInstance().warn(e, "get id error from request Param");
				id = -1;
			}
		}
		return id;
	}

	private static UserInfo getUserInfoFromCookie(RequestMessage requestMess) throws HttpRouterDelegateMethodCallError {
		String cookie = requestMess.getRequestHeaderValue(RequsetHeaderType.Cookie);
		if("".equals(cookie)) {
			throw new HttpRouterDelegateMethodCallError(ResonseStatusLine.Bad_Request, null, "cookie is not exisit.", nowActiveMethod());
		} 
		String userName = SessionOperator.getUserDataFromSession(cookie);
		if(userName == null) {
			return null;
		}
		return UserDataLogic.getUserInfoDataByUsername(userName);
	}

	private static boolean acceptableGZIPcompressedBodyData(RequestMessage requestMess) {
		String acceptableEncondingDataStr = requestMess.getRequestHeaderValue(RequsetHeaderType.AcceptEncoding);
		if(acceptableEncondingDataStr == null) {
			return false;
		}
		for(String acceptEncoding : acceptableEncondingDataStr.split(",")) {
			if("GZIP".equals(acceptEncoding.trim().toUpperCase())) {
				return true;
			}
		}
		return false;
	}

	private static boolean checkWebSocketConnectRequest(RequestMessage requestMess) {
		String connection = requestMess.getRequestHeaderValue(RequsetHeaderType.Connection);
		if(connection == null || !"UPGRADE".equals(connection.toUpperCase())) {
			return false;
		}
		String upgrade = requestMess.getRequestHeaderValue(RequsetHeaderType.Upgrade);
		if(upgrade == null || !"WEBSOCKET".equals(upgrade.toUpperCase())) {
			return false;
		}
		return true;
	}

	private static ResponseContentType createImgResponseContentsByBase64ImgHeader(String base64RawData) {
		switch(ImageUtil.getFileTypeFromImgHeader(base64RawData.split(",")[0])) {
		case PNG:
			return ResponseContentType.PNG;
		case JPEG:
			return ResponseContentType.JPEG;
		default:
			return ResponseContentType.PNG;
		}
	}
}
