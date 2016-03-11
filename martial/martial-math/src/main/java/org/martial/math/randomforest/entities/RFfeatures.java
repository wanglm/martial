package org.martial.math.randomforest.entities;

public class RFfeatures {
	private int name;
	private int result;
	private String value;

	public int getName() {
		return name;
	}

	public void setName(int name) {
		this.name = name;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		return name + result + value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof RFfeatures) {
			RFfeatures f = (RFfeatures) obj;
			isEqual = name == f.getName() && result == f.getResult()
					&& value.equals(f.getValue());
		}
		return isEqual;
	}

}
