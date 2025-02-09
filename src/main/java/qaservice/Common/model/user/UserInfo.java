package qaservice.Common.model.user;

public class UserInfo {
	private int userId_;
	private String userName_;
	private byte[] userPicture_;
	//private UserLevel level_;
	private int userPoint_;

	public UserInfo(int userId, String userName, byte[] userPicture, int userPoint) {
		userId_ = userId;
		userName_ = userName;
		userPicture_ = userPicture;
		//level_ = createUserLevelField(userLevel);
		userPoint_ = userPoint;
	}

	public int getUserId() {
		return userId_;
	}

	public String getUserName() {
		return userName_;
	}

	public byte[] getUserPicture() {
		return userPicture_;
	}

	public int getUserPoint() {
		return userPoint_;
	}
//	public int getUserLevelValue() {
//		return level_.getLevelValue();
//	}
//
//	private UserLevel createUserLevelField(int userLevel) {
//		for(UserLevel l : UserLevel.values()) {
//			if(l.getLevelValue() == userLevel) {
//				return l;
//			}
//		}
//		return UserLevel.BRONZE;
//	}
}
