package mainsystem;

import java.io.Serializable;

public class Sms extends Message implements Serializable {
	private static final long serialVersionUID = 5246511297006028670L;
	//	String code;

	public Sms(String destination, String code) {
		super();
		this.destination = destination;
		this.code = code;
		this.direction = true;
	}

	public String getCode() {
		return code;
	}
	
	public String getDestination() {
		return this.destination;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getSource() {
		return source;
	}
	
	
}