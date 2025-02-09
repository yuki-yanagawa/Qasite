package qaservice.Common.userPoint;

import qaservice.Common.prop.CommonPropReader;

public class UserPointDefinition {
	public static final int POST_QUESTION;
	public static final int POST_ANSWER;
	public static final int GOOD_ACTION;
	public static final int HELPFUL_ACTION;
	static {
		POST_QUESTION = Integer.parseInt(CommonPropReader.getProperties("POST_QUESTTION_POINT").toString());
		POST_ANSWER = Integer.parseInt(CommonPropReader.getProperties("POST_ANSWER_POINT").toString());
		GOOD_ACTION = Integer.parseInt(CommonPropReader.getProperties("GOOD_ACTION_POINT").toString());
		HELPFUL_ACTION = Integer.parseInt(CommonPropReader.getProperties("HELPFUL_ACTION_POINT").toString());
	}
}
