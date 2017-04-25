package edu.fudan.jliu.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.fudan.jliu.CameraPageGenerator;
import edu.fudan.jliu.constant.EffectType;
import edu.fudan.jliu.constant.RequestCode;
import edu.fudan.jliu.constant.ResultCode;
import edu.fudan.jliu.db.CameraDao;
import edu.fudan.jliu.db.CameraDaoImpl;
import edu.fudan.jliu.model.CameraInfo;
import edu.fudan.jliu.model.EffectRtmpInfo;
import edu.fudan.jliu.model.RawRtmpInfo;
import edu.fudan.lwang.service.CaptureDispatcher;

/*
 * @author jliu
 */

@Path("camera")
public class RestfulCameraInfos {
	
	private static final Logger logger =LoggerFactory.getLogger(RestfulCameraInfos.class);
	private static final CameraDao cameraDao = new CameraDaoImpl(); 	
	private static CameraPageGenerator cpg = CameraPageGenerator.getInstance();
	private static CaptureDispatcher captureDispatcher = new CaptureDispatcher();

	public RestfulCameraInfos() {
		
	}
	
	public static void main(String[] args) {
		/*TODO*/
		RestfulCameraInfos restfulCameraInfos = new RestfulCameraInfos();
		//restfulCameraInfos.allCamerasList();
		JSONObject request; 
		JSONObject retJson;
		String retStr;
		String streamId = "jkyan-test";
		
		
		//add camera
		request = new JSONObject();
		request.put("code", RequestCode.ADD_CAMERA);
		request.put("streamId", streamId);
		request.put("address", "rtsp://10.134.142.114/bigbang1080.mkv");
		retStr = restfulCameraInfos.addCamera(request.toString());
		logger.info("add camera return: {}", retStr);
		retJson = new JSONObject(retStr);
		streamId = retJson.getString("streamId");
		
		//all camera infos
		restfulCameraInfos.allCamerasList();
		
		//start raw
		request = new JSONObject();
		request.put("code", RequestCode.START_RAW);
		request.put("streamId", streamId);
		retStr = restfulCameraInfos.startPlayRaw(request.toString());
		logger.info("start raw camera return: {}", retStr);
		
		//raw rtmp
		retStr = restfulCameraInfos.rawRtmp(streamId);
		logger.info("raw rtmp for stream {} : {}", streamId, retStr);
		
		//all raw rtmp
		retStr = restfulCameraInfos.allRawRtmp();
		logger.info("all raw rtmp info: {}", retStr);
				
		//stop Raw
		request = new JSONObject();
		request.put("code", RequestCode.END_RAW);
		request.put("streamId", streamId);
		retStr = restfulCameraInfos.stopPlayRaw(request.toString());
		logger.info("stop raw camera return: {}", retStr);
		
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
		
		//all effects for stream
		retStr = restfulCameraInfos.allEffects(streamId);
		logger.info("all effects for stream {}: {}", streamId, retStr);
		
		//stopEffect
		request = new JSONObject();
		request.put("code", RequestCode.END_EFFECT);
		request.put("id", effectId);
		retStr = restfulCameraInfos.stopEffect(request.toString());
		logger.info("stop effect {} return: {}", effectId, retStr);
		
		//deleteCamera
		request = new JSONObject();
		request.put("code", RequestCode.DELETE_CAMERA);
		request.put("streamId", streamId);
		retStr = restfulCameraInfos.deleteCamera(request.toString());
		logger.info("delete camera {} return: {}", streamId, retStr);
		
		//add camera
		streamId = "jkyan-test2";
		request = new JSONObject();
		request.put("code", RequestCode.ADD_CAMERA);
		request.put("streamId", streamId);
		request.put("address", "rtsp://10.134.142.114/bigbang1080.mkv");
		retStr = restfulCameraInfos.addCamera(request.toString());
		logger.info("add camera return: {}", retStr);
		retJson = new JSONObject(retStr);
		streamId = retJson.getString("streamId");
		//add new raw
		request = new JSONObject();
		request.put("code", RequestCode.START_RAW);
		request.put("streamId", streamId);
		retStr = restfulCameraInfos.startPlayRaw(request.toString());
		logger.info("start raw camera return: {}", retStr);
		//add new effect
		request = new JSONObject();
		request.put("code", RequestCode.START_EFFECT);
		request.put("streamId", streamId);
		request.put("effectType", "gray");
		params = new HashMap<String, String>();
		params.put("para1", "test1");
		params.put("para2", "test2");
		request.put("effectParams", new JSONObject(params));
		retStr = restfulCameraInfos.addEffect(request.toString());
		logger.info("start effect camera return: {}", retStr);
		retJson = new JSONObject(retStr);
		effectId = retJson.getInt("id");
		//and then delete camera
		request = new JSONObject();
		request.put("code", RequestCode.DELETE_CAMERA);
		request.put("streamId", streamId);
		retStr = restfulCameraInfos.deleteCamera(request.toString());
		logger.info("delete camera {} return: {}", streamId, retStr);
	}

