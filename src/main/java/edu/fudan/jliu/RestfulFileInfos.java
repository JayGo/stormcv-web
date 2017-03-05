package edu.fudan.jliu;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.fudan.jliu.constant.RequestCode;
import edu.fudan.jliu.constant.ResultCode;
import edu.fudan.jliu.message.BaseMessage;
import edu.fudan.jliu.message.EffectMessage;
import edu.fudan.lwang.service.CaptureDispatcher;

@Path("file")
public class RestfulFileInfos {
	
	private static CaptureDispatcher captureDispatcher = new CaptureDispatcher();
	
	@POST
	@Path("/processPicture")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public BaseMessage addCamera(EffectMessage fmsg) {

		BaseMessage respond = new BaseMessage(ResultCode.UNKNOWN_ERROR);
		
		String srcPath = fmsg.getAddr();
		String dstPath = fmsg.getRtmpAddr();
		String streamId = fmsg.getStreamId();
		String effectType = fmsg.getEffectType();
		
		
		fmsg.setCode(RequestCode.PIC_PROCESS);

		System.out.println(fmsg);

//		List<BaseMessage> cameraList = sm.getCameraList();

//		if (containsAddr(cameraList, addr)) {
//			respond.setCode(ResultCode.CAM_ADDR_IS_EXSITED);
//			return respond;
//		}


		respond = captureDispatcher.sendStartMessageToStorm(fmsg);
		
		// For test
//		respond.setAddr(addr);
//		respond.setStreamId(streamId);
//		respond.setCode(ResultCode.RESULT_OK);
		
		if (respond.getCode() == ResultCode.RESULT_OK) {
			// DB add picture process id.
//			cpg.createPage(streamId);
//			sm.addCamera(respond);

		}

		print("pic process request respond - " + respond.toString());
		return respond;
	}
	
	private void print(String msg) {
		System.out.println(msg);
	}
}
