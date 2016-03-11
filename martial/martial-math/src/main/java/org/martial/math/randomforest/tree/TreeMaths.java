package org.martial.math.randomforest.tree;

import java.util.List;
import java.util.Map;

import org.martial.math.randomforest.entities.RFData;
import org.martial.math.randomforest.entities.TargetIDs;
import org.martial.math.randomforest.entities.TrainNode;
import org.martial.math.randomforest.entities.Trees;

/**
 * 决策树
 * 
 * @author ming
 *
 */
public interface TreeMaths {
	public static final int BEGIN = -1;

	/**
	 * 决策判断
	 * 
	 * @param tree
	 * @param map
	 *            需要判断的数据
	 * @return
	 */
	default TrainNode judge(Trees tree, Map<Integer, String> map)
			throws IllegalArgumentException {
		List<TrainNode> nodes = tree.getNodes();
		TrainNode judgeNode = null;
		String value = null;
		int pid = TargetIDs.ROOT;
		for (TrainNode node : nodes) {
			if (node.getPId() == pid) {
				int name = node.getName();
				if (map.containsKey(name)) {
					value = map.get(name);
					if (value.equals(node.getValue())) {
						pid = node.getId();
						if (node.getIsLeaf() == 1) {
							judgeNode = node;
							break;
						}
					}
				} else {
					throw new IllegalArgumentException("需要判断的数据有问题,key无法对应:"
							+ name);
				}
			}

		}

		return judgeNode;
	}

	/**
	 * 创建决策树
	 * 
	 * @return
	 */
	public Trees bulidTree(List<RFData> datas, int size,
			int featureNumber);
}
