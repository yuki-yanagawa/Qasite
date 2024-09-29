package qaservice.Common.dbaccesor.cashe;

import java.sql.Timestamp;

public class TimestampWrapCahse<T> {
	private T wrapObj_;
	private Timestamp timestamp_;
	public TimestampWrapCahse(T obj, Timestamp timestamp) {
		wrapObj_ = obj;
		timestamp_ = timestamp;
	}
	
	public void setTime(Timestamp timestamp) {
		timestamp_ = timestamp;
	}
	
	public T getWrapObj() {
		return wrapObj_;
	}

	public Timestamp getTimestamp() {
		return timestamp_;
	}
}
