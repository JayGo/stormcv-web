package edu.fudan.jliu.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import edu.fudan.jliu.model.CameraInfo;
import edu.fudan.jliu.model.EffectRtmpInfo;
import edu.fudan.jliu.model.RawRtmpInfo;
import edu.fudan.jliu.util.Utils;

public class CameraDaoImpl implements CameraDao {

	private static final Logger logger = LoggerFactory.getLogger(CameraDaoImpl.class);
	private DBManager dbManager = DBManager.getInstance();

	/* test case */
	public static void main(String[] args) {
		CameraDao cameraDao = new CameraDaoImpl();
		// clear all records
		DBManager.getInstance().clearAllRecords(DBManager.CAMERA_INFO_TABLE);

		// add camera test
		cameraDao.addCamera("test", "rtsp://10.134.142.114/bigbang480.mkv");
		cameraDao.addCamera("test", "rtsp://10.134.142.114/bigbang480.mkv");
		cameraDao.addCamera("test2", "rtsp://10.134.142.114/bigbang1080.mkv");

		logger.info("address of test:" + cameraDao.getCameraAddress("test"));
		logger.info("address of test3:" + cameraDao.getCameraAddress("test3"));

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// get all camera
		cameraDao.getAllCameraList();

		// delete camera
		cameraDao.deleteCamera("test2");

		// add raw rtmp
		RawRtmpInfo rawRtmpInfo = new RawRtmpInfo("test", "rtmp://10.134.142.141:1935/live/grayscale", true,
				"10.134.142.141", 7556);
		cameraDao.addRawRtmp(rawRtmpInfo);
		cameraDao.getAllRawRtmp();
		cameraDao.getCameraRawRtmpInfo("test");

		// add effect rtmp
		Map<String, String> params = new HashMap<String, String>();
		params.put("para1", "test1");
		params.put("para2", "test2");
		EffectRtmpInfo effectRtmpInfo = new EffectRtmpInfo("test", "rtmp://10.134.142.141:1935/live/grayscale", true,
				"gray", params, "topo_123456");
		int effectIndex = cameraDao.addEffectRtmp(effectRtmpInfo);
		cameraDao.getAllEffectRtmp();

		// get all effect rtmp
		logger.info("raw rtmp address of test:" + cameraDao.getCameraRawRtmpAddress("test"));
		logger.info("effect rtmp address of test:" + cameraDao.getCameraEffectRtmpAddress(effectIndex));
		List<EffectRtmpInfo> infos = cameraDao.getCameraAllEffectRtmp("test");
		for (EffectRtmpInfo info : infos) {
			System.out.println(info.toString());
		}

		// delete all rtmp for streamId
		cameraDao.deleteAllRtmp("test");
	}

	@Override
	public boolean addCamera(String streamId, String address) {
		boolean ret = true;
		try {
			dbManager.geJdbcTemplate().update("INSERT INTO " + DBManager.CAMERA_INFO_TABLE + " "
					+ DBManager.CAMERA_INFO_TABLE_SIMPLE_COLS + " VALUES(?, ?)", new Object[] { streamId, address });
		} catch (Exception e) {
			ret = false;
			logger.info("add camera {} failed due to {}", streamId, e.getMessage());
		}
		if (ret) {
			logger.info("add camera success [streamId={}, addr={}]", streamId, address);
			new Thread(new CameraInfoFetcher(streamId, address)).start();
		}
		return ret;
	}

	@Override
	public boolean deleteCamera(String streamId) {
		boolean ret = true;
		try {
			dbManager.geJdbcTemplate().update("DELETE FROM " + DBManager.CAMERA_INFO_TABLE + " WHERE stream_id = ?",
					new Object[] { streamId });
		} catch (Exception e) {
			ret = false;
			logger.info("delete camera {} failed due to {}", streamId, e.getMessage());
		}
		if (ret) {
			logger.info("delete camera success [streamId={}]", streamId);
		}
		return ret;
	}

