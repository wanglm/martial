package org.martial.math.kmeans;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.martial.math.DistanceMath;
import org.martial.math.entities.Distances;
import org.martial.math.entities.OptionEntry;
import org.martial.math.entities.PointWritable;

/**
 * k-means聚类算法
 * 
 * @author ming
 *
 */
public class KmeansMath {

	/**
	 * 建立k个中心点 k根据提前给的中心点数量决定
	 * 
	 * @return
	 */
	public static List<PointWritable> createCenters() {
		List<PointWritable> list = new LinkedList<PointWritable>();
		return list;
	}

	/**
	 * 移动中心点
	 * 
	 * @param list
	 *            向量集合，每个List<ClusterOptions>是每个向量的"坐标值"的集合
	 * @param center
	 *            中心点
	 * @return
	 */
	public static Distances moveCenter(DistanceMath distanceMath,List<List<OptionEntry>> list,
			PointWritable center) {
		int size = list.size();
		BigDecimal sizes = new BigDecimal(size);
		// 相同“坐标”的值的和
		Map<String, Double> map = new HashMap<String, Double>();
		for (List<OptionEntry> cOptions:list) {
			for (OptionEntry co: cOptions) {
				if (map.containsKey(co.getName())) {
					Double val = map.get(co.getName());
					map.replace(co.getName(),
							Double.valueOf(val.doubleValue() + co.getValue()));
				} else {
					map.put(co.getName(), Double.valueOf(co.getValue()));
				}
			}
		}
		MapWritable mw = new MapWritable();
		map.entrySet().forEach(e -> {
			BigDecimal pointsVal = new BigDecimal(e.getValue());
			// 坐标的平均值，即新的中心点的坐标
				BigDecimal avgVal = pointsVal.divide(sizes,
						BigDecimal.ROUND_HALF_UP);
				mw.put(new Text(e.getKey()),
						new DoubleWritable(avgVal.doubleValue()));
			});
		PointWritable newCenter = new PointWritable(center.getId(), mw);
		return new Distances(newCenter, distanceMath.distance(newCenter, center));
	}


}
