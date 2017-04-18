package edu.fudan.lwang.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.fudan.jliu.constant.RequestCode;
import edu.fudan.jliu.constant.ResultCode;
import edu.fudan.jliu.message.BaseMessage;
import edu.fudan.jliu.message.EffectMessage;

public class CaptureDispatcher {
	
	private final static Logger logger = Logger.getLogger("CaptureDispatcher.class");

	private final int serverMsgPort = 8998;
	private final int serverDataPort = 8999;

	private final String stormMaster = "10.134.142.141";
//	private final String stormMaster = "10.134.142.114";
	private final int stormcvCoreMsgPort = 8999;

//	private static String[] serverIps = { "10.134.142.101", "10.134.142.104", "10.134.142.106", "10.134.142.107",
//			"10.134.142.108", "10.134.142.109", "10.134.142.111", "10.134.142.115", "10.134.142.116", "10.134.142.117",
//			"10.134.142.118", "10.134.142.119", "10.134.142.120", "10.134.142.121", "10.134.142.122", "10.134.142.123",
//			"10.134.142.114" };

	// private static String[] serverIps = { "10.134.142.101", "10.134.142.104"};
	
	private static String[] serverIps = {"10.134.142.104"};
	
	private static Map<String, List<String>> serverStatusMap;

	private static String[] rtmpServers = { "rtmp://10.134.142.141:1935/live1/" };
	private static Map<String, Integer> rtmpServerMap;

	public CaptureDispatcher() {
		serverStatusMap = new HashMap<String, List<String>>();

		for (int i = 0; i < serverIps.length; i++) {
			serverStatusMap.put(serverIps[i], new ArrayList<String>());
		}

		rtmpServerMap = new HashMap<String, Integer>();
		for (int i = 0; i < rtmpServers.length; i++) {
			rtmpServerMap.put(rtmpServers[i], 0);
		}
	}
	
	public BaseMessage sendStartMessageToStorm(BaseMessage msg) {
		BaseMessage result = new BaseMessage(ResultCode.NO_SERVER_AVAILABLE);
		EffectMessage emsg = null;

		boolean isEffectMessage = msg instanceof EffectMessage;
		if (isEffectMessage) {
			emsg = (EffectMessage) msg;
		}
		
		TCPClient client2 = new TCPClient(stormMaster, stormcvCoreMsgPort);
		
		switch (msg.getCode()) {
		case RequestCode.START_STORM:
			result = client2.sendMsg(msg);
			logger.info("send startStorm: " + msg);
			break;
		
		case RequestCode.START_EFFECT_STORM:
			result = client2.sendMsg(emsg);
			logger.info("send startEffectStorm: " + emsg);
			logger.info("result of startEffectStorm: "+result);
			break;
			
		case RequestCode.PIC_PROCESS:
			result = client2.sendMsg(emsg);
			logger.info("send picture process: " + emsg);
			break;
		default:
			break;
		}
		
		if(result != null) {
			logger.info("storm core's respond: " + result);
		} else {
			logger.error("result is null!");
		}
		
		return result;
	}

	// find suitable server to decode video stream
	private int getMinVideoStreamsNum() {
		int minVideoNum = 100;
		Iterator<Map.Entry<String, List<String>>> it = serverStatusMap.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<String, List<String>> entry = it.next();
			if (minVideoNum > entry.getValue().size()) {
				minVideoNum = entry.getValue().size();
			}
		}
		return minVideoNum;
	}

	private String getSuitableRTMPServer() {
		String rtmpAddr = rtmpServers[0];
		Iterator<Map.Entry<String, Integer>> itRTMP = rtmpServerMap.entrySet().iterator();
		int minimalNum = 100;
		while (itRTMP.hasNext()) {
			Map.Entry<String, Integer> entryRTMP = itRTMP.next();
			if (minimalNum > entryRTMP.getValue()) {
				minimalNum = entryRTMP.getValue();
			}
		}

		itRTMP = rtmpServerMap.entrySet().iterator();
		while (itRTMP.hasNext()) {
			Map.Entry<String, Integer> entryRTMP = itRTMP.next();
			if (minimalNum == entryRTMP.getValue()) {
				rtmpAddr = entryRTMP.getKey();
				rtmpServerMap.put(rtmpAddr, entryRTMP.getValue() + 1);
				break;
			}
		}
		return rtmpAddr;
	}

	public BaseMessage dispatchStartMessage(BaseMessage msg) {
		BaseMessage result = new BaseMessage(ResultCode.NO_SERVER_AVAILABLE);
		String camAddr = msg.getAddr();
		String streamId = msg.getStreamId();
		EffectMessage emsg = null;

		boolean isEffectMessage = msg instanceof EffectMessage;
		if (isEffectMessage) {
			emsg = (EffectMessage) msg;
		}

		int minVideoNum = getMinVideoStreamsNum();

		Iterator<Map.Entry<String, List<String>>> it = serverStatusMap.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<String, List<String>> entry = it.next();

			if (entry.getValue().size() == minVideoNum && minVideoNum <= 1) {

				String avaliableServer = entry.getKey();

				// ====================== For CaptureService test ====================
				// Client1 for the capture process.
				TCPClient client1 = new TCPClient(avaliableServer, serverMsgPort);
				if (!isEffectMessage) {
					msg.setCode(RequestCode.START_BY_MASTER);
					result = client1.sendMsg(msg);
					logger.info("send startByMaster: " + msg);
					//System.out.println("send startByMaster: " + msg);
				} else {
					emsg.setCode(RequestCode.START_EFFECT_BY_MASTER);
					result = client1.sendMsg(emsg);
					logger.info("send startEffectByMaster: " + emsg);
//					System.out.println("send startEffectByMaster: " + emsg);
				}
				System.out.println("client1's respond: "+result);
				// =================== End of CaptureService test =====================
//				result.setCode(ResultCode.RESULT_OK);
				if (result.getCode() != ResultCode.RESULT_OK)
					continue;
				// ==================== For web-server test ====================
				// ==================== end of web-server test
				// ===================

				// ====================== For CaptureService test ====================
				// Client2 for the storm's read process.
				TCPClient client2 = new TCPClient(stormMaster, stormcvCoreMsgPort);

				String dataOutputAddr = entry.getKey() + ":" + serverDataPort;
				String stormRTMPAddr = getSuitableRTMPServer();

				if (!isEffectMessage) {
					msg.setAddr(dataOutputAddr);
					msg.setRtmpAddr(stormRTMPAddr);
					msg.setCode(RequestCode.START_STORM);
					result = client2.sendMsg(msg);
					System.out.println("send startStorm: " + msg);
				} else {
					emsg.setAddr(dataOutputAddr);
					emsg.setRtmpAddr(stormRTMPAddr);
					emsg.setCode(RequestCode.START_EFFECT_STORM);
					result = client2.sendMsg(emsg);
					System.out.println("send startEffectStorm: " + emsg);
				}
				System.out.println("client2's respond: "+result);
				// =================== End of CaptureService test =====================
				if (result.getCode() == ResultCode.RESULT_OK) {
					List<String> camLists = entry.getValue();
					camLists.add(camAddr);
					serverStatusMap.put(entry.getKey(), camLists);
					result.setAddr(camAddr);
					result.setCode(ResultCode.RESULT_OK);
					result.setRtmpAddr(stormRTMPAddr);
					result.setStreamId(streamId);
					return result;
				}
			}
		}

		return result;
	}

}
