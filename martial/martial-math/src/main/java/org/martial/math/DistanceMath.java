package org.martial.math;

import org.martial.math.entities.PointWritable;



/**距离计算的接口
 * @author ming
 *
 */
public interface DistanceMath {
	/**计算距离
	 * @param point
	 * @param center 坐标计算以这个点为准
	 * @return
	 */
	public double distance(PointWritable point,PointWritable center);

}
