package org.martial.math.bayes.entities;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

public class BayesEntity implements WritableComparable<BayesEntity> {
	public static final String NAME = "r";
	public static final String PRIOR = "p";
	public static final String DATA = "d";
	private Text name;
	private Text prior;// 先验条件
	private Text value;
	private IntWritable num;
	private DoubleWritable probability;// 概率

	public BayesEntity() {
		this.name = new Text();
		this.prior = new Text();
		this.value = new Text();
		this.num = new IntWritable(0);
		this.probability = new DoubleWritable(0d);
	}

	public static BayesEntity getBase() {
		BayesEntity be = new BayesEntity();
		be.setName(NAME);
		be.setPrior(PRIOR);
		be.setValue("1");
		return be;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		this.name.write(out);
		this.prior.write(out);
		this.value.write(out);
		this.num.write(out);
		this.probability.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.name.readFields(in);
		this.prior.readFields(in);
		this.value.readFields(in);
		this.num.readFields(in);
		this.probability.readFields(in);
	}

	@Override
	public int compareTo(BayesEntity o) {
		int n = name.compareTo(o.getName());
		if (n == 0) {
			n = prior.compareTo(o.getPrior());
		}
		if (n == 0) {
			n = value.compareTo(o.getValue());
		}
		if (n == 0) {
			n = num.compareTo(o.getNum());
		}
		if (n == 0) {
			n = probability.compareTo(o.getProbability());
		}
		return n;
	}

	@Override
	public int hashCode() {
		return name.hashCode() + prior.hashCode() + value.hashCode()
				+ num.get() + probability.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof BayesEntity) {
			BayesEntity _obj = (BayesEntity) obj;
			isEqual = name.toString().equals(_obj.getName().toString())
					&& prior.toString().equals(_obj.getPrior().toString())
					&& value.toString().equals(_obj.getValue().toString())
					&& num.get() == _obj.getNum().get()
					&& probability.get() == _obj.getProbability().get();
		}
		return isEqual;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name.toString());
		sb.append(",");
		sb.append(prior.toString());
		sb.append(",");
		sb.append(value.toString());
		sb.append(",");
		sb.append(num.toString());
		sb.append(",");
		sb.append(probability.toString());
		return sb.toString();
	}

	public Text getName() {
		return name;
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public Text getPrior() {
		return prior;
	}

	public void setPrior(String prior) {
		this.prior.set(prior);
	}

	public Text getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value.set(value);
	}

	public IntWritable getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num.set(num);
	}

	public DoubleWritable getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability.set(probability);
	}

}
