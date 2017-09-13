package ipcamservice;

public class IPCam {
	String id;
	long timestamp;
	String url;
	String path;
	String group;
	private static int count = 0;

	IPCam(String url) {
		count++;
		this.url = url;
		this.path = "photoshot" + count;
		//this.id = "id" + count;
		//this.id.intern();
	}

	public String getId() {
		return id;
	}
	

}