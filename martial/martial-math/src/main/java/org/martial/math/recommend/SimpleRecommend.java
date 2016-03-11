package org.martial.math.recommend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.mahout.cf.taste.hadoop.RecommendedItemsWritable;
import org.apache.mahout.cf.taste.hadoop.item.VectorAndPrefsWritable;
import org.apache.mahout.cf.taste.impl.recommender.ByValueRecommendedItemComparator;
import org.apache.mahout.cf.taste.impl.recommender.GenericRecommendedItem;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.math.VarLongWritable;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.math.Vector.Element;

public class SimpleRecommend {
	private static final int NUM = 5;// 推荐数量

	/**
	 * 计算推荐向量
	 * 
	 * @author ming
	 *
	 */
	public static class RecommedMapper
			extends
			Mapper<IntWritable, VectorAndPrefsWritable, VarLongWritable, VectorWritable> {

		@Override
		protected void map(
				IntWritable key,
				VectorAndPrefsWritable value,
				Mapper<IntWritable, VectorAndPrefsWritable, VarLongWritable, VectorWritable>.Context context)
				throws IOException, InterruptedException {
			Vector coColumn = value.getVector();
			List<Long> userIDs = value.getUserIDs();
			List<Float> prefValues = value.getValues();
			for (int i = 0, n = userIDs.size(); i < n; i++) {
				long userID = userIDs.get(i);
				float prefValue = prefValues.get(i);
				Vector partialProduct = coColumn.times(prefValue);
				context.write(new VarLongWritable(userID), new VectorWritable(
						partialProduct));
			}
		}

	}

	/**
	 * 本地部分聚合
	 * 
	 * @author ming
	 *
	 */
	public static class RecommedCombiner
			extends
			Reducer<VarLongWritable, VectorWritable, VarLongWritable, VectorWritable> {

		@Override
		protected void reduce(
				VarLongWritable key,
				Iterable<VectorWritable> values,
				Reducer<VarLongWritable, VectorWritable, VarLongWritable, VectorWritable>.Context context)
				throws IOException, InterruptedException {
			Vector partial = null;
			for (VectorWritable value : values) {
				partial = (partial == null) ? value.get() : partial.plus(value
						.get());
			}
			context.write(key, new VectorWritable(partial));
		}

	}

	/**
	 * 形成推荐
	 * 
	 * @author ming
	 *
	 */
	public static class RecommedReducer
			extends
			Reducer<VarLongWritable, VectorWritable, VarLongWritable, RecommendedItemsWritable> {

		@Override
		protected void reduce(
				VarLongWritable key,
				Iterable<VectorWritable> values,
				Reducer<VarLongWritable, VectorWritable, VarLongWritable, RecommendedItemsWritable>.Context context)
				throws IOException, InterruptedException {
			Vector recommendationVector = null;
			for (VectorWritable value : values) {
				recommendationVector = (recommendationVector == null) ? value
						.get() : recommendationVector.plus(value.get());
			}
			Queue<RecommendedItem> topitems = new PriorityQueue<RecommendedItem>(
					NUM,
					Collections.reverseOrder(ByValueRecommendedItemComparator
							.getInstance()));
			Iterator<Element> it = recommendationVector.nonZeroes().iterator();
			while (it.hasNext()) {
				Element e = it.next();
				int index = e.index();
				float value = (float) e.get();
				if (topitems.size() < NUM) {
					topitems.add(new GenericRecommendedItem(index, value));
				} else if (value > topitems.peek().getValue()) {
					// 优先queue添加元素必须非空，且自动排序
					// 获取指定数量的排好序的推荐序列
					topitems.add(new GenericRecommendedItem(index, value));
					topitems.poll();
				}
			}
			List<RecommendedItem> recommendations = new ArrayList<RecommendedItem>(
					topitems.size());
			recommendations.addAll(topitems);
			Collections.sort(recommendations,
					ByValueRecommendedItemComparator.getInstance());
			context.write(key, new RecommendedItemsWritable(recommendations));
		}

	}

}
