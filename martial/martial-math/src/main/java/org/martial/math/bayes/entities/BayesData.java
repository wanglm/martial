package org.martial.math.bayes.entities;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

public class BayesData implements WritableComparable<BayesData> {
	public static final String PRIOR_DATA = "d";
	private Text name;
	private Text value;
	private Text prior;

	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public int compareTo(BayesData o) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Text getName() {
		return name;
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public Text getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value.set(value);
	}

	public Text getPrior() {
		return prior;
	}

	public void setPrior(String prior) {
		this.prior.set(prior);
	}

}