	@GET
	@Path("/allCamerasList")
	@Produces(MediaType.APPLICATION_JSON)
	public String allCamerasList() {
		List<CameraInfo> allCameras = cameraDao.getAllCameraList();
		JSONArray array = new JSONArray(allCameras);
		logger.info("[allCamerasList]All camera infos(json): {}", array.toString());
		return array.toString();
	}
 	
	private String getStreamIdByAddr(String addr) {
		return addr.hashCode() < 0 ? ("" + addr.hashCode()).replace("-", "a") : addr.hashCode() + "";
	}

	@POST
	@Path("/add")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String addCamera(String request) {
		JSONObject jsonObject = new JSONObject(request);
		JSONObject retJson = new JSONObject();
		retJson.put("code", RequestCode.RET_ADD_CAMERA);
		if ((int)jsonObject.get("code") == RequestCode.ADD_CAMERA) {
			String streamId = jsonObject.getString("streamId");
			String address = jsonObject.getString("address");
			streamId = streamId + "_" + getStreamIdByAddr(address);
			boolean isAdded = cameraDao.addCamera(streamId, address);
			if (isAdded) {
				retJson.put("streamId", streamId);
				retJson.put("status", ResultCode.RESULT_SUCCESS);
			} else {
				retJson.put("status", ResultCode.RESULT_FAILED);
			}
		} else {
			logger.error("Illegal request {}!", request);
		}
		logger.info("return of addCamera: {}", retJson.toString());
		return retJson.toString();
	}
	
	@POST
	@Path("/startRaw")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String startPlayRaw(String request) {
		logger.info("Receive startRaw request: {}", request);
		JSONObject retJson = new JSONObject();

		JSONObject jRequest = new JSONObject(request);
		String streamId = jRequest.getString("streamId");
		if (streamId != null) {
			jRequest.put("address", cameraDao.getCameraAddress(streamId));
			String result = captureDispatcher.sendRequestToStorm(jRequest.toString());
			retJson.put("code", RequestCode.RET_START_RAW);
			RawRtmpInfo info = new RawRtmpInfo(result);
			if (info.isValid()) {
				cameraDao.addRawRtmp(info);
				retJson.put("status", ResultCode.RESULT_SUCCESS);
				retJson.put("host", info.getHost());
				retJson.put("pid", info.getPid());
				retJson.put("streamId", streamId);
				retJson.put("rtmpAddress", info.getRtmpAddress());
			} else {
				retJson.put("status", ResultCode.RESULT_FAILED);
			}
			logger.info("result of startPlayRaw: {}", retJson.toString());
		}
		return retJson.toString();
	}
	
	@POST
	@Path("/stopRaw")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String stopPlayRaw(String request) {
		logger.info("Receive stopPlayRaw request: {}", request);
		JSONObject retJson = new JSONObject();
		retJson.put("code", RequestCode.RET_END_RAW);

		JSONObject jRequest = new JSONObject(request);
		String streamId = jRequest.getString("streamId");
		if (streamId != null) {
			RawRtmpInfo rawRtmpInfo = cameraDao.getCameraRawRtmpInfo(streamId);
			if (rawRtmpInfo.isValid()) {
				jRequest.put("host", rawRtmpInfo.getHost());
				jRequest.put("pid", rawRtmpInfo.getPid());
				captureDispatcher.sendRequestToStorm(jRequest.toString());
				cameraDao.deleteRawRtmp(streamId);
				retJson.put("status", ResultCode.RESULT_SUCCESS);
			} else {
				retJson.put("status", ResultCode.RESULT_FAILED);
			}
			logger.info("result of stopPlayRaw: {}", retJson.toString());
		}
		return retJson.toString();
	}
	
	@GET
	@Path("/rawRtmp")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String rawRtmp(@QueryParam("nowRawStreamId") String nowRawStreamId) {
		RawRtmpInfo info = cameraDao.getCameraRawRtmpInfo(nowRawStreamId);
		JSONObject jsonObject = new JSONObject(info);
		return jsonObject.toString();
	}
	
