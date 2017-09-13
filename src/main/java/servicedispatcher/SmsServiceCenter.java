package servicedispatcher;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import mainsystem.GsmServer;
import mainsystem.Mms;
import mainsystem.Sms;

public class SmsServiceCenter implements Runnable {
	final static Logger logger = LogManager.getLogger(SmsServiceCenter.class.getName());
	HashMap<String, ZMQService> services;
	Queue<Sms> smsQueue;

	public static SmsServiceCenter getInstance() {
		return SmsServiceCenterHolder.instanceHolder;
	}

	private SmsServiceCenter() {
		super();
		this.smsQueue = new ConcurrentLinkedQueue<Sms>();
		this.services = new HashMap<String, ZMQService>();
	}

	public static class SmsServiceCenterHolder {// thread safe
		public static SmsServiceCenter instanceHolder = new SmsServiceCenter();
	}

	public boolean putSmsToQueue(Sms sms) {
		return smsQueue.offer(sms);
	}

	private ZMsg requestImageFromIPCam(String code) {
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket ipCamPhotoRequestor = context.socket(ZMQ.REQ);
		ipCamPhotoRequestor.connect("tcp://localhost:5070");//hardcoded, better pull from ZMQService 
		ipCamPhotoRequestor.send(code);// get [timestamp, image] from ipcam

		// ZFrame f = new ZFrame(data);
		// ZFrame.recvFrame(ipCamPhotoRequestor, 0);

		ZMsg m = ZMsg.recvMsg(ipCamPhotoRequestor, 0);
		ipCamPhotoRequestor.close();
		return m;
	}

	private Mms prepareMMS(ZMsg zmsg, Sms sms) {
		ZFrame timestampFr = zmsg.getFirst();
		long timestamp = Long.parseLong(timestampFr.getData().toString(), 10);
		ZFrame imageFr = zmsg.getLast();
		byte[] imageForMMS = imageFr.getData();

		Mms mms = new Mms(sms);
		mms.image = imageForMMS;
		mms.setTimestamp(timestamp);
		return mms;
	}

	public void run() {
		logger.log(Level.TRACE, ">>>>>>>>>>>> RUNNING thread:" + Thread.currentThread().getName());
		GsmServer gsm = GsmServer.getInstance();
		while (true) {
			try {
				Thread.sleep(1000l);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			if (!smsQueue.isEmpty()) {
				Sms sms = smsQueue.poll();
				//ZMQService ss = services.get("camService");
				//ss.serve(sms);
				
				ZMsg zmsg = requestImageFromIPCam(sms.getCode());
				Mms mms = prepareMMS(zmsg, sms);
				gsm.sendMms(mms);

			} else {
				try {
					Thread.sleep(100l);
				} catch (InterruptedException e) {
					logger.log(Level.DEBUG, e);
				}
			}
		}
	}
	//?remove
	public String addSmsService(ZMQService service) {
		String ussdCode = "camService";
		services.put(ussdCode, service);
		return ussdCode;
	}
}
