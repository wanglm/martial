package org.martial.math.recommend;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.VarLongWritable;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;

/**
 * 将原始数据转换为用户向量型数据 需要预处理
 * 
 * @author ming
 *
 */
public class UserVectors {

	public static class UserVectorMapper extends
			Mapper<LongWritable, Text, VarLongWritable, VarLongWritable> {

		@Override
		protected void map(
				LongWritable key,
				Text value,
				Mapper<LongWritable, Text, VarLongWritable, VarLongWritable>.Context context)
				throws IOException, InterruptedException {
			VarLongWritable userID = new VarLongWritable(key.get());
			VarLongWritable itemID = new VarLongWritable();
			String[] strs = value.toString().split(",");
			for (String str : strs) {
				itemID.set(Long.valueOf(str));
				context.write(userID, itemID);
			}
		}

	}

	public static class UserVectorReducer
			extends
			Reducer<VarLongWritable, VarLongWritable, VarLongWritable, VectorWritable> {

		@Override
		protected void reduce(
				VarLongWritable key,
				Iterable<VarLongWritable> value,
				Reducer<VarLongWritable, VarLongWritable, VarLongWritable, VectorWritable>.Context context)
				throws IOException, InterruptedException {
			Vector userVector=new RandomAccessSparseVector(Integer.MAX_VALUE, 100);
			value.forEach(e->{
				userVector.set((int) e.get(),1.0f);
			});
			context.write(key, new VectorWritable(userVector));
		}

	}
}
