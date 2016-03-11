package org.martial.math.randomforest.entities;

public class TrainNode {
	public static final int LEAF = 1;
	private int id = 0;
	private int pId = 0;// 上一层的父id
	private int name;
	private String value;// 值
	private int isLeaf = 0;// 默认不为叶子节点
	private int result = TargetIDs.NULL;// 最可能的结果
	private double probability = 0d;// 概率

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ID:");
		sb.append(id);
		sb.append(",PID:");
		sb.append(pId);
		sb.append(",特征名：");
		sb.append(name);
		sb.append(",特征值：");
		sb.append(value);
		sb.append(",叶子：");
		sb.append(isLeaf);
		sb.append(",判定：");
		sb.append(result);
		sb.append(",概率：");
		sb.append(probability);
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return id + pId + name + result + value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof TrainNode) {
			TrainNode tn = (TrainNode) obj;
			isEqual = id == tn.getId() && pId == tn.getPId()
					&& name == tn.getName() && result == tn.getResult()
					&& probability == tn.getProbability()
					&& value.equals(tn.getValue());
		}
		return isEqual;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPId() {
		return pId;
	}

	public void setPId(int pId) {
		this.pId = pId;
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

	public int getIsLeaf() {
		return isLeaf;
	}

	public void setIsLeaf(int isLeaf) {
		this.isLeaf = isLeaf;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}

}
