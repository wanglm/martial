package org.martial.math.recommend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.mahout.cf.taste.hadoop.item.VectorAndPrefsWritable;
import org.apache.mahout.cf.taste.hadoop.item.VectorOrPrefWritable;
import org.apache.mahout.math.VarLongWritable;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.math.Vector.Element;

/**收集向量
 * @author ming
 *
 */
public class CollectorVectors {
	/**
	 * 封装共现矩阵关系列
	 * 
	 * @author ming
	 *
	 */
	public static class CoWrapper
			extends
			Mapper<IntWritable, VectorWritable, IntWritable, VectorOrPrefWritable> {

		@Override
		protected void map(
				IntWritable key,
				VectorWritable value,
				Mapper<IntWritable, VectorWritable, IntWritable, VectorOrPrefWritable>.Context context)
				throws IOException, InterruptedException {
			context.write(key, new VectorOrPrefWritable(value.get()));
		}

	}

	/**
	 * 分割用户向量
	 * 
	 * @author ming
	 *
	 */
	public static class UserVectorSplitter
			extends
			Mapper<VarLongWritable, VectorWritable, IntWritable, VectorOrPrefWritable> {

		@Override
		protected void map(
				VarLongWritable key,
				VectorWritable value,
				Mapper<VarLongWritable, VectorWritable, IntWritable, VectorOrPrefWritable>.Context context)
				throws IOException, InterruptedException {
			long userID = key.get();
			Vector userVector = value.get();
			Iterator<Element> it = userVector.nonZeroes().iterator();
			IntWritable itemIndexWritable = new IntWritable();
			while (it.hasNext()) {
				Element e = it.next();
				int itemIndex = e.index();
				float preferenceValue = (float) e.get();
				itemIndexWritable.set(itemIndex);
				context.write(itemIndexWritable, new VectorOrPrefWritable(
						userID, preferenceValue));
			}
		}

	}

	/**
	 * 什么都不做，作为收集mapreduce的输入存储
	 * 收集用户和物品的关系向量
	 * 
	 * @author ming
	 *
	 */
	public static class NoneDoReducer
			extends
			Reducer<IntWritable, VectorOrPrefWritable, IntWritable, VectorOrPrefWritable> {

		@Override
		protected void reduce(
				IntWritable key,
				Iterable<VectorOrPrefWritable> value,
				Reducer<IntWritable, VectorOrPrefWritable, IntWritable, VectorOrPrefWritable>.Context context)
				throws IOException, InterruptedException {
			for(VectorOrPrefWritable val:value){
				context.write(key, val);
			}
		}

	}
	
	/**什么都不做，简单的收集两个输入传递到reducer
	 * @author ming
	 *
	 */
	public static class NoneDoMapper extends Mapper<IntWritable, VectorOrPrefWritable,IntWritable, VectorOrPrefWritable>{

		@Override
		protected void map(
				IntWritable key,
				VectorOrPrefWritable value,
				Mapper<IntWritable, VectorOrPrefWritable, IntWritable, VectorOrPrefWritable>.Context context)
				throws IOException, InterruptedException {
			context.write(key, value);
		}
		
		
	}
	
	/**
	 * 
	 * 收集用户和物品的关系向量
	 * 
	 * @author ming
	 *
	 */
	public static class CollectReducer
			extends
			Reducer<IntWritable, VectorOrPrefWritable, IntWritable, VectorAndPrefsWritable> {

		@Override
		protected void reduce(
				IntWritable key,
				Iterable<VectorOrPrefWritable> value,
				Reducer<IntWritable, VectorOrPrefWritable, IntWritable, VectorAndPrefsWritable>.Context context)
				throws IOException, InterruptedException {
			List<Long> userIDs = new ArrayList<Long>();
			List<Float> preferenceValues = new ArrayList<Float>();
			Vector vector = null;
			Vector _vector=null;
			for (VectorOrPrefWritable val : value) {
				_vector = val.getVector();
				if(_vector!=null){
					vector=_vector;
					continue;
				}
				userIDs.add(val.getUserID());
				preferenceValues.add(val.getValue());
			}
			VectorAndPrefsWritable vp = new VectorAndPrefsWritable(vector,
					userIDs, preferenceValues);
			context.write(key, vp);
		}

	}
}
