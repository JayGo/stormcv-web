package edu.fudan.jliu.util;

import java.util.HashMap;
import java.util.Map;

public class Utils {

	/**
	 * map to string
	 * @param keyValue
	 * @return
	 */
	public static String mapToString(Map<String, String> keyValue) {
		if (keyValue.size() <= 0) return "";
		StringBuilder builder = new StringBuilder();
		for (String key : keyValue.keySet()) {
			builder.append(key).append("=").append(keyValue.get(key)).append(",");
		}
		return builder.substring(0, builder.length() - 1);
	}
	
	/**
	 * string to map
	 * @param mapStr
	 * @return
	 */
	public static Map<String, String>  stringToMap(String mapStr) {
		Map<String, String> ret = new HashMap<String, String>();
		if (mapStr.length() > 0) {
			String[] keyValueList = mapStr.split(",");
			for (String keyValue : keyValueList) {
				String[] splits = keyValue.split("=");
				if (splits.length == 2) {
					ret.put(splits[0], splits[1]);
				}
			}
		}
		return ret;
	}
}
