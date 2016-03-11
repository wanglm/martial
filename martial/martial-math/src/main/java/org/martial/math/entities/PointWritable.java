package org.martial.math.entities;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

/**
 * 空间向量模型，看成点 与mahout的vector类一个意思，命名区别：）
 * 
 * @author ming
 *
 */
public class PointWritable implements WritableComparable<PointWritable> {
	private Text id;// 向量id
	private MapWritable map;// 向量,key=Text,value=DoubleWritable

	public PointWritable() {
	}

	public PointWritable(Text id, MapWritable map) {
		this.id = id;
		this.map = map;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		id.write(out);
		map.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		id.readFields(in);
		map.readFields(in);
	}

	@Override
	public int compareTo(PointWritable o) {
		return id.compareTo(o.getId());
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof PointWritable) {
			PointWritable pw = (PointWritable) obj;
			isEqual = id.equals(pw.getId());
		}
		return isEqual;
	}

	public Text getId() {
		return id;
	}

	public void setId(Text id) {
		this.id = id;
	}

	public MapWritable getMap() {
		return map;
	}

	public void setMap(MapWritable map) {
		this.map = map;
	}

}
