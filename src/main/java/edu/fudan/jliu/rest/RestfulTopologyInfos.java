package edu.fudan.jliu.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.org.apache.bcel.internal.classfile.Code;

import edu.fudan.jliu.constant.ResultCode;
import edu.fudan.jliu.db.TopologyDao;
import edu.fudan.jliu.db.TopologyDaoImpl;
import edu.fudan.jliu.model.TopologyComponentInfo;
import edu.fudan.jliu.model.TopologyWorkerInfo;

@Path("topology")
public class RestfulTopologyInfos {
	private static final TopologyDao topologyDao = new TopologyDaoImpl();
	
	@GET
	@Path("/topoCompInfo")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String topoCompInfo(@QueryParam("topoName") String topoName) {
		JSONObject retJson = new JSONObject();
		List<TopologyComponentInfo> topoCompInfoList = topologyDao.getAllTopologyComponentInfo(topoName);
		
		if(topoCompInfoList == null || topoCompInfoList.isEmpty()) {
			retJson.put("status", ResultCode.RESULT_ERROR_EMPTY);
			return retJson.toString();
		}
		
		JSONArray retJsons = new JSONArray();
		for(TopologyComponentInfo info : topoCompInfoList) {
			JSONObject jInfo = new JSONObject();
			jInfo.put("componentId", info.getComponentId());
			jInfo.put("type", info.getType());
			jInfo.put("executorNum", info.getExecutorNum());
			jInfo.put("taskNum", info.getTaskNum());
			jInfo.put("allTimeProcess", info.getAllTimeProcessed());
			jInfo.put("allTimeFailed", info.getAllTimeFailed());
			jInfo.put("allTimeLatency", info.getAllTimeLatency());
			retJsons.put(jInfo);
		}
		return retJsons.toString();
	}
	
	
	@GET
	@Path("/topoWorkerInfo")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String topoWorkerInfo(@QueryParam("topoName") String topoName) {
		JSONObject retJson = new JSONObject();
		List<TopologyWorkerInfo> topologyWorkerInfoList = topologyDao.getAllTopologyWorkerInfo(topoName);
		
		if(topologyWorkerInfoList == null || topologyWorkerInfoList.isEmpty()) {
			retJson.put("status", ResultCode.RESULT_ERROR_EMPTY);
			return retJson.toString();
		}
		
		JSONArray retJsons = new JSONArray();
		for(TopologyWorkerInfo info : topologyWorkerInfoList) {
			JSONObject jInfo = new JSONObject();
			jInfo.put("host", info.getHost());
			jInfo.put("pid", info.getPid());
			jInfo.put("port", info.getPort());
			jInfo.put("cpuUsage", info.getCpuUsage());
			jInfo.put("memoryUsage", info.getMemoryUsage());
			System.out.println("info: "+info.toString());
			System.out.println("jInfo: "+jInfo.toString());
			retJsons.put(jInfo);
		}
		return retJsons.toString();
	}
}
