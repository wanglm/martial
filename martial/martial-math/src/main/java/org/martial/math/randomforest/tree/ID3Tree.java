package org.martial.math.randomforest.tree;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.lucene.util.MathUtil;
import org.martial.math.randomforest.entities.RFData;
import org.martial.math.randomforest.entities.RFentry;
import org.martial.math.randomforest.entities.RFfeatures;
import org.martial.math.randomforest.entities.TargetIDs;
import org.martial.math.randomforest.entities.TrainNode;
import org.martial.math.randomforest.entities.Trees;
import org.martial.math.utils.BaseMathUtils;
import org.martial.math.utils.JobUtils;

public class ID3Tree implements TreeMaths {
	private int size;// 数据总数

	/**
	 * @param allCount
	 *            特征总数==数据总数
	 * @param featureNum
	 *            特征该值的数量
	 * @param num
	 *            特征该值该判定结果的数量
	 * @return
	 */
	public double entropy(BigDecimal allCount, int featureNum, int num) {
		BigDecimal fNum = new BigDecimal(featureNum);
		BigDecimal rNum = new BigDecimal(num);
		double valueRatio = fNum.divide(allCount, 4, BigDecimal.ROUND_HALF_UP)
				.doubleValue();
		double ratio = rNum.divide(fNum, 4, BigDecimal.ROUND_HALF_UP)
				.doubleValue();
		return 0 - valueRatio * MathUtil.log(2, ratio);
	}

	/**
	 * 经验熵
	 * 
	 * @param resultMap
	 * @return
	 */
	public double empEntropy(Map<Integer, Integer> resultMap,
			BigDecimal allCount) {
		double entropy = resultMap
				.values()
				.parallelStream()
				.map(e -> {
					BigDecimal num = new BigDecimal(e.intValue());
					double p = num
							.divide(allCount, 4, BigDecimal.ROUND_HALF_UP)
							.doubleValue();
					return 0 - p * MathUtil.log(2, p);
				}).reduce(0d, Double::sum);
		return entropy;
	}

	/**
	 * 计算切分节点
	 * 
	 * @param list
	 *            特征集合
	 * @param allCount
	 *            数据总数,即id总数
	 * @return 节点名+值
	 */
	public List<RFentry> math(List<RFData> list, int allCount) {
		BigDecimal all = new BigDecimal(allCount);
		// 根据结果分组统计
		Map<Integer, Long> resultMap = list.parallelStream().collect(
				Collectors.groupingByConcurrent(e -> {
					return e.getResult();
				}, Collectors.counting()));
		// 根据特征分组统计
		Map<RFfeatures, Long> featuresMap = list.parallelStream().collect(
				Collectors.groupingByConcurrent(e -> {
					RFfeatures feature = new RFfeatures();
					feature.setName(e.getName());
					feature.setResult(e.getResult());
					feature.setValue(e.getValue());
					return feature;
				}, Collectors.counting()));
		// double empEntropy = empEntropy(resultMap, all);
		// 计算每个特征每个值的熵
		List<RFentry> rfList = featuresMap
				.entrySet()
				.parallelStream()
				.map(e -> {
					int rNum = resultMap.get(e.getKey().getResult()).intValue();
					RFentry entry = new RFentry();
					entry.setName(e.getKey().getName());
					entry.setValue(e.getKey().getValue());
					entry.setEntropy(entropy(all, e.getValue().intValue(), rNum));
					return entry;
				}).collect(Collectors.toList());
		// 分组统计计算熵
		Map<Integer, List<RFentry>> nameMap = rfList.parallelStream().collect(
				Collectors.groupingByConcurrent(e -> {
					return e.getName();
				}));
		// 找到最小的特征熵
		RFentry minRFentry = nameMap.entrySet().parallelStream().map(e -> {
			double entropyFeature = 0d;
			for (RFentry rfe : e.getValue()) {
				entropyFeature += rfe.getEntropy();
			}
			RFentry f = new RFentry();
			f.setName(e.getKey().intValue());
			f.setEntropy(entropyFeature);
			return f;
		}).min((a, b) -> {
			return Double.compare(a.getEntropy(), b.getEntropy());
		}).get();

		// 返回子特征集，去重
		return nameMap.get(minRFentry.getName()).parallelStream().distinct()
				.collect(Collectors.toList());
	}

