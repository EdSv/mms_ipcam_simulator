package ipcamservice;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.concurrent.Future;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

import mainsystem.Sms;
import servicedispatcher.SmsService;

public class ProcessCam implements SmsService, Runnable {
	static final Logger logger = LogManager.getLogger();
	HashMap<String, IPCam> cameras;
	final private long connectionTimeout = 10000l;
	final private long socketTimeout = 30000l;

	public ProcessCam() {
		this.cameras = new HashMap<String, IPCam>();
		Unirest.setTimeouts(connectionTimeout, socketTimeout);
	}

	synchronized public String serve(Sms sms) {
		if (sms == null)
			return null;

		IPCam ipCam = cameras.get(sms.getCode());

		if (ipCam == null)
			return null;// replace on custom Exception

		sms.setTimestamp(ipCam.timestamp);

		logger.log(Level.TRACE, "sms:" + sms);
		logger.log(Level.TRACE, " ipCam: " + ipCam);
		logger.log(Level.TRACE, " ipCam.timestamp: " + ipCam.timestamp);
		logger.log(Level.TRACE, "return PATH: " + ipCam.path);
		logger.log(Level.TRACE, "return T: " + sms.getTimestamp());

		return ipCam.path + ".jpg";// {timestamp, camera-id, image } //convert
		
	}

	synchronized public void run() {
		Thread.currentThread().setName(ProcessCam.class.getSimpleName());
		logger.log(Level.TRACE, ">>>>>>>>>>>> RUNNING thread:" +Thread.currentThread().getName());
		this.refreshPhotosFromCams();
	}

	synchronized private void refreshPhotosFromCams() {
		int qntLoop = 7;
		while (qntLoop > 0) {
			for (IPCam ipcam : cameras.values()) {
				this.getJpgFromIpCam(ipcam);
			}
			qntLoop--;

			try {
				this.wait(10000l);
			} catch (InterruptedException e) {
				logger.log(Level.DEBUG, e);
			}
		}
	}

	synchronized private void getJpgFromIpCam(final IPCam ipCam) {
		Future<HttpResponse<InputStream>> future = Unirest.get(ipCam.url).header("accept", "image/jpeg")
				.asBinaryAsync(new Callback<InputStream>() {

					public void failed(UnirestException e) {
						logger.log(Level.TRACE, "The request has failed");
					}

					public void completed(HttpResponse<InputStream> response) {
						logger.log(Level.TRACE, "Thread is: " + Thread.currentThread().getName());

						if (200 == response.getStatus()) {
							logger.log(Level.TRACE, "server ok, timestamp " + System.currentTimeMillis());
						} else {
							logger.log(Level.TRACE, "server no");
							return;
						}

						InputStream body = response.getBody();
						try {
							long t = System.currentTimeMillis();
							String p = ipCam.path + "-" + t + ".jpg";// temp
																		
							File file = new File(p);
							OutputStream fout = new BufferedOutputStream(new FileOutputStream(file));

							byte[] buffer = new byte[1024];
							int lengthRead;
							while ((lengthRead = body.read(buffer)) > 0) {
								fout.write(buffer, 0, lengthRead);
								fout.flush();
							}

							fout.close();
							file.renameTo(new File(ipCam.path + ".jpg"));
																			
						} catch (IOException e) {
							logger.log(Level.DEBUG, e);
						}
						ipCam.timestamp = System.currentTimeMillis();
						logger.log(Level.TRACE, "CHANGE timeStamp " + ipCam.timestamp);
					}

					public void cancelled() {
						logger.log(Level.TRACE, "The request has been cancelled");
					}

				});

	}

	public void addCamera(IPCam ipCam) {
		cameras.put(ipCam.id, ipCam);
	}

	public void removeCamera(IPCam ipCam) {
		cameras.remove(ipCam);
	}

	int stopProcess() {
		try {
			Thread.sleep(socketTimeout);
			Unirest.shutdown();
		} catch (IOException e) {
			logger.log(Level.DEBUG, e);
			return 1;
		} catch (InterruptedException e) {
			logger.log(Level.DEBUG, e);
			System.exit(0);
		}
		return 0;
	}

}