	@POST
	@Path("/addEffect")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String addEffect(String request) {
		JSONObject ret = new JSONObject();
		ret.put("code", RequestCode.RET_START_EFFECT);
		
		JSONObject jRequest = new JSONObject(request);
		String streamId = jRequest.getString("streamId");
		String effectType = jRequest.getString("effectType");
		
		if (streamId != null && EffectType.isSupportedEffect(effectType)) {
			String result = captureDispatcher.sendRequestToStorm(jRequest.toString());
			
			logger.info("storm start effect result:{}", result);
			
			EffectRtmpInfo info = new EffectRtmpInfo(result);
			if (info.isValid()) {
				int id = cameraDao.addEffectRtmp(info);
				if (id > 0) {
					ret.put("status", ResultCode.RESULT_SUCCESS);
					ret.put("id", id);
					ret.put("streamId", streamId);
					ret.put("effectType", effectType);
					ret.put("rtmpAddress", info.getRtmpAddress());
					ret.put("topoId", info.getTopoId());
				} else {
					ret.put("status", ResultCode.RESULT_FAILED);
				}
			} else {
				ret.put("status", ResultCode.RESULT_FAILED);
			}
		} else {
			ret.put("status", ResultCode.RESULT_FAILED);
		}
		
		return ret.toString();
	}
	
	@GET
	@Path("/allEffects")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String allEffects(@QueryParam("nowRawStreamId") String nowRawStreamId) {
		List<EffectRtmpInfo> allEffectRtmp = cameraDao.getCameraAllEffectRtmp(nowRawStreamId);
		JSONArray ret = new JSONArray(allEffectRtmp);
		return ret.toString();
	}
	
	@GET
	@Path("/allRawRtmp")
	@Produces(MediaType.APPLICATION_JSON)
	public String allRawRtmp() {
		List<RawRtmpInfo> allRawRtmp = cameraDao.getAllRawRtmp();
		JSONArray ret = new JSONArray(allRawRtmp);
		return ret.toString();
	}
	
	@POST
	@Path("/stopEffect")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String stopEffect(String request) {
		logger.info("Receive stopEffect request: {}", request);
		JSONObject retJson = new JSONObject();
		retJson.put("code", RequestCode.RET_START_EFFECT);
		
		JSONObject jRequest = new JSONObject(request);
		int id = jRequest.getInt("id");
		if (id >= 0) {
//			RawRtmpInfo rawRtmpInfo = cameraDao.getCameraRawRtmpInfo(streamId);
			EffectRtmpInfo effectRtmpInfo = cameraDao.getCameraEffectRtmpInfo(id);
			if (effectRtmpInfo.isValid()) {
				jRequest.put("streamId", effectRtmpInfo.getStreamId());
				jRequest.put("topoId", effectRtmpInfo.getTopoId());
				captureDispatcher.sendRequestToStorm(jRequest.toString());
				cameraDao.deleteEffectRtmp(id);
				retJson.put("status", ResultCode.RESULT_SUCCESS);
			} else {
				retJson.put("status", ResultCode.RESULT_FAILED);
			}
			logger.info("result of stopEffect: {}", retJson.toString());
		}
		logger.info("[{}]result {}", "stopEffect", retJson.toString());
		return retJson.toString();
	}
	
	@POST
	@Path("/deleteCamera")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String deleteCamera(String request) {
		logger.info("Receive deleteCamera request: {}", request);
		JSONObject retJson = new JSONObject();
		retJson.put("code", RequestCode.RET_START_EFFECT);
		
		JSONObject jRequest = new JSONObject(request);
		String streamId = jRequest.getString("streamId");
		if (streamId != null) {
			String rawRtmpAddress = cameraDao.getCameraRawRtmpAddress(streamId);
			if (rawRtmpAddress != null) {
				JSONObject stopRawRequest = new JSONObject();
				stopRawRequest.put("code", RequestCode.END_RAW);
				stopRawRequest.put("streamId", streamId);
				stopPlayRaw(stopRawRequest.toString());
			}
			
			List<EffectRtmpInfo> allEffects = cameraDao.getCameraAllEffectRtmp(streamId);
			logger.info("[deleteCamera]effect rtmp info for streamid {} : {}", streamId, allEffects);
			for (EffectRtmpInfo info : allEffects) {
				JSONObject stopEffectRequest = new JSONObject();
				stopEffectRequest.put("code", RequestCode.END_EFFECT);
				stopEffectRequest.put("id", info.getId());
				stopEffect(stopEffectRequest.toString());
			}
			
			cameraDao.deleteCamera(streamId);
			retJson.put("status", ResultCode.RESULT_SUCCESS);
		} else {
			retJson.put("status", ResultCode.RESULT_FAILED);
		}
		logger.info("[{}]result: {}", "deleteCamera", retJson.toString());
		return retJson.toString();
	}
	
	@POST
	@Path("/helloWorld")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String getHello(String str) {
		logger.info("receive {}", str);
		return str;
	}
}