	/**
	 * @param tree
	 * @param datas
	 * @param parentId
	 *            父节点
	 * @param name
	 *            特征名
	 * @param value
	 *            值
	 * @param index
	 *            同层索引
	 * @param featureNumber
	 *            剩余特征数量
	 */
	public void addNode(Trees tree, List<RFData> datas, int parentId, int name,
			String value, int index, int featureNumber) {
		List<RFData> currentNodeList = null;
		TrainNode node = new TrainNode();
		node.setName(name);
		node.setValue(value);
		node.setPId(parentId);
		boolean isBegin = parentId == TreeMaths.BEGIN;
		int n = 0;
		if (isBegin) {
			node.setId(TargetIDs.ROOT);
			tree.addNode(node);
			List<RFentry> rootChildren = math(datas, size);
			for (RFentry child : rootChildren) {
				addNode(tree, datas, node.getId(), child.getName(),
						child.getValue(), n, featureNumber);
				n++;
			}
		} else {
			featureNumber--;
			node.setId(JobUtils.buildId(name, featureNumber, index));
			// 符合当前节点的原始数据,得到对应的id
			currentNodeList = datas.parallelStream().filter(e -> {
				return e.getName() == name && e.getValue().equals(value);
			}).collect(Collectors.toList());
			// 值-对应的数据ID
			Set<Long> idSet = currentNodeList.parallelStream().map(e -> {
				return e.getId();
			}).distinct().collect(Collectors.toSet());
			if (featureNumber > 0) {
				if (idSet.size() == 1) {
					// 只有一条数据，那就是100%可能，不用递归了
					node.setIsLeaf(TrainNode.LEAF);
					node.setResult(currentNodeList.get(0).getResult());
					node.setProbability(100d);
					tree.addNode(node);
				} else {
					tree.addNode(node);// 添加当前节点
					// 获取按值分的统计数据集
					List<RFData> list = datas
							.parallelStream()
							.filter(e -> {
								return e.getName() != name
										&& idSet.contains(e.getId());
							}).collect(Collectors.toList());
					int allCount = idSet.size();// 数据总数
					List<RFentry> re = math(list, allCount);
					for (RFentry child : re) {
						addNode(tree, list, node.getId(), child.getName(),
								child.getValue(), n, featureNumber);
						n++;
					}
				}
			} else {
				Map<Integer, Long> resultMap = datas.parallelStream()
						.filter(e -> {
							return idSet.contains(e.getId());
						}).collect(Collectors.groupingByConcurrent(e -> {
							return e.getResult();
						}, Collectors.counting()));
				Entry<Integer, Long> entryMax = null;
				long resultSize = 0l;
				for (Entry<Integer, Long> e : resultMap.entrySet()) {
					long elong = e.getValue().longValue();
					resultSize += elong;
					if (entryMax != null
							&& elong <= entryMax.getValue().longValue()) {
						continue;
					}
					entryMax = e;
				}
				double probability = BaseMathUtils.probability(entryMax
						.getValue().longValue(), resultSize);

				node.setIsLeaf(TrainNode.LEAF);
				node.setResult(entryMax.getKey().intValue());
				node.setProbability(probability);
				tree.addNode(node);
			}
		}

	}

	@Override
	public Trees bulidTree(List<RFData> datas, int size,
			int featureNumber) {
		Trees tree = new Trees(featureNumber);
		this.size = size;
		addNode(tree, datas, TreeMaths.BEGIN, TargetIDs.ROOT, "",
				TreeMaths.BEGIN, featureNumber);
		return tree;
	}

}
