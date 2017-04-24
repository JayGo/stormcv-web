package edu.fudan.jliu.constant;

public class EffectType {
	public static final String GRAY_EFFECT = "gray";
	public static final String CANNY_EFFECT = "cannyEdge";
	public static final String COLOR_HISTOGRAM = "colorHistogram";
	
	public static final String[] EFFECT_LIST = new String[] {GRAY_EFFECT, CANNY_EFFECT, COLOR_HISTOGRAM};
	
	public static boolean isSupportedEffect(String effectType) {
		for (String type : EFFECT_LIST) {
			if (effectType.equals(type)) {
				return true;
			}
		}
		return false;
	}
}