	@Override
	public List<CameraInfo> getAllCameraList() {
		List<CameraInfo> ret = new ArrayList<>();

		List<Map<String, Object>> rows = dbManager.geJdbcTemplate()
				.queryForList("SELECT * FROM " + DBManager.CAMERA_INFO_TABLE);
		Iterator<Map<String, Object>> it = rows.iterator();
		while (it.hasNext()) {
			CameraInfo info = new CameraInfo();
			Map<String, Object> userMap = (Map<String, Object>) it.next();
			info.setStreamId((String) userMap.get("stream_id"));
			info.setAddress((String) userMap.get("address"));
			info.setValid(((int) userMap.get("valid") == 0) ? true : false);
			info.setWidth((int) userMap.get("width"));
			info.setHeight((int) userMap.get("height"));
			info.setFrameRate((float) userMap.get("frame_rate"));
			ret.add(info);
			logger.info(info.toString());
		}
		return ret;
	}

	@Override
	public boolean addRawRtmp(RawRtmpInfo info) {
		boolean ret = true;
		try {
			dbManager.geJdbcTemplate()
					.update("INSERT INTO " + DBManager.RAW_RTMP_TABLE + " " + DBManager.RAW_RTMP_TABLE_COLS
							+ " VALUES(?, ?, ?, ?, ?)",
							new Object[] { info.getStreamId(), info.getRtmpAddress(), info.getHost(), info.getPid(),
									(info.isValid() ? 0 : 1) });
		} catch (Exception e) {
			ret = false;
			logger.info("add RawRtmp {} failed due to {}", info.getStreamId(), e.getMessage());
		}
		if (ret) {
			logger.info("add RawRtmp success [streamId={}, rtmpAddr={}, host={}, pid={}]", info.getStreamId(),
					info.getRtmpAddress(), info.getHost(), info.getPid());
		}
		return ret;
	}

	@Override
	public boolean deleteRawRtmp(String streamId) {
		boolean ret = true;
		try {
			dbManager.geJdbcTemplate().update("DELETE FROM " + DBManager.RAW_RTMP_TABLE + " WHERE stream_id = ?",
					new Object[] { streamId });
		} catch (Exception e) {
			ret = false;
			logger.info("delete RawRtmp {} failed due to {}", streamId, e.getMessage());
		}
		if (ret) {
			logger.info("delete RawRtmp success [streamId={}]", streamId);
		}
		return ret;
	}

	@Override
	public List<RawRtmpInfo> getAllRawRtmp() {
		List<RawRtmpInfo> ret = new ArrayList<>();
		List<Map<String, Object>> rows = dbManager.geJdbcTemplate()
				.queryForList("SELECT * FROM " + DBManager.RAW_RTMP_TABLE);
		Iterator<Map<String, Object>> it = rows.iterator();
		while (it.hasNext()) {
			RawRtmpInfo info = new RawRtmpInfo();
			Map<String, Object> userMap = (Map<String, Object>) it.next();
			info.setStreamId((String) userMap.get("stream_id"));
			info.setRtmpAddress((String) userMap.get("rtmp_addr"));
			info.setValid(((int) userMap.get("valid") == 0) ? true : false);
			info.setHost((String) userMap.get("host"));
			info.setPid((long) userMap.get("pid"));
			ret.add(info);
			logger.info(info.toString());
		}
		return ret;
	}
	
	

	@Override
	public boolean deleteAllRtmp(String streamId) {
		dbManager.clearAllRecords(DBManager.RAW_RTMP_TABLE);
		dbManager.clearAllRecords(DBManager.EFFECT_RTMP_TABLE);
		return true;
	}

