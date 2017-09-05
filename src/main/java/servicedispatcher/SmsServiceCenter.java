package servicedispatcher;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mainsystem.Sms;

public class SmsServiceCenter implements Runnable {
	final static  Logger logger = LogManager.getLogger(SmsServiceCenter.class.getName());
	//private static SmsServiceCenter instance;
	// ussd
	HashMap<String, SmsService> services;
	Queue<Sms> smsQueue;

	public static SmsServiceCenter getInstance() {
		return SmsServiceCenterHolder.instanceHolder;
	}
	

	private SmsServiceCenter() {
		super();
		this.smsQueue = new ConcurrentLinkedQueue<Sms>();
		this.services = new HashMap<String, SmsService>();
	}
	
	public static class SmsServiceCenterHolder {//thread safe
		public static SmsServiceCenter instanceHolder = new SmsServiceCenter ();
	}

	public boolean putSmsToQueue(Sms sms) {
		return smsQueue.offer(sms);
	}

	public void run() {

		while (true) {
			try {
				Thread.sleep(1000l);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			if (!smsQueue.isEmpty()) {
				Sms sms = smsQueue.poll();
				String path = null;
				SmsService ss = null;
				ss = services.get("camService");
				try {
					path = (String) (ss.serve(sms));
				} catch (NullPointerException e) {
					logger.log(Level.DEBUG, e);
					logger.log(Level.TRACE, "DESTINATION:" + sms.getDestination());
				}

				try {
					if (path == null)
						continue;
					FileInputStream fin = new FileInputStream(path);
					File directory = new File(sms.getSource());// directory as phoneNumber
															
					if (!directory.exists()) {
						directory.mkdir();// directory.mkdirs();
					}

					OutputStream fout = new BufferedOutputStream(new FileOutputStream(
							directory.getPath() + "/photoshot-" 
							+ sms.getCode() + "-T" + sms.getTimestamp() + ".jpg"));
					
					byte [] buffer = new byte [1024];
		         	int lengthRead;
		         	while ((lengthRead = fin.read(buffer)) >0) {
		         		fout.write(buffer, 0, lengthRead);
		         		fout.flush();
		         	}
					fout.close();
					fin.close();
				} catch (FileNotFoundException e) {
					logger.log(Level.ERROR, e);
				} catch (IOException e) {
					logger.log(Level.DEBUG, e);
				}

			} else {
				try {
					Thread.sleep(100l);
				} catch (InterruptedException e) {
					logger.log(Level.DEBUG, e);
				}
			}
		}
	}

	public String addSmsService(SmsService service) {
		String ussdCode = "camService";
		services.put(ussdCode, service);
		return ussdCode;
	}
	
}
