/**
 * 
 */
package edu.fudan.jliu.rest;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.fudan.jliu.constant.RequestCode;

/** 
* @author jkyan 
* @version  
* @description  
*/

public class CameraRestTest {
	private static final Logger logger = LoggerFactory.getLogger(CameraRestTest.class);
	
	public static void main(String[] args) {
		RestfulCameraInfos restfulCameraInfos = new RestfulCameraInfos();
		
		//add camera
		JSONObject request; 
		JSONObject retJson;
		String retStr;
		String streamId = "jkyan-test";
		
		//add camera
		request = new JSONObject();
		request.put("code", RequestCode.ADD_CAMERA);
		request.put("streamId", streamId);
		request.put("address", "rtsp://10.134.142.141:8554/bigbang480.mkv");
		retStr = restfulCameraInfos.addRaw(request.toString());
		logger.info("add camera return: {}", retStr);
		retJson = new JSONObject(retStr);
		streamId = retJson.getString("streamId");
		
		//sleep to wait cameraInfo capture
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//all camera infos
		restfulCameraInfos.allCamerasList();
		
		//start raw
		request = new JSONObject();
		request.put("code", RequestCode.START_RAW);
		request.put("streamId", streamId);
		retStr = restfulCameraInfos.startPlayRaw(request.toString());
		logger.info("start raw camera return: {}", retStr);
		
		//sleep to wait cameraInfo capture
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// stop Raw
		request = new JSONObject();
		request.put("code", RequestCode.END_RAW);
		request.put("streamId", streamId);
		retStr = restfulCameraInfos.stopPlayRaw(request.toString());
		logger.info("stop raw camera return: {}", retStr);
		
		//sleep 3s
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//add effect
		request = new JSONObject();
		request.put("code", RequestCode.START_EFFECT);
		request.put("streamId", streamId);
		request.put("effectType", "gray");
		Map<String, String> params = new HashMap<String, String>();
		params.put("para1", "test1");
		params.put("para2", "test2");
		request.put("effectParams", new JSONObject(params));
		retStr = restfulCameraInfos.addEffect(request.toString());
		logger.info("start effect camera return: {}", retStr);
		retJson = new JSONObject(retStr);
		int effectId = retJson.getInt("id");
		
		//sleep 6s
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//stopEffect
		request = new JSONObject();
		request.put("code", RequestCode.END_EFFECT);
		request.put("id", effectId);
		retStr = restfulCameraInfos.deleteEffect(request.toString());
		logger.info("stop effect {} return: {}", effectId, retStr);
	}
}
