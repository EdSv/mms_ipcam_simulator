package ipcamservice;

import ipcamservice.dao.IPCamDao;
import spark.Spark;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class IPCamService {
	IPCamDao ipCamDao = IPCamDao.getInstance();

	public static void main(String[] args) {
		IPCamService rService = new IPCamService();
		
		Spark.post("/ipcams/:newCam", "application/json", (req, res) -> {
			if(req.body().length() == 0) {
				res.status(406);
				return "406";
			}
			// req.params("idNewCam");
			//Gson g = new Gson();
			IPCam ipCam = null;
			String idResource = null;
			try {
				ipCam = new Gson().fromJson(req.body(), IPCam.class);
				idResource = rService.ipCamDao.create(ipCam);

			} catch (JsonSyntaxException e) {
				res.status(400);
			}
			res.status(201);
			return idResource;
		});
		
		// remove ip camera
		Spark.delete("/ipcams/:idCam", (req, res) -> {
			String idCam = req.params("idCam");
			if (rService.ipCamDao.deleteById(idCam)) {
				res.status(200);
			} else {
				res.status(410);// gone
			}
			return "";
		});
	}
}


/*for test:
 
 {	"id":"1",
	"url":"http://96.10.1.168/jpg/image.jpg?size=3"
}

{	"id":"2",
	"url":"http://82.144.57.103/oneshotimage.jpg"
	
}
 
 * */


