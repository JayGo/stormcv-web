//package edu.fudan.jliu;
//
//import java.io.IOException;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import javax.ws.rs.Consumes;
//import javax.ws.rs.GET;
//import javax.ws.rs.POST;
//import javax.ws.rs.Path;
//import javax.ws.rs.Produces;
//import javax.ws.rs.QueryParam;
//import javax.ws.rs.core.MediaType;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import edu.fudan.jliu.constant.RequestCode;
//import edu.fudan.jliu.constant.ResultCode;
//import edu.fudan.jliu.message.BaseMessage;
//import edu.fudan.jliu.message.EffectMessage;
//import edu.fudan.lwang.service.CaptureDispatcher;
//import edu.fudan.lwang.service.TCPClient;
//
///*
// * @author jliu
// */
//
//@Path("camera")
//public class RestfulCameraInfos {
//	
////	static { 
////		PropertyConfigurator.configure("/home/jliu/Documents/log4j.prop");
////	}
//
//	private static final Logger logger =LoggerFactory.getLogger(RestfulCameraInfos.class);
//	private static SqlManager sm = SqlManager.getInstance();
//	private static CameraPageGenerator cpg = CameraPageGenerator.getInstance();
//
//	// static private String dirString =
//	// "/home/jliu/workspace/simple-service-webapp/src/main/webapp/";
//
//
//	private static CaptureDispatcher captureDispatcher = new CaptureDispatcher();
//
//	public RestfulCameraInfos() {
//	}
//
//	private void print(String msg) {
//		System.out.println(msg);
//	}
//
//	@GET
//	@Path("/allCamerasList")
//	@Produces(MediaType.APPLICATION_JSON)
//	public ArrayList<BaseMessage> allCamerasList() {
//		// print("allCameraList is invoked!");
//		return (ArrayList<BaseMessage>) sm.getCameraList();
//	}
//
//	private String getStreamIdByAddr(String addr) {
//		return addr.hashCode() < 0 ? ("" + addr.hashCode()).replace("-", "a") : addr.hashCode() + "";
//	}
//
//	private String generateStreamId(BaseMessage msg) {
//		String streamId = "";
//		String addr = msg.getAddr();
//		if (!(msg instanceof EffectMessage)) {
//			streamId = getStreamIdByAddr(addr);
//		} else {
//			EffectMessage eMsg = (EffectMessage) msg;
//			streamId = getStreamIdByAddr(addr) + "_" + eMsg.getEffectType();
//		}
//		return streamId;
//	}
//
//	@POST
//	@Path("/add")
//	@Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.APPLICATION_JSON)
//	public BaseMessage addCamera(BaseMessage cam) {
//
//		BaseMessage respond = new BaseMessage(ResultCode.UNKNOWN_ERROR);
//		
//		// System.out.println(cam);
//		
//		logger.info("receive camera: "+cam);
//
//		String addr = cam.getAddr();
//		
//		if(sm == null) {
//			logger.info("sm is null!");
//		}
//		List<BaseMessage> cameraList = sm.getCameraList();
//
//		if (containsAddr(cameraList, addr)) {
//			respond.setCode(ResultCode.CAM_ADDR_IS_EXSITED);
//			return respond;
//		}
//
//		String streamId = generateStreamId(cam);
//		cam.setStreamId(streamId);
//
//		cam.setCode(RequestCode.START_STORM);
//		respond = captureDispatcher.sendStartMessageToStorm(cam);
//		
//		// For test
////		respond.setAddr(addr);
////		respond.setStreamId(streamId);
////		respond.setCode(ResultCode.RESULT_OK);
//		
//		if (respond.getCode() == ResultCode.RESULT_OK) {
//			cpg.createPage(streamId);
//			sm.addCamera(respond);
//
//		}
//		
//		logger.info("add camera respond from storm core: "+respond);
////		print("add - " + respond.toString());
////		print("add respond - " + respond.toString());
//
//		return respond;
//	}
//
//	@GET
//	@Path("/rawRtmp")
//	@Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.APPLICATION_JSON)
//	public BaseMessage rawRtmp(@QueryParam("nowRawStreamId") String nowRawStreamId) {
//		return sm.getRawRtmp(nowRawStreamId);
//	}
//
//	private boolean containsEffect(List<EffectMessage> effectList, String effectType) {
//		for (EffectMessage e : effectList) {
//			if (e.getEffectType().equals(effectType)) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	@POST
//	@Path("/addEffect")
//	@Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.APPLICATION_JSON)
//	public BaseMessage addEffect(EffectMessage effectMessage) {
//		// print("============Add effect info:==============");
//		BaseMessage respond = new BaseMessage();
//		respond.setCode(ResultCode.UNKNOWN_ERROR);
//		String rawStreamId = effectMessage.getStreamId();
//
//		// check if effect has existed.
//		List<EffectMessage> effectList = sm.getEffectVideoList(rawStreamId);
//		if (containsEffect(effectList, effectMessage.getEffectType())) {
//			respond.setCode(ResultCode.CAM_ADDR_IS_EXSITED);
//			return respond;
//		}
//
//		String rawAddr = sm.getRawAddr(rawStreamId);
//		effectMessage.setAddr(rawAddr);
//		effectMessage.setCode(RequestCode.START_EFFECT);
//		String effectStreamId = generateStreamId(effectMessage);
//		effectMessage.setStreamId(effectStreamId);
//
//		// respond = captureDispatcher.dispatchStartMessage(effectMessage);
//		effectMessage.setCode(RequestCode.START_EFFECT_STORM);
//		respond = captureDispatcher.sendStartMessageToStorm(effectMessage);
//
//		if (respond.getCode() == ResultCode.RESULT_OK) {
//			effectMessage.setRtmpAddr(respond.getRtmpAddr());
//			sm.addEffect(effectMessage);
//			print("addEffect - " + effectMessage);
//			respond = effectMessage;
//			respond.setCode(ResultCode.RESULT_OK);
//		}
//
//		print("addEffect respond - " + respond.toString());
//		return respond;
//	}
//
//	private boolean containsAddr(List<BaseMessage> cameraList, String addr) {
//		int length = cameraList.size();
//		for (int i = 0; i < length; i++) {
//			if (cameraList.get(i).getAddr().equals(addr))
//				return true;
//		}
//		return false;
//	}
//
//	@GET
//	@Path("/allEffects")
//	@Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.APPLICATION_JSON)
//	public ArrayList<EffectMessage> allEffects(@QueryParam("nowRawStreamId") String nowRawStreamId) {
//		List<EffectMessage> res = sm.getEffectVideoList(nowRawStreamId);
//		for (EffectMessage e : res) {
//			System.out.println(e.getStreamId() + " : " + e.getEffectType());
//		}
//
//		return (ArrayList<EffectMessage>) sm.getEffectVideoList(nowRawStreamId);
//	}
//	
//	@GET
//	@Path("/helloWorld")
//	@Produces(MediaType.APPLICATION_XML)
//	public String getHello() {
//		return "Hello world!";
//	}
//}
