package ipcamservice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.zeromq.ZMQ;
import org.zeromq.*;
import org.zeromq.ZMsg;

import servicedispatcher.SmsServiceCenter;
import servicedispatcher.ZMQService;


import ipcamservice.dao.IPCamDao;

public class IPCamInternalService implements Runnable, ZMQService {
	final static Logger logger = LogManager.getLogger(SmsServiceCenter.class.getName());
	private int port = 5070;
	private IPCamDao dao = IPCamDao.getInstance();

	public static void main(String[] args) { // main for test
		IPCamInternalService iService = new IPCamInternalService();
		Thread t = new Thread(iService);
		t.start();
	}

	@Override
	public void run() {
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket imageProvider = context.socket(ZMQ.REP);
		imageProvider.bind("tcp://localhost:" + port);
		HashMap<String, IPCam> cameras = dao.getAll();

		while (Thread.currentThread().isInterrupted()) {
			// wait = block here req
			byte[] idCamera = imageProvider.recv();
			String key = idCamera.toString();
			IPCam ipCam = cameras.get(key);

			if (ipCam == null) {
				imageProvider.send("no such camera".getBytes());
				continue;
			} else {
				byte[] image = null;
				try {
					image = Files.readAllBytes(Paths.get(".", ipCam.path + ".jpg").toAbsolutePath());
					logger.log(Level.TRACE, Paths.get(".", ipCam.path + ".jpg"));
				} catch (IOException e) {
					logger.log(Level.DEBUG, e);
				}

				ZMsg msg = new ZMsg();
				msg.addFirst(Long.toString(ipCam.timestamp, 10));
				msg.addLast(image);
				msg.send(imageProvider);
				// ZFrame zf1;
				// ZFrame.recvFrame(socket, flags)

				imageProvider.close();
			}
		}
	}
}