	/**
	 * @return return the auto_incremented id after insert the effect request
	 */
	@Override
	public int addEffectRtmp(final EffectRtmpInfo info) {
		int ret = -1;
		try {
			final String sql = "INSERT INTO " + DBManager.EFFECT_RTMP_TABLE + " " + DBManager.EFFECT_RTMP_TABLE_COLS
					+ " VALUES(?, ?, ?, ?, ?, ?)";

			KeyHolder keyHolder = new GeneratedKeyHolder();
			dbManager.geJdbcTemplate().update(new PreparedStatementCreator() {
				@Override
				public java.sql.PreparedStatement createPreparedStatement(java.sql.Connection connection)
						throws SQLException {
					PreparedStatement ps = (PreparedStatement) connection.prepareStatement(sql,
							Statement.RETURN_GENERATED_KEYS);
					int i = 1;
					ps.setString(i++, info.getStreamId());
					ps.setString(i++, info.getEffectType());
					ps.setString(i++, (new JSONObject(info.getEffectParams()).toString()));
					ps.setString(i++, info.getRtmpAddress());
					ps.setString(i++, info.getTopoName());
					ps.setInt(i++, (info.isValid() ? 0 : 1));
					return ps;
				}
			}, keyHolder);

			ret = keyHolder.getKey().intValue();

		} catch (Exception e) {
			logger.info("add EffectRtmp {streamId:{}, effectType:{}} failed due to {}", info.getStreamId(),
					info.getEffectType(), e.getMessage());
		}
		if (ret >= 0) {
			logger.info("add EffectRtmp [id={}] success {}", ret, info.toString());
		}
		return ret;
	}

	@Override
	public boolean deleteEffectRtmp(int id) {
		boolean ret = true;
		try {
			dbManager.geJdbcTemplate().update("DELETE FROM " + DBManager.EFFECT_RTMP_TABLE + " WHERE id = ?",
					new Object[] { id });
		} catch (Exception e) {
			ret = false;
			logger.info("delete EffectRtmp [id={}] failed due to {}", id, e.getMessage());
		}
		if (ret) {
			logger.info("delete EffectRtmp success [id={}]", id);
		}
		return ret;
	}

	@Override
	public List<EffectRtmpInfo> getAllEffectRtmp() {
		List<EffectRtmpInfo> ret = new ArrayList<>();
		List<Map<String, Object>> rows = dbManager.geJdbcTemplate()
				.queryForList("SELECT * FROM " + DBManager.EFFECT_RTMP_TABLE);
		Iterator<Map<String, Object>> it = rows.iterator();
		while (it.hasNext()) {
			EffectRtmpInfo info = new EffectRtmpInfo();
			Map<String, Object> userMap = (Map<String, Object>) it.next();
			info.setId((int) userMap.get("id"));
			info.setStreamId((String) userMap.get("stream_id"));
			info.setRtmpAddress((String) userMap.get("rtmp_addr"));
			info.setValid(((int) userMap.get("valid") == 0) ? true : false);
			info.setTopoName((String) userMap.get("topo_name"));
			info.setEffectType((String) userMap.get("effect_type"));
			String paramStr = (String) userMap.get("effect_params");
			info.setEffectParams(new JSONObject(paramStr).toMap());
			ret.add(info);
			logger.info(info.toString());
		}
		return ret;
	}

	@Override
	public String getCameraAddress(String streamId) {
		String address = null;
		try {
			address = dbManager.geJdbcTemplate().queryForObject(
					"SELECT address FROM " + DBManager.CAMERA_INFO_TABLE + " WHERE stream_id = ?",
					new Object[] { streamId }, String.class);
		} catch (Exception e) {
			logger.error("cannot query address for streamId {} due to {}", streamId, e.getMessage());
		}
		return address;
	}

	@Override
	public String getCameraRawRtmpAddress(String streamId) {
		String address = null;
		try {
			address = dbManager.geJdbcTemplate().queryForObject(
					"SELECT rtmp_addr FROM " + DBManager.RAW_RTMP_TABLE + " WHERE stream_id = ?",
					new Object[] { streamId }, String.class);
		} catch (Exception e) {
			logger.error("cannot query rtmp_addr for streamId {} due to {}", streamId, e.getMessage());
		}
		return address;
	}

