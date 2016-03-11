package org.martial.math.entities;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.WritableComparable;

/**k-means Object
 * 一个中心以及属于该中心的点的id集
 * @author ming
 *
 */
public class KclusterWritable implements WritableComparable<KclusterWritable> {
	private PointWritable center;
	private ArrayWritable pointIDs;

	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public int compareTo(KclusterWritable o) {
		// TODO Auto-generated method stub
		return 0;
	}

	public PointWritable getCenter() {
		return center;
	}

	public void setCenter(PointWritable center) {
		this.center = center;
	}

	public ArrayWritable getPointIDs() {
		return pointIDs;
	}

	public void setPointIDs(ArrayWritable pointIDs) {
		this.pointIDs = pointIDs;
	}

}
