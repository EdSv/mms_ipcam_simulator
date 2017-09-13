package mainsystem;

import java.io.Serializable;

public abstract class Message  implements Serializable{
	String source;
	String destination;
	String message;
	String code;
	long timestamp;
	int netAddr;
	public boolean direction;
	//byte [] image;
	
	public String getSource() {
		return this.source;
	}
	
	public long getTimestamp() {
		return this.timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getCode() {
		return this.code;
	}
}
