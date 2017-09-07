package mainsystem;

import java.io.Serializable;

public class Mms extends Message implements Serializable {
	public byte [] image;
	
	public byte[] getImage() {
		return image;
	}
	public void setImage(byte[] image) {
		this.image = image;
	}
	
	public void setDirection (boolean direction) {
		this.direction = direction;
	}
	
	public Mms() {
		super();
	}
	public Mms(Sms sms) {
		super();
		this.timestamp = sms.timestamp;
		this.destination = sms.getDestination();
		this.source = sms.getSource();
		this.netAddr = sms.netAddr;
		this.code = sms.getCode();
		this.timestamp = sms.timestamp;
		
	}

}