package edu.fudan.jliu;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.fudan.jliu.constant.EffectType;
import edu.fudan.jliu.constant.ResultCode;
import edu.fudan.jliu.message.BaseMessage;
import edu.fudan.jliu.message.EffectMessage;

public class SqlManager {

	private static SqlManager instance = null;

	private final String CAMERA_LIST_TABLE = "CAMERA_LIST";
	private final String CAMERA_LIST_COLS = "(STREAM_ID,RTMP_ADDR,ADDR)";
	private final String GRAY_EFFECT_LIST_TABLE = "GRAY_EFFECT_LIST";
	private final String GRAY_EFFECT_LIST_COLS = "(STREAM_ID,RTMP_ADDR,R_WEIGHT,G_WEIGHT,B_WEIGHT)";
	private final String CANNY_EDGE_EFFECT_LIST_TABLE = "CANNY_EDGE_EFFECT_LIST";
	private final String CANNY_EFFECT_LIST_COLS = "(STREAM_ID,RTMP_ADDR,LOW_TH,HIGH_TH)";
	private final String COLOR_HISTOGRAM_EFFECT_LIST_TABLE = "COLOR_HISTOGRAM_EFFECT_LIST";
	private final String COLOR_HISTOGRAM_EFFECT_LIST_COLS = "(STREAM_ID,RTMP_ADDR,R_WEIGHT,G_WEIGHT,B_WEIGHT)";
	private final String[] EFFECT_TABLE_SET = { GRAY_EFFECT_LIST_TABLE, CANNY_EDGE_EFFECT_LIST_TABLE, COLOR_HISTOGRAM_EFFECT_LIST_TABLE };

	private Connection connection = null;

