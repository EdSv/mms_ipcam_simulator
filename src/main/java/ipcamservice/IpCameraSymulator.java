package ipcamservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mainsystem.Abonent;
import mainsystem.Sms;
import servicedispatcher.SmsServiceCenter;

public class IpCameraSymulator {
	static final Logger logger = LogManager.getLogger();

	static void activateAbonents(int qntAbonents) {
		List<Abonent> abonents = new ArrayList<Abonent>();
		
		for (int i = 0; i < 10; i++) {
			abonents.add(new Abonent());
		}
		
		Random random = new Random();
		//each abonent will send 5 service messages 
		for (int k = 0; k < 5; k++) {
			for (int i = 0; i < qntAbonents; i++) {
				abonents.get(i).sendSms(new Sms("camService", "id" + (random.nextInt(3) % 3 + 1)));
			}
		}
	}

	public static void main(String[] args) {
		Thread.currentThread().setName("MAIN tread");
		activateAbonents(10);
		ProcessCam pc = new ProcessCam();

		pc.addCamera(new IPCam("http://96.10.1.168/jpg/image.jpg?size=3"));
		pc.addCamera(new IPCam("http://94.74.71.103:8080/axis-cgi/jpg/image.cgi?camera=1&resolution=320x240&compression=25"));
		pc.addCamera(new IPCam("http://cam.unitop.ua:8161/record/current.jpg"));
		
		Thread processIPCams = new Thread(pc);
		processIPCams.setName(ProcessCam.class.getSimpleName());
		SmsServiceCenter sc = SmsServiceCenter.getInstance();
		sc.addSmsService(pc);
		processIPCams.start();
		
		Thread processSmsService = new Thread(sc);
		processSmsService.setName(SmsServiceCenter.class.getSimpleName());
		processSmsService.start();
		
		//
		ThreadGroup gr = Thread.currentThread().getThreadGroup();
		Thread[] lt = new Thread[gr.activeCount()];
		gr.enumerate(lt);
		logger.log(Level.TRACE, "Qnt threads: " + Thread.activeCount());
		for (Thread t : lt) {
			logger.log(Level.TRACE, "THREAD: " + t.getName());
			t.interrupt();
		}

		
		try {
			Thread.sleep(100000l);
		} catch (InterruptedException e) {
			logger.log(Level.DEBUG, e);
		}
	
		// pc.stopProcess();
		// System.exit(0);
	}
}
