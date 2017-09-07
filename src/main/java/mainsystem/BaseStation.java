package mainsystem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeromq.ZMQ;


public class BaseStation implements Runnable {
	static final Logger logger = LogManager.getLogger(BaseStation.class);
	private static BaseStation instance = new BaseStation();// for simplicity

	public static BaseStation getInstance() {
		return BaseStation.instance;
	}

	private HashMap track = new HashMap<String, String>();

	public void run() {
		Thread.currentThread().setName(BaseStation.class.getSimpleName());
		logger.log(Level.TRACE, ">>>>>>>>>>>> RUNNING thread:" + Thread.currentThread().getName());
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket responder = context.socket(ZMQ.REP);
		responder.bind("tcp://*:5555");
		while (!Thread.currentThread().isInterrupted()) {
			byte[] request = responder.recv(0);
			ObjectInputStream ob = null;
			// Sms
			Message sms = null;
			logger.log(Level.TRACE, " - - - - - request len: " + request.length);

			try {
				ob = new ObjectInputStream(new ByteArrayInputStream(request));
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				sms = (Message) ob.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			responder.send("ok".getBytes());
			if (sms.direction)
				forwardRequest(request, "tcp://localhost:5556");
			else {
				forwardRequest(request, "tcp://localhost:" + sms.netAddr);
			}
		}
		responder.close();
		context.term();
	}

	private void forwardRequest(byte[] data, String addr) {
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket forwarder = context.socket(ZMQ.REQ);
		forwarder.connect(addr);
		forwarder.send(data);
		forwarder.recv(0);
		forwarder.close();
		context.term();
		logger.log(Level.TRACE, " forward to " + addr);
	}

}
