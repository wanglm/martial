package org.martial.math.randomforest.entities;

/**
 * 原数据
 * 
 * @author ming
 *
 */
public class RFData {
	private long id;
	private int name;
	private String value;
	private int result;

	@Override
	public int hashCode() {
		return Long.hashCode(id) + name + value.hashCode() + result;
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEq = false;
		if (obj instanceof RFData) {
			RFData _obj = (RFData) obj;
			isEq = id == _obj.getId() && name == _obj.getName()
					&& result == _obj.getResult()
					&& value.equals(_obj.getValue());
		}
		return isEq;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id);
		sb.append(",");
		sb.append(name);
		sb.append(",");
		sb.append(value);
		sb.append(",");
		sb.append(result);
		return sb.toString();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

}
