package servicedispatcher;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.http.util.ByteArrayBuffer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mainsystem.GsmServer;
import mainsystem.Message;
import mainsystem.Mms;
import mainsystem.Sms;

public class SmsServiceCenter implements Runnable {
	final static  Logger logger = LogManager.getLogger(SmsServiceCenter.class.getName());
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
				}

				if (path == null) {
					this.putSmsToQueue(sms);
					continue;
				}
				
				
				byte [] image = null;
				try {
					image = Files.readAllBytes(Paths.get(".", path).toAbsolutePath());
				} catch (IOException e) {
					logger.log(Level.DEBUG, e);
				}catch (NullPointerException e){
					logger.log(Level.DEBUG, e);
				}
				
					
				Mms mms = new Mms(sms);
				if(image != null && image.length > 0) {
				mms.setImage(image);
				mms.setDirection(false);
				}
				
				GsmServer gsm = GsmServer.getInstance();
				gsm.sendMms(mms);
				
				///---------move<<<
				
//				try {
//					FileInputStream fin = new FileInputStream(path);
//					File directory = new File(sms.getSource());// directory as phoneNumber
//															
//					if (!directory.exists()) {
//						directory.mkdir();// directory.mkdirs();
//					}
//
//					OutputStream fout = new BufferedOutputStream(new FileOutputStream(
//							directory.getPath() + "/photoshot-" 
//							+ sms.getCode() + "-T" + sms.getTimestamp() + ".jpg"));
//					
//					byte [] buffer = new byte [1024];
//		         	int lengthRead;
//		         	while ((lengthRead = fin.read(buffer)) >0) {
//		         		fout.write(buffer, 0, lengthRead);
//		         		fout.flush();
//		         	}
//					fout.close();
//					fin.close();
//				} catch (FileNotFoundException e) {
//					logger.log(Level.ERROR, e);
//					this.putSmsToQueue(sms);//try again
//				} catch (IOException e) {
//					logger.log(Level.DEBUG, e);
//					this.putSmsToQueue(sms);
//				}
				
				///>>>>>>>>>>

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
