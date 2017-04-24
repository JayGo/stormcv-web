package edu.fudan.jliu.db;

import java.util.List;

import edu.fudan.jliu.model.CameraInfo;
import edu.fudan.jliu.model.EffectRtmpInfo;
import edu.fudan.jliu.model.RawRtmpInfo;

public interface CameraDao {
	 boolean addCamera(String streamId, String address);
	 boolean deleteCamera(String streamId);
	 List<CameraInfo> getAllCameraList();
	 CameraInfo getCameraInfo(String streamId);
	 
	 boolean addRawRtmp(RawRtmpInfo info);
	 boolean deleteRawRtmp(String streamId);
	 List<RawRtmpInfo> getAllRawRtmp();
	 
	 int addEffectRtmp(EffectRtmpInfo info);
	 boolean deleteEffectRtmp(int id);
	 List<EffectRtmpInfo> getAllEffectRtmp();
	 
	 boolean deleteAllRtmp(String streamId);
	 
	 String getCameraAddress(String streamId);
	 String getCameraRawRtmpAddress(String streamId);
	 String getCameraEffectRtmpAddress(int id);
	 List<EffectRtmpInfo> getCameraAllEffectRtmp(String streamId);
	 
	 RawRtmpInfo getCameraRawRtmpInfo(String streamId);
	 EffectRtmpInfo getCameraEffectRtmpInfo(int id);
 }