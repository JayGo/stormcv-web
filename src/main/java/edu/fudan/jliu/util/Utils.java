package edu.fudan.jliu.util;

import java.util.HashMap;
import java.util.Map;

public class Utils {

	/**
	 * map to string
	 * @param keyValue
	 * @return
	 */
//	public static String mapToString(Map<String, Object> keyValue) {
//		if (keyValue.size() <= 0) return "";
//		StringBuilder builder = new StringBuilder();
//		for (String key : keyValue.keySet()) {
//			builder.append(key).append("=").append(keyValue.get(key)).append(",");
//		}
//		return builder.substring(0, builder.length() - 1);
//	}
	
	/**
	 * string to map
	 * @param mapStr
	 * @return
	 */
/*	public static Map<String, Object>  stringToMap(String mapStr) {
		Map<String, Object> ret = new HashMap<String, Object>();
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
	}*/
}
