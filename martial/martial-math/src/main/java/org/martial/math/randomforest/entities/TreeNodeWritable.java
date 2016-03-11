package org.martial.math.randomforest.entities;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

public class TreeNodeWritable implements Writable {
	private IntWritable index;// 在决策树中list中的排序
	private IntWritable id;
	private IntWritable pId;// 上一层的父id
	private IntWritable name;
	private Text value;// 值
	private IntWritable isLeaf;// 默认不为叶子节点
	private IntWritable result;// 最可能的结果
	private DoubleWritable probability;// 概率

	public TreeNodeWritable() {
		this.index = new IntWritable();
		this.id = new IntWritable();
		this.pId = new IntWritable();
		this.name = new IntWritable();
		this.value = new Text();
		this.isLeaf = new IntWritable();
		this.result = new IntWritable();
		this.probability = new DoubleWritable();
	}

	@Override
	public int hashCode() {
		return index.get() + id.get() + pId.get() + name.get() + isLeaf.get()
				+ result.get() + value.hashCode() + probability.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof TreeNodeWritable) {
			TreeNodeWritable _obj = (TreeNodeWritable) obj;
			isEqual = index.get() == _obj.getIndex().get()
					&& id.get() == _obj.getId().get()
					&& pId.get() == _obj.getpId().get()
					&& name.get() == _obj.getName().get()
					&& isLeaf.get() == _obj.getIsLeaf().get()
					&& result.get() == _obj.getResult().get()
					&& probability.get() == _obj.getProbability().get()
					&& value.equals(_obj.getValue());
		}
		return isEqual;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("index:");
		sb.append(index.toString());
		sb.append(",ID:");
		sb.append(id.toString());
		sb.append(",PID:");
		sb.append(pId.toString());
		sb.append(",特征名：");
		sb.append(name.toString());
		sb.append(",特征值：");
		sb.append(value.toString());
		sb.append(",叶子：");
		sb.append(isLeaf.toString());
		sb.append(",判定：");
		sb.append(result.toString());
		sb.append(",概率：");
		sb.append(probability.toString());
		return sb.toString();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		this.index.write(out);
		this.id.write(out);
		this.pId.write(out);
		this.name.write(out);
		this.value.write(out);
		this.isLeaf.write(out);
		this.result.write(out);
		this.probability.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.index.readFields(in);
		this.id.readFields(in);
		this.pId.readFields(in);
		this.name.readFields(in);
		this.value.readFields(in);
		this.isLeaf.readFields(in);
		this.result.readFields(in);
		this.probability.readFields(in);
	}

	public IntWritable getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index.set(index);
	}

	public IntWritable getId() {
		return id;
	}

	public void setId(int id) {
		this.id.set(id);
	}

	public IntWritable getpId() {
		return pId;
	}

	public void setpId(int pId) {
		this.pId.set(pId);
	}

	public IntWritable getName() {
		return name;
	}

	public void setName(int name) {
		this.name.set(name);
	}

	public Text getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value.set(value);
	}

	public IntWritable getIsLeaf() {
		return isLeaf;
	}

	public void setIsLeaf(int isLeaf) {
		this.isLeaf.set(isLeaf);
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

}