	private SqlManager() {

		String dbPath = FileUtil.getProjectPath() + "/db/";
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath + "CameraAddr.db");
			connection.setAutoCommit(false);
		} catch (Exception e) {
			System.out.println("Class not found!");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Opened database successfully");
	}

	public static synchronized SqlManager getInstance() {
		if (instance == null) {
			instance = new SqlManager();
		}
		return instance;
	}

	// Get the left bar's camera list.
	public synchronized List<BaseMessage> getCameraList() {
		Statement stmt = null;
		ResultSet rs = null;
		List<BaseMessage> result = new ArrayList<BaseMessage>();

		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT * FROM " + CAMERA_LIST_TABLE + ";");

			while (rs.next()) {
				String streamId = rs.getString("STREAM_ID");
				// System.out.println("getCameraList - streamId: "+streamId);
				String rtmpAddr = rs.getString("RTMP_ADDR");
				// System.out.println("getCameraList - rtmpADdr: "+rtmpAddr);
				String addr = rs.getString("ADDR");
				result.add(new BaseMessage(streamId, rtmpAddr, addr));
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public synchronized BaseMessage getRawRtmp(String streamId) {
		BaseMessage res = new BaseMessage(ResultCode.UNKNOWN_ERROR);
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT * FROM " + CAMERA_LIST_TABLE + " WHERE STREAM_ID='"+streamId+"';");

			while (rs.next()) {
				String rtmpAddr = rs.getString("RTMP_ADDR");
				res.setRtmpAddr(rtmpAddr);
				res.setStreamId(streamId);
				res.setCode(ResultCode.RESULT_OK);
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	// Get all the effect videos in the mainbody's video box.
	public synchronized List<EffectMessage> getEffectVideoList(String streamId) {
		List<EffectMessage> result = new ArrayList<>();
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.createStatement();

			for (String table : EFFECT_TABLE_SET) {
				rs = stmt.executeQuery("SELECT * FROM " + table + " WHERE STREAM_ID LIKE '" + streamId + "%';");
				while (rs.next()) {
					String effectStreamId = rs.getString("STREAM_ID");
					String rtmpAddr = rs.getString("RTMP_ADDR");
					HashMap<String, Double> parameters = new HashMap<>();
					String effectType = "";
					switch (table) {
					case GRAY_EFFECT_LIST_TABLE: {
						parameters.put("r", rs.getDouble("R_WEIGHT"));
						parameters.put("g", rs.getDouble("G_WEIGHT"));
						parameters.put("b", rs.getDouble("B_WEIGHT"));
						effectType = EffectType.GRAY_EFFECT;
						break;
					}
					case CANNY_EDGE_EFFECT_LIST_TABLE: {
						parameters.put("l_th", rs.getDouble("LOW_TH"));
						parameters.put("h_th", rs.getDouble("HIGH_TH"));
						effectType = EffectType.CANNY_EFFECT;
						break;
					}
					case COLOR_HISTOGRAM_EFFECT_LIST_TABLE: {
						parameters.put("r", rs.getDouble("R_WEIGHT"));
						parameters.put("g", rs.getDouble("G_WEIGHT"));
						parameters.put("b", rs.getDouble("B_WEIGHT"));
						effectType = EffectType.COLOR_HISTOGRAM;
						break;
					}
					default:
						break;
					}

					result.add(new EffectMessage(ResultCode.RESULT_OK, null, rtmpAddr, effectStreamId, effectType, parameters));
				}
				rs.close();
				stmt.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public synchronized String getRawAddr(String rawStreamId) {
		String addr = "";
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT * FROM " + CAMERA_LIST_TABLE + " WHERE STREAM_ID='"+rawStreamId+"';");

			while (rs.next()) {
				addr = rs.getString("ADDR");
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return addr;
	}
	
	// Add effect
	public synchronized boolean addEffect(EffectMessage effectMessage) {
		Statement stmt = null;
		String table = "";
		String cols = "";
		String streamId = effectMessage.getStreamId();
		String rtmpAddr = effectMessage.getRtmpAddr();
		String effectType = effectMessage.getEffectType();
		HashMap<String, Double> parameters = effectMessage.getParameters();
		
		String paraStr = "'" + streamId + "', '" + rtmpAddr + "',";

		switch (effectType) {
		case EffectType.GRAY_EFFECT: {
			table = GRAY_EFFECT_LIST_TABLE;
			cols = GRAY_EFFECT_LIST_COLS;
			paraStr += parameters.get("r") + ", " + parameters.get("g") + ", " + parameters.get("b");
			break;
		}
		case EffectType.CANNY_EFFECT: {
			table = CANNY_EDGE_EFFECT_LIST_TABLE;
			cols = CANNY_EFFECT_LIST_COLS;
			paraStr += parameters.get("l_th") + ", " + parameters.get("h_th");
			break;
		}
		case EffectType.COLOR_HISTOGRAM: {
			table = COLOR_HISTOGRAM_EFFECT_LIST_TABLE;
			cols = COLOR_HISTOGRAM_EFFECT_LIST_COLS;
			paraStr += parameters.get("r") + ", " + parameters.get("g") + ", " + parameters.get("b");
			break;
		}
		
		default:
			break;
		}

		String sql = "INSERT INTO " + table + " " + cols + " " + "VALUES (" + paraStr + ");";

		try {
			stmt = connection.createStatement();
			stmt.executeUpdate(sql);
			connection.commit();
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	// Add source camera(only rtsp)
	public synchronized boolean addCamera(BaseMessage cam) {
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			String sql = "INSERT INTO " + CAMERA_LIST_TABLE + " " + CAMERA_LIST_COLS + " " + "VALUES ('" + cam.getStreamId() + "', '"
					+ cam.getRtmpAddr() + "', '"+cam.getAddr()+"');";
			// System.out.println("insert sql is:" + sql);
			stmt.executeUpdate(sql);
			System.out.println("Add camera to sql: "+cam);
			connection.commit();
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	public synchronized boolean clearAll() {
		clearCameraList();
		clearEffectList();
		return true;
	}

	// Delete all items in EFFECT_TABLE_SET
	public boolean clearEffectList() {
		String sql = "";
		try {
			Statement stmt = connection.createStatement();
			for (String t : EFFECT_TABLE_SET) {
				sql = "DELETE FROM " + t + ";";
				stmt.executeUpdate(sql);
				connection.commit();
				// System.out.println(t + " has been cleared!");
			}
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Database has been cleared up");
		return true;
	}


	// Delete all items in CAMERA_LIST_TABLE
	public boolean clearCameraList() {
		String sql = "DELETE FROM " + CAMERA_LIST_TABLE + ";";
		Statement stmt = null;

		try {
			stmt = connection.createStatement();
			stmt.executeUpdate(sql);
			connection.commit();
			stmt.close();
			// System.out.println(CAMERA_LIST_TABLE + " has been cleared!");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	
	public static void main(String args[]) {
		SqlManager sm = SqlManager.getInstance();
		sm.clearAll();
//		EffectMessage effectMessage = new EffectMessage();
//		effectMessage.setStreamId("1743341377");
//		effectMessage.setRtmpAddr("rtmp://10.31.23.41");
//		effectMessage.setEffectType("gray");
//		
//		HashMap<String, Double> parameters = new HashMap<>();
//		parameters.put("r", 0.52);
//		parameters.put("g", 0.33);
//		parameters.put("b", 0.15);
//		
//		effectMessage.setParameters(parameters);
//		sm.addEffect(effectMessage);
	}
	
//	public static void main(String args[]) {
//		SqlManager sm = SqlManager.getInstance();
//		sm.clearAll();
//		sm.addCamera(0, "rtsp://0");
//		sm.recordRawRtmp(0, 0, "rtmp://0");
//		sm.addCamera(1, "rtsp://1");
//		sm.recordRawRtmp(1, 0, "rtmp://1");
//
//		HashMap<String, Double> map0 = new HashMap<>();
//		map0.put("r", 0.33);
//		map0.put("g", 0.34);
//		map0.put("b", 0.33);
//
//		HashMap<String, Double> map1 = new HashMap<>();
//		map1.put("l_th", 0.33);
//		map1.put("h_th", 0.66);
//
//		sm.addEffect(0, 1, "rtmp://0-1", "gray", map0);
//		sm.addEffect(0, 2, "rtmp://0-2", "canny_edge", map1);
//
//		sm.addEffect(1, 1, "rtmp://1-1", "gray", map0);
//		sm.addEffect(1, 2, "rtmp://1-2", "canny_edge", map1);
//
//		System.out.println("All cameras:");
//		List<CameraInfo> cameras = sm.getCameraList();
//		for (CameraInfo c : cameras) {
//			System.out.println(c.getCameraId() + " : " + c.getCameraAddr());
//		}
//
//		System.out.println("Effects list on camera 1:");
//		List<EffectInfo> effects1 = sm.getEffectVideoList(1);
//		for (EffectInfo e : effects1) {
//			System.out.println(
//					e.getCamId() + " : " + e.getPlayerId() + " : " + e.getRtmpUrl() + " : " + e.getEffectType());
//		}
//		sm.clearAll();
		// sm.addCamera(4, "rtmp://255.255.253.1");
//	}

}