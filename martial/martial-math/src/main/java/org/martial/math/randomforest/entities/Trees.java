package org.martial.math.randomforest.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * 决策树模型
 * 
 * @author ming
 *
 */
public class Trees {
	private List<TrainNode> nodes;

	public Trees(int features) {
		this.nodes = new ArrayList<TrainNode>(features);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (TrainNode node : nodes) {
			sb.append(node.toString());
			sb.append("\n");
		}
		return sb.toString();
	}


	public List<TrainNode> getNodes() {
		return nodes;
	}

	public void addNode(TrainNode node) {
		nodes.add(node);
	}
}
