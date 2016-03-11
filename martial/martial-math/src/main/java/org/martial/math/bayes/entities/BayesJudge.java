package org.martial.math.bayes.entities;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

public class BayesJudge implements WritableComparable<BayesJudge> {
	private Text id;
	private DoubleWritable probability;

	public BayesJudge() {
		this.id = new Text();
		this.probability = new DoubleWritable(0);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public int compareTo(BayesJudge o) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Text getId() {
		return id;
	}

	public void setId(String id) {
		this.id.set(id);
	}

	public DoubleWritable getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability.set(probability);
	}

}
