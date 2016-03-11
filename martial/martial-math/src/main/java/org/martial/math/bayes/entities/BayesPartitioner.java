package org.martial.math.bayes.entities;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Partitioner;

public class BayesPartitioner extends Partitioner<BayesEntity, IntWritable> {

	@Override
	public int getPartition(BayesEntity key, IntWritable value,
			int numPartitions) {
		return Math.abs(key.hashCode()) % numPartitions;
	}
}
