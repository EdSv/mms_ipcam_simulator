package ipcamservice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mainsystem.Abonent;
import mainsystem.BaseStation;
import mainsystem.GsmServer;
import servicedispatcher.SmsServiceCenter;

public class IpCameraSymulator {
	static final Logger logger = LogManager.getLogger();

	static void activateGsmServer() {
		GsmServer server = GsmServer.getInstance();
		executor.execute(server);
	}

	static void activateBaseStations(int qntBases) {
		BaseStation baseStation = BaseStation.getInstance();
		executor.execute(baseStation);
	}

	static ExecutorService executor;

	static void activateAbonents(int qntAbonents) {
		List<Abonent> abonents = new ArrayList<Abonent>();
		for (int i = 0; i < qntAbonents; i++) {
			Abonent ab = new Abonent();
			abonents.add(ab);
			executor.execute(ab);
		}
	}

	static ProcessCam listenIPcameras() {
		ProcessCam pc = new ProcessCam();
		// pc.addCamera(new IPCam("http://96.10.1.168/jpg/image.jpg?size=3"));
		// pc.addCamera(new IPCam("http://82.144.57.103/oneshotimage.jpg"));
		// pc.addCamera(new IPCam(
		// "http://94.74.71.103:8080/axis-cgi/jpg/image.cgi?camera=1&resolution=320x240&compression=25"));
		// pc.addCamera(new
		// IPCam("http://cam.unitop.ua:8161/record/current.jpg"));

		executor.execute(pc);
		return pc;
	}

	public static void main(String[] args) {
		ipcamservice.IPCamService.main(null);// start thread

		IPCamInternalService iservice = new IPCamInternalService();
		Thread ts = new Thread(iservice);
		ts.start();

		try {
			Thread.sleep(10000l);
		} catch (InterruptedException e) {
			logger.log(Level.DEBUG, e);
		}
		Thread.currentThread().setName("MAIN thread");
		int qntAbonents = 10;
		executor = Executors.newFixedThreadPool(qntAbonents + 4);
		ProcessCam pc = listenIPcameras();
		activateGsmServer();
		activateBaseStations(1);
		activateAbonents(qntAbonents);

		SmsServiceCenter sc = SmsServiceCenter.getInstance();

		Thread processSmsService = new Thread(sc);
		processSmsService.setName(SmsServiceCenter.class.getSimpleName());
		processSmsService.start();

		logger.log(Level.TRACE, "Qnt threads: " + Thread.activeCount());

		try {
			Thread.sleep(100000l);
		} catch (InterruptedException e) {
			logger.log(Level.DEBUG, e);
		}

		// pc.stopProcess();
		// System.exit(0);
	}
}
