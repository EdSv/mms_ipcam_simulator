package mainsystem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeromq.ZMQ;

import ipcamservice.ProcessCam;
import servicedispatcher.SmsServiceCenter;
import servicedispatcher.SmsServiceCenter.SmsServiceCenterHolder;

public class GsmServer implements Runnable {
	static final Logger logger = LogManager.getLogger(GsmServer.class);
	Queue<Message> messageQueue;

	public static GsmServer getInstance() {
		return GsmServerHolder.instanceHolder;
	}

	private GsmServer() {
		this.messageQueue = new ConcurrentLinkedQueue<Message>();
	}

	public static class GsmServerHolder {// thread safe
		public static GsmServer instanceHolder = new GsmServer();
	}

	public void sendMms(Mms mms) {
		messageQueue.add(mms);
		Message msg = messageQueue.poll();
		String addr = "tcp://localhost:5555"; 
		byte[] data = serialize(msg);
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket sender = context.socket(ZMQ.REQ);
		sender.connect(addr);
		sender.send(data);
	}

	public void run() {
		Thread.currentThread().setName(GsmServer.class.getSimpleName());
		logger.log(Level.TRACE, ">>>>>>>>>>>> RUNNING thread:" + Thread.currentThread().getName());

		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket responder = context.socket(ZMQ.REP);
		responder.bind("tcp://localhost:5556");

		while (!Thread.currentThread().isInterrupted()) {
			byte[] request = responder.recv(0);
			// System.out.println("Received " + new String(request));

			String reply = "ok";
			responder.send(reply.getBytes(), 0);
			// responder.recv(0);

			Sms sms = (Sms) deserialize(request);

			SmsServiceCenter.getInstance().putSmsToQueue(sms);

			while (messageQueue.size() > 0) {
				Message msg = messageQueue.poll();
				String addr = "tcp://localhost:5555"; 
														
				byte[] data = serialize(msg);
				ZMQ.Socket sender = context.socket(ZMQ.REQ);
				sender.connect(addr);
				sender.send(data);
			}
		}
		responder.close();
		context.term();
	}

	private byte[] serialize(Message msg) {
		ByteArrayOutputStream bMsg = new ByteArrayOutputStream();
		ObjectOutputStream ob;

		try {
			ob = new ObjectOutputStream(bMsg);
			ob.writeObject(msg);
			ob.close();

		} catch (IOException e) {
			logger.log(Level.DEBUG, e);
		}
		return bMsg.toByteArray();

	}

	private Object deserialize(byte[] data) {
		ObjectInputStream ob = null;
		Sms sms = null;
		try {
			ob = new ObjectInputStream(new ByteArrayInputStream(data));
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			sms = (Sms) ob.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sms;
	}

}
