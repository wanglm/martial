package org.martial.math.randomforest.entities;

public class RFentry {
	private int name;
	private String value;
	private double entropy;

	@Override
	public int hashCode() {
		return name + value.hashCode() + Double.hashCode(entropy);
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEq = false;
		if (obj instanceof RFentry) {
			RFentry _obj = (RFentry) obj;
			isEq = name == _obj.getName() && value.equals(_obj.getValue())
					&& entropy == _obj.getEntropy();
		}
		return isEq;
	}

	public int getName() {
		return name;
	}

	public void setName(int name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public double getEntropy() {
		return entropy;
	}

	public void setEntropy(double entropy) {
		this.entropy = entropy;
	}

}
