package mainsystem;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import org.apache.http.util.ByteArrayBuffer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.mom.kafka.KafkaManager;
import org.zeromq.ZMQ;
import com.esotericsoftware.kryo.*;
import com.esotericsoftware.kryo.io.Output;

import servicedispatcher.SmsServiceCenter;

public class Abonent implements Runnable {
	static final Logger logger = LogManager.getLogger();
	final static SmsServiceCenter smsCenter = SmsServiceCenter.getInstance();
	private static int count = 0;
	String phoneNumber;
	private int port;
	private BaseStation baseStation;
	private byte [] MMSstore;
	
	static class netModule {
		static ZMQ.Context context = ZMQ.context(1);
	}
	
	 public boolean sendSms(Sms sms) {
		sms.source = this.phoneNumber;
		sms.netAddr = this.port;
		ZMQ.Socket requestor = netModule.context.socket(ZMQ.REQ);
		ByteArrayOutputStream bSms = serialize(sms);
			boolean con = requestor.connect("tcp://localhost:5555");
			requestor.send(bSms.toByteArray());
			byte [] answ = requestor.recv(0);
			requestor.close();
		return true;
	}

	public int getPort() {
		return port;
	}

	public Abonent() {
		this.phoneNumber = "(096) 300-900-" + (700 + count++);
		this.baseStation = BaseStation.getInstance();
		this.port = 5560 + count;//port for mms
	}

	public void run() {
		Thread.currentThread().setName(Abonent.class.getSimpleName() + count);
		logger.log(Level.TRACE, ">>>>>>>>>>>> RUNNING thread:" +Thread.currentThread().getName());
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		Random random = new Random();
		int qntSms = 5;// each will send 5 sms 
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket responder = context.socket(ZMQ.REP);
		responder.bind("tcp://localhost:" + this.port);
		
		logger.log(Level.TRACE, "^^^^^^^^^^^^^^^ ABONENT #" + count + "  " + this.phoneNumber);
		while (!Thread.currentThread().isInterrupted()){
			if (qntSms > 0) {
				Sms sms = new Sms("camService", "id" + (random.nextInt(3) % 3 + 1));
				sms.netAddr = this.getPort();
				sendSms(new Sms("camService", "id" + (random.nextInt(3) % 3 + 1)));
			}
			

			byte[] bMms = responder.recv(0);
			Object ob = deserialize(bMms);
			Mms mms = null;
			if(ob instanceof Mms) {
				mms = (Mms)ob;
				System.out.println("MMS sise:  " + mms.getImage().length);
				this.MMSstore = mms.getImage();
				saveToLocalstore(mms);
			} 
			
			//responder.send("ok".getBytes());
			qntSms--;
		}
		responder.close();
		context.term();
	}
	
	private ByteArrayOutputStream serialize(Sms sms) {
		ByteArrayOutputStream bSms = new ByteArrayOutputStream();
		ObjectOutputStream ob;
		
		try {
			ob = new ObjectOutputStream(bSms);
			ob.writeObject(sms);
			ob.close();
			
		} catch (IOException e) {
			logger.log(Level.DEBUG, e);
		}
		return bSms;
	}
	
	private Object deserialize (byte [] data) {
		ObjectInputStream ob = null;
		Object message = null;
		try {
			 ob = new ObjectInputStream(new ByteArrayInputStream(data));
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			message = ob.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return message;
	}
	
	private void saveToLocalstore(Mms mms){
		try {
			File directory = new File(mms.getSource());// directory as phoneNumber
			if (!directory.exists()) {
				directory.mkdir();
			}

			OutputStream fout = new BufferedOutputStream(new FileOutputStream(
					directory.getPath() + "/photoshot-" 
					+ mms.getCode() + "-T" + mms.getTimestamp() + ".jpg"));
			
			fout.write(mms.getImage());
			
			fout.close();
		} catch (FileNotFoundException e) {
			logger.log(Level.ERROR, e);
		} catch (IOException e) {
			logger.log(Level.DEBUG, e);
		}
		
	}
	
}