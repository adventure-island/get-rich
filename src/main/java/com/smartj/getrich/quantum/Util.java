package com.smartj.getrich.quantum;

public class Util {
	public static int scale(final double valueIn, final double baseMin,
			final double baseMax, final double limitMin,
			final double limitMax) {
		return (int) (((limitMax - limitMin) * (valueIn - baseMin)
				/ (baseMax - baseMin)) + limitMin);
	}
}
