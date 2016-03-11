package org.martial.math;

import java.util.Set;
import java.util.Map.Entry;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.martial.math.entities.PointWritable;

/**欧式距离
 * @author ming
 *
 */
public class EuclideanDistanceMath implements DistanceMath {

	@Override
	public double distance(PointWritable point, PointWritable center) {
		MapWritable mw=point.getMap();
		Set<Entry<Writable, Writable>> set = center.getMap().entrySet();
		Double values = set.stream().map(e -> {
			Text key = (Text) e.getKey();
			double value = ((DoubleWritable) e.getValue()).get();
			DoubleWritable avg = (DoubleWritable) mw.get(key);
			return Double.valueOf(Math.pow(avg.get() - value, 2));
		}).reduce(0d, (a, b) -> Double.sum(a.doubleValue(), b.doubleValue()));
		return Math.sqrt(values.doubleValue());
	}

}
