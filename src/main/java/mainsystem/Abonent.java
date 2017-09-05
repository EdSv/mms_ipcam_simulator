package mainsystem;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import servicedispatcher.SmsServiceCenter;

public class Abonent {
	static final Logger logger = LogManager.getLogger();
	final static SmsServiceCenter smsCenter = SmsServiceCenter.getInstance();
	private static int count = 0;
	String phoneNumber;

	public boolean sendSms(Sms sms) {
		sms.source = this.phoneNumber;
		logger.log(Level.TRACE, sms.destination + ": get cam #" + sms.getCode());

		return smsCenter.putSmsToQueue(sms);
	}

	public Abonent() {
		this.phoneNumber = "(096) 300-900-" + (700 + count++);
	}
}