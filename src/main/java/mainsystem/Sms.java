package mainsystem;

public class Sms extends Message {
	private String code;
	
	
//	String source;
//	String destination;
//	String message;
//	long timestamp;

	public Sms(String destination, String code) {
		super();
		this.destination = destination;
		this.code = code;
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