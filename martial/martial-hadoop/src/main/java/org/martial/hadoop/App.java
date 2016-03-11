package org.martial.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.martial.math.randomforest.tree.ID3Tree;
import org.martial.math.randomforest.tree.TreeMaths;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		Configuration config = HadoopConfig.INSRANCE.getConfiguration();
		// config.setInt("mapreduce.job.reduces", 10);
		/*
		 * ClassificationAlgorithm algorithm = new RandomForest(ID3Tree.class,
		 * 100, 5, 15); String trainInput = "randomForest/train/input"; String
		 * trianOutput = "randomForest/train/output";
		 * algorithm.train(trainInput, trianOutput, config);
		 */
		// String input="randomForest/data/input";
		// String output="randomForest/data/output";
		// algorithm.execute(input, output, trianOutput, config);

		try {
			config.setInt("ClassificationAlgorithm.randomforest.treeNum", 100);
			config.setInt("ClassificationAlgorithm.randomforest.featureMin", 5);
			config.setInt("ClassificationAlgorithm.randomforest.featureMax", 15);
			config.setClass("ClassificationAlgorithm.randomforest.tree.class",
					ID3Tree.class, TreeMaths.class);
			sampling(config);
		} catch (IOException | ClassNotFoundException | InterruptedException e) {
			e.printStackTrace();
		}

	}

	public static void sampling(Configuration config) throws IOException,
			ClassNotFoundException, InterruptedException {
	}

}
