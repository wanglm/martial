package org.martial.math.recommend;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.VarLongWritable;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;
import org.apache.mahout.math.VectorWritable;

/**
 * 共现矩阵
 * 
 * @author ming
 *
 */
public class Cooccurrence {

	public static class CoMapper extends
			Mapper<VarLongWritable, VectorWritable, IntWritable, IntWritable> {

		@Override
		protected void map(
				VarLongWritable key,
				VectorWritable value,
				Mapper<VarLongWritable, VectorWritable, IntWritable, IntWritable>.Context context)
				throws IOException, InterruptedException {
			Iterable<Element> it = value.get().nonZeroes();
			Iterator<Element> it1 = it.iterator();
			while (it1.hasNext()) {
				int index1 = it1.next().index();
				Iterator<Element> it2 = it.iterator();
				while (it2.hasNext()) {
					int index2 = it2.next().index();
					context.write(new IntWritable(index1), new IntWritable(
							index2));
				}
			}
		}

	}

	public static class coReducer extends
			Reducer<IntWritable, IntWritable, IntWritable, VectorWritable> {

		@Override
		protected void reduce(
				IntWritable key,
				Iterable<IntWritable> value,
				Reducer<IntWritable, IntWritable, IntWritable, VectorWritable>.Context context)
				throws IOException, InterruptedException {
			Vector coRow = new RandomAccessSparseVector(Integer.MAX_VALUE, 100);
			value.forEach(e -> {
				int itemIndex2 = e.get();
				coRow.set(itemIndex2, coRow.get(itemIndex2) + 1.0);
			});
			context.write(key, new VectorWritable(coRow));

		}

	}
}
