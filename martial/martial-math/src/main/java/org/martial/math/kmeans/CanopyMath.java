package org.martial.math.kmeans;

import java.util.List;
import java.util.Set;

import org.martial.math.DistanceMath;
import org.martial.math.entities.Distances;
import org.martial.math.entities.PointWritable;



/**canopy自动聚类计算簇个数
 * @author ming
 *
 */
public class CanopyMath {
	
	/**获取canopy算法
	 * @param distance 属于canopy的距离
	 * @param distanceMath 距离算法
	 * @param canopys  canopy中心
	 * @param point  需要计算距离的canopy
	 * @return 距离内的返回所属的canopy,都不在距离内，返回自身作为canopy
	 */
	public static PointWritable canopy(double distance,DistanceMath distanceMath,Set<PointWritable> canopys,PointWritable point){
		Distances result=canopys.stream().map(e->{
			return new Distances(e,distanceMath.distance(e, point));
		}).min((a,b)->Double.compare(a.getDistance(),b.getDistance())).get();
		return result.getDistance()<=distance?result.getPoint():point;
	}
	
	public static PointWritable avgPoint(List<PointWritable> list){
		return null;
	}

}