	@Override
	public String getCameraEffectRtmpAddress(int id) {
		String address = null;
		try {
			address = dbManager.geJdbcTemplate().queryForObject(
					"SELECT rtmp_addr FROM " + DBManager.EFFECT_RTMP_TABLE + " WHERE id = ? ", new Object[] { id },
					String.class);
		} catch (Exception e) {
			logger.error("cannot query rtmp_addr for id {} effcode {} due to {}", id, e.getMessage());
		}
		return address;
	}

	@Override
	public List<EffectRtmpInfo> getCameraAllEffectRtmp(String streamId) {
		List<EffectRtmpInfo> ret = new ArrayList<>();
		List<Map<String, Object>> rows = dbManager.geJdbcTemplate().queryForList(
				"SELECT * FROM " + DBManager.EFFECT_RTMP_TABLE + " WHERE stream_id = ?", new Object[] { streamId });
		Iterator<Map<String, Object>> it = rows.iterator();
		while (it.hasNext()) {
			EffectRtmpInfo info = new EffectRtmpInfo();
			Map<String, Object> userMap = (Map<String, Object>) it.next();
			info.setId((int) userMap.get("id"));
			info.setStreamId((String) userMap.get("stream_id"));
			info.setRtmpAddress((String) userMap.get("rtmp_addr"));
			info.setValid(((int) userMap.get("valid") == 0) ? true : false);
			info.setTopoName((String) userMap.get("topo_name"));
			info.setEffectType((String) userMap.get("effect_type"));
			String paramStr = (String) userMap.get("effect_params");
			info.setEffectParams(new JSONObject(paramStr).toMap());
			ret.add(info);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.fudan.jliu.db.CameraDao#getCameraRawRtmpInfo(java.lang.String)
	 */
	@Override
	public RawRtmpInfo getCameraRawRtmpInfo(String streamId) {
		RawRtmpInfo ret = null;

		Map<String, Object> rows = null;
		try {
			rows = dbManager.geJdbcTemplate().queryForMap(
					"SELECT * FROM " + DBManager.RAW_RTMP_TABLE + " WHERE stream_id = ?", new Object[] { streamId });
			ret = new RawRtmpInfo();
			ret.setStreamId((String) rows.get("stream_id"));
			ret.setRtmpAddress((String) rows.get("rtmp_addr"));
			ret.setValid(((int) rows.get("valid") == 0) ? true : false);
			ret.setHost((String) rows.get("host"));
			ret.setPid((long) rows.get("pid"));
			logger.info("[{}]{}", "getCameraRawRtmpInfo", ret.toString());
		} catch (IncorrectResultSizeDataAccessException e) {
			// TODO: handle exception
			logger.info("{} not push to rtmp yet!", streamId);
		} catch (DataAccessException e) {
			logger.info("{} no permission!", streamId);
		}

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.fudan.jliu.db.CameraDao#getCameraEffectRtmpInfo(java.lang.String)
	 */
	@Override
	public EffectRtmpInfo getCameraEffectRtmpInfo(int id) {
		EffectRtmpInfo ret = new EffectRtmpInfo();
		Map<String, Object> rows = dbManager.geJdbcTemplate()
				.queryForMap("SELECT * FROM " + DBManager.EFFECT_RTMP_TABLE + " WHERE id = ?", new Object[] { id });
		ret.setId(id);
		ret.setStreamId((String) rows.get("stream_id"));
		ret.setRtmpAddress((String) rows.get("rtmp_addr"));
		ret.setValid(((int) rows.get("valid") == 0) ? true : false);
		ret.setTopoName((String) rows.get("topo_name"));
		ret.setEffectType((String) rows.get("effect_type"));
		String paramStr = (String) rows.get("effect_params");
		ret.setEffectParams(new JSONObject(paramStr).toMap());
		logger.info("[{}]{}", "getCameraRawRtmpInfo: ", ret.toString());
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.fudan.jliu.db.CameraDao#getCameraInfo(java.lang.String)
	 */
	@Override
	public CameraInfo getCameraInfo(String streamId) {
		CameraInfo ret = new CameraInfo();

		Map<String, Object> queryRet = dbManager.geJdbcTemplate().queryForMap(
				"SELECT * FROM " + DBManager.CAMERA_INFO_TABLE + " WHERE stream_id = ?", new Object[] { streamId });

		ret.setStreamId((String) queryRet.get("stream_id"));
		ret.setName(ret.getName());
		ret.setAddress((String) queryRet.get("address"));
		ret.setValid(((int) queryRet.get("valid") == 0) ? true : false);
		ret.setWidth((int) queryRet.get("width"));
		ret.setHeight((int) queryRet.get("height"));
		ret.setFrameRate((float) queryRet.get("frame_rate"));
		logger.info("{}{}","getCameraInfo: ",ret.toString());

		return ret;
	}

	@Override
	public JSONObject getCameraAndRtmpInfo(String streamId) {
		JSONObject ret = null;

		Map<String, Object> row = null;
		try {
			row = dbManager.geJdbcTemplate().queryForMap(
					"SELECT * FROM " + DBManager.CAMERA_INFO_TABLE + " LEFT JOIN  " + DBManager.RAW_RTMP_TABLE + " USING (stream_id) WHERE stream_id="+streamId);
			ret = new JSONObject();
			ret.put("name", Utils.extractNameFromStreamID((String)row.get("stream_id")));
			ret.put("streamId", (String) row.get("stream_id"));
			ret.put("address", (String) row.get("address"));
			ret.put("height", (int)row.get("height"));
			ret.put("width", (int)row.get("width"));
			ret.put("frameRate", (float)row.get("frame_rate"));
			ret.put("rtmp_addr", (String) row.get("rtmp_addr"));
			ret.put("valid", ((row.get("valid") == null)? false :((int)row.get("valid") == 0) ? true : false));
			ret.put("host", (String) row.get("host"));
			ret.put("pid", (row.get("pid") == null)? 0 :(long)row.get("pid"));
			logger.info("[{}]{}", "getCameraAndRtmpInfo", ret.toString());
		} catch (IncorrectResultSizeDataAccessException e) {
			// TODO: handle exception
			logger.info("{} not push to rtmp yet!", streamId);
		} catch (DataAccessException e) {
			logger.info("{} no permission!", streamId);
		}
		return ret;
	}

	@Override
	public JSONArray getAllCameraAndRtmpInfos() {
		// TODO Auto-generated method stub
		JSONArray rets = new JSONArray();

		List<Map<String, Object>> rows = null;
		try {
			rows =  dbManager.geJdbcTemplate().queryForList(
					"SELECT * FROM " + DBManager.CAMERA_INFO_TABLE + " LEFT JOIN  " + DBManager.RAW_RTMP_TABLE + " USING (stream_id)");
			
			Iterator<Map<String, Object>> iterator = rows.iterator();
			
			while (iterator.hasNext()) {
				JSONObject ret = new JSONObject();
				Map<String, Object> row = iterator.next();
				ret.put("name", Utils.extractNameFromStreamID((String)row.get("stream_id")));
				ret.put("streamId", (String) row.get("stream_id"));
				ret.put("address", (String) row.get("address"));
				ret.put("height", (int)row.get("height"));
				ret.put("width", (int)row.get("width"));
				ret.put("frameRate", (float)row.get("frame_rate"));
				ret.put("rtmp_addr", (String) row.get("rtmp_addr"));
				ret.put("valid", ((row.get("valid") == null)? false :((int)row.get("valid") == 0) ? true : false));
				ret.put("host", (String) row.get("host"));
				ret.put("pid", (row.get("pid") == null)? 0 :(long)row.get("pid"));
				rets.put(ret);
			}
			logger.info("[{}]{}", "getAllCameraAndRtmpInfos", rets.toString());

		} catch (IncorrectResultSizeDataAccessException e) {
			// TODO: handle exception
			logger.info("no camera yet!");
		} catch (DataAccessException e) {
			e.printStackTrace();
			logger.info("no permission access to db during {}", "getAllCameraAndRtmpInfos");
		}
		return rets;
	}
}