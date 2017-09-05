package mainsystem;

class Mms extends Message {
	public Mms() {
		super();
	}
	public Mms(Sms sms) {
		super();
		this.timestamp = sms.timestamp;
		// copy source and dest
	}

}