package org.martial.math.entities;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;

/**
 * 判定结果
 * 
 * @author ming
 *
 */
public class ResultWritable implements Writable {
	private IntWritable result;
	private DoubleWritable probability;
	private IntWritable num;

	public ResultWritable() {
		this.result = new IntWritable();
		this.probability = new DoubleWritable();
		this.num = new IntWritable(1);
	}

	public ResultWritable(int result, double probability, int num) {
		this.result = new IntWritable(result);
		this.probability = new DoubleWritable(probability);
		this.num = new IntWritable(num);
	}

	@Override
	public int hashCode() {
		return result.get() + num.get() + probability.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEq = false;
		if (obj instanceof ResultWritable) {
			ResultWritable _obj = (ResultWritable) obj;
			isEq = result.get() == _obj.getResult().get()
					&& num.get() == _obj.getNum().get()
					&& probability.get() == _obj.getProbability().get();
		}
		return isEq;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("result:");
		sb.append(result.toString());
		sb.append(",probability:");
		sb.append(probability.toString());
		sb.append(",num:");
		sb.append(num.toString());
		return sb.toString();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		this.result.write(out);
		this.probability.write(out);
		this.num.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.result.readFields(in);
		this.probability.readFields(in);
		this.num.readFields(in);
	}

	public IntWritable getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result.set(result);
	}

	public DoubleWritable getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability.set(probability);
	}

	public void addProbability(double p) {
		double d = probability.get();
		d += p;
		probability.set(d);
	}

	public IntWritable getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num.set(num);
	}

	public void addNum(int no) {
		int n = num.get();
		n += no;
		num.set(n);
	}

}
