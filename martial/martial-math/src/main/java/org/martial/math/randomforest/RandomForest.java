package org.martial.math.randomforest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;
import org.martial.math.ClassificationAlgorithm;
import org.martial.math.randomforest.tree.ID3Tree;
import org.martial.math.randomforest.tree.TreeMaths;
import org.martial.math.utils.JobUtils;

/**
 * 随机森林分类算法
 * 
 * @author ming
 *
 */
public class RandomForest implements ClassificationAlgorithm {
	private static final Log LOG = LogFactory.getLog(RandomForest.class);
	private static final String FEATURES_EXTRACT = "randomForest/train/features/";
	private Class<? extends TreeMaths> tree;
	private int treeNum;// 决策树数量
	private int featureMin;// 最少特征数量
	private int featureMax;// 最大特征数量

	public RandomForest(Class<? extends TreeMaths> tree, int treeNum,
			int featureMin, int featureMax) {
		this.tree = tree;
		this.treeNum = treeNum;
		this.featureMin = featureMin;
		this.featureMax = featureMax;
	}

	@Override
	public void train(List<String> input, String output, Configuration config) {
		try {
			config.setInt("ClassificationAlgorithm.randomforest.treeNum",
					treeNum);
			config.setInt("ClassificationAlgorithm.randomforest.featureNum",
					featureMin);
			config.setInt("ClassificationAlgorithm.randomforest.featureMax",
					featureMax);
			config.setClass("ClassificationAlgorithm.randomforest.tree.class",
					tree, ID3Tree.class);
			String featureOutput = FEATURES_EXTRACT
					+ JobUtils.getToday("yyyy-MM-dd");
			Trains train = new Trains();
			Job extractJob = train.extract(config, input, featureOutput);
			extractJob.submit();
		} catch (IOException | ClassNotFoundException | InterruptedException e) {
			LOG.error("随机森林训练出错：", e);
		}

	}

	@Override
	public void execute(List<String> input, String output, String trainInput,
			Configuration config) {
		try {
			Executes exe = new Executes();
			Job job = exe.execute(config, input, output, trainInput);
			job.submit();
		} catch (IOException | URISyntaxException | ClassNotFoundException
				| InterruptedException e) {
			LOG.error("随机森林执行出错：", e);
		}
	}

}
