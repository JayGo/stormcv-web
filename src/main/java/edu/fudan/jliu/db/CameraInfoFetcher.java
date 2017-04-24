package edu.fudan.jliu.db;

import org.opencv.highgui.VideoCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.fudan.jliu.model.CameraInfo;

public class CameraInfoFetcher implements Runnable {
	private String streamId;
	private String address;

	private static final String opencvlibName = "libopencv_java2413.so";
	private static final String defaultOpenCVLibPath = "/usr/local/opencv/share/OpenCV/java/";

	private static final Logger logger = LoggerFactory.getLogger(CameraInfoFetcher.class);

	public CameraInfoFetcher(String streamId, String address) {
		this.streamId = streamId;
		this.address = address;
	}

	private static boolean loadLibrary(String path, String libName) {
		if (!path.endsWith("/")) {
			path += "/";
		}
		try {
			System.load(path + libName);
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public void run() {
		loadLibrary(defaultOpenCVLibPath, opencvlibName);
		VideoCapture capture = new VideoCapture();
		CameraInfo info = new CameraInfo(streamId, address, true);
		boolean isOpen = false;
		try {
			isOpen = capture.open(this.address);
		} catch (Exception e) {
		}
		if (isOpen) {
			int width = (int) capture.get(3);
			int height = (int) capture.get(4);
			float frameRate = (float) capture.get(5);
			info.setWidth(width);
			info.setHeight(height);
			info.setFrameRate(frameRate);
		} else {
			info.setValid(false);
		}
		capture.release();
		updateSql(info);
	}

	private void updateSql(CameraInfo info) {
		try {
			DBManager.getInstance().geJdbcTemplate()
					.update("UPDATE " + DBManager.CAMERA_INFO_TABLE
							+ " SET valid=?, width=?, height=?, frame_rate=? WHERE stream_id=?",
							new Object[] { (info.isValid() ? 0 : 1), info.getWidth(), info.getHeight(),
									info.getFrameRate(), info.getStreamId() });
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("update camera_info {}", info.toString());
	}
}
