package org.martial.math;

import java.util.List;

import org.apache.hadoop.conf.Configuration;

/**
 * 分类算法
 * 
 * @author ming
 *
 */
public interface ClassificationAlgorithm {

	/**
	 * 训练分类器
	 * 
	 * @param input
	 * @param output
	 * @param config
	 */
	public void train(List<String> input, String output, Configuration config);

	/**
	 * 运行分类器
	 * 
	 * @param input
	 * @param output
	 * @param trainInput
	 *            分类器所在路径
	 * @param config
	 */
	public void execute(List<String> input, String output, String trainInput,
			Configuration config);

}
