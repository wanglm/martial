package org.martial.math.utils;

import java.math.BigDecimal;

public class BaseMathUtils {

	/**
	 * 概率
	 * 
	 * @param a
	 *            分子
	 * @param b
	 *            分母
	 * @return 保留4位小数
	 */
	public static double probability(long a, long b) {
		BigDecimal ab = new BigDecimal(a);
		BigDecimal bb = new BigDecimal(b);
		return ab.divide(bb, 4, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 概率
	 * 
	 * @param a
	 *            分子
	 * @param b
	 *            分母
	 * @return 保留4位小数
	 */
	public static double probability(double a, double b) {
		BigDecimal ab = new BigDecimal(a);
		BigDecimal bb = new BigDecimal(b);
		return ab.divide(bb, 4, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 概率
	 * 
	 * @param a
	 *            分子
	 * @param b
	 *            分母
	 * @return 保留4位小数
	 */
	public static double probability(int a, int b) {
		BigDecimal ab = new BigDecimal(a);
		BigDecimal bb = new BigDecimal(b);
		return ab.divide(bb, 4, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 概率
	 * 
	 * @param a
	 *            分子
	 * @param b
	 *            分母
	 * @param l
	 *            与分子相加的整数，默认为拉普拉斯平滑l=1
	 * @param k
	 *            先验条件的取值数量
	 * @return 保留4位小数
	 */
	public static double probability(long a, long b, long l, long k) {
		if (l == 0l) {
			l = 1l;
		}
		long z = a + l;
		long m = b + k * l;
		BigDecimal ab = new BigDecimal(z);
		BigDecimal bb = new BigDecimal(m);
		return ab.divide(bb, 4, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

}
