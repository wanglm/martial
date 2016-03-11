package org.martial.math.entities;

/**
 * 普通单值对象 name value result entropy no
 * 
 * @author ming
 *
 */
public class OptionEntry {
	private String name;
	private double value;
	private String result;
	private double entropy;// 熵
	private int no;// 数量

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public double getEntropy() {
		return entropy;
	}

	public void setEntropy(double entropy) {
		this.entropy = entropy;
	}

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

}
