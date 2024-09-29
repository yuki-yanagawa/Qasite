package qaservice.Common.model.user;

public enum UserLevel {
	BRONZE(0),
	SILVER(1),
	GOLD(2);
	
	private int level_;
	private UserLevel(int level) {
		level_ = level;
	}
	
	public int getLevelValue() {
		return level_;
	}
}
