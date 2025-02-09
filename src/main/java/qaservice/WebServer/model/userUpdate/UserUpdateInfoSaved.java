package qaservice.WebServer.model.userUpdate;

import java.io.Serializable;

import qaservice.WebServer.model.accessDBServer.UserDataLogic;

public class UserUpdateInfoSaved implements Serializable{
	private static final long serialVersionUID = -1112530667087024479L;
	private int userid;
	private String username;
	private byte[] introText;
	private byte[] pictureData;
	UserUpdateInfoSaved(int userid, String username, byte[] introText, byte[] pictureData) {
		this.userid = userid;
		this.username = username;
		this.introText = introText;
		this.pictureData = pictureData;
	}

	int getUserId() {
		return this.userid;
	}

	String getUsername() {
		return this.username;
	}

	byte[] getIntroText() {
		return this.introText;
	}

	byte[] getPictureData() {
		return this.pictureData;
	}

	public boolean updateUserIntrodution() {
		if(introText == null) {
			return true;
		}
		return UserDataLogic.spUpdateUserIntroduction(this.userid, this.introText);
	}

	public boolean updateUserPictureData() {
		if(pictureData == null) {
			return true;
		}
		return UserDataLogic.spUpdateUserPicture(this.userid, this.pictureData);
	}
}
