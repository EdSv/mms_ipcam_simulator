package ipcamservice.dao;

import java.util.HashMap;

import ipcamservice.IPCam;

public class IPCamDao {
	HashMap<String, IPCam> cameras = new HashMap<String, IPCam>();;

	private IPCamDao() {

	}

	static private IPCamDao instance = new IPCamDao();

	static public IPCamDao getInstance() {
		return instance;
	}

	public String create(IPCam ipCam) {
		cameras.put(ipCam.getId(), ipCam);
		System.out.println("Created: " + cameras.size());
		return ipCam.getId();
	}

	public boolean deleteById(String id) {
		return cameras.remove(id) == null ? false : true;
	}

	public HashMap<String, IPCam> getAll() {
		return this.cameras;
	}

	public IPCam getById(String id) {
		return this.cameras.get(id);
	}

}
