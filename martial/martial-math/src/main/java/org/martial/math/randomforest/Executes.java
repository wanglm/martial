package org.martial.math.randomforest;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.martial.math.entities.ResultWritable;
import org.martial.math.randomforest.entities.TargetIDs;
import org.martial.math.randomforest.entities.TrainNode;
import org.martial.math.randomforest.entities.TreeNodeWritable;
import org.martial.math.randomforest.entities.Trees;
import org.martial.math.randomforest.tree.ID3Tree;
import org.martial.math.randomforest.tree.TreeMaths;
import org.martial.math.utils.JobUtils;
import org.martial.math.utils.MathConfig;

public class Executes {
	private MathConfig mconfig = new MathConfig();

	public Job execute(Configuration config, List<String> inputList,
			String output, String trianInput) throws IOException,
			URISyntaxException {
		Job job = Job.getInstance(config, "randomForest-train");
		Path out = JobUtils.reSetOutput(output, config);
		for (String input : inputList) {
			FileInputFormat.addInputPath(job, new Path(input));
		}
		FileOutputFormat.setOutputPath(job, out);
		String jar = mconfig.value("jar");
		job.setJar(jar);
		job.setJarByClass(Trains.class);
		URI cache = new URI(trianInput);
		job.addCacheFile(cache);
		job.setMapperClass(ExecuteMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(ResultWritable.class);
		job.setCombinerClass(ExecuteCombiner.class);
		job.setReducerClass(ExecuteReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(ResultWritable.class);
		return job;
	}

	private static class ExecuteMapper extends
			Mapper<Object, Text, Text, ResultWritable> {
		private int resultNum;
		private List<Integer> names;
		private TreeMaths math;
		private List<Trees> forest;

		@Override
		protected void setup(
				Mapper<Object, Text, Text, ResultWritable>.Context context)
				throws IOException, InterruptedException {
			try {
				names = new ArrayList<Integer>(11);
				names.add(TargetIDs.RESULT);
				names.add(TargetIDs.ACT);
				names.add(TargetIDs.UDID);
				names.add(TargetIDs.IMSI);
				names.add(TargetIDs.IP);
				names.add(TargetIDs.NET);
				names.add(TargetIDs.SDK_VERSION);
				names.add(TargetIDs.VERSION);
				names.add(TargetIDs.APP_VERSION);
				names.add(TargetIDs.DEVICE_NAME);
				names.add(TargetIDs.CHANNEL);

				Configuration config = context.getConfiguration();
				Class<? extends TreeMaths> treeClass = config.getClass(
						"ClassificationAlgorithm.randomforest.treeMath",
						ID3Tree.class, TreeMaths.class);
				math = treeClass.newInstance();

				resultNum = config.getInt(
						"ClassificationAlgorithm.randomforest.result.num", 0);

				String trainPath = context.getCacheFiles()[0].getPath();
				IntWritable key = new IntWritable();
				TreeNodeWritable value = new TreeNodeWritable();
				MapWritable map = JobUtils.readSequenceFile(config, new Path(
						trainPath), key, value);
				Map<Writable, List<Entry<Writable, Writable>>> groupMap = map
						.entrySet().parallelStream()
						.collect(Collectors.groupingByConcurrent(e -> {
							return e.getKey();
						}));
				forest = new ArrayList<Trees>(groupMap.size());
				for (List<Entry<Writable, Writable>> list : groupMap.values()) {
					List<TreeNodeWritable> _list = list
							.parallelStream()
							.map(e -> {
								TreeNodeWritable tnw = (TreeNodeWritable) e
										.getValue();
								return tnw;
							})
							.sorted((a, b) -> {
								return Integer.compare(a.getIndex().get(), b
										.getIndex().get());
							}).collect(Collectors.toList());
					Trees tree = new Trees(_list.size());
					for (TreeNodeWritable tnw : _list) {
						TrainNode node = new TrainNode();
						node.setId(tnw.getId().get());
						node.setIsLeaf(tnw.getIsLeaf().get());
						node.setName(tnw.getName().get());
						node.setPId(tnw.getpId().get());
						node.setProbability(tnw.getProbability().get());
						node.setResult(tnw.getResult().get());
						node.setValue(tnw.getValue().toString());
						tree.addNode(node);
					}
					forest.add(tree);
				}
			} catch (InstantiationException | IllegalAccessException e) {
				throw new IOException("获取决策树算法错误：", e);
			}
		}

		@Override
		protected void map(Object key, Text value,
				Mapper<Object, Text, Text, ResultWritable>.Context context)
				throws IOException, InterruptedException {
			String[] line = value.toString().split(",");
			int n = line.length - 1;
			Map<Integer, String> map = new HashMap<Integer, String>(n);
			for (int i = 1, m = n + 1; i < m; i++) {
				map.put(names.get(i - 1), line[i]);
			}
			ResultWritable result = null;
			Text id = new Text(line[0]);
			Map<Integer, ResultWritable> results = new HashMap<Integer, ResultWritable>(
					resultNum);
			for (Trees tree : forest) {
				TrainNode node = math.judge(tree, map);
				int resultName = node.getResult();
				if (results.containsKey(resultName)) {
					result = results.get(resultName);
					result.addProbability(node.getProbability());
					result.addNum(1);
					results.replace(resultName, result);
				} else {
					result = new ResultWritable();
					result.setResult(resultName);
					result.setProbability(node.getProbability());
					results.put(resultName, result);
				}
			}
			for (ResultWritable e : results.values()) {
				context.write(id, e);
			}

		}
	}

	private static class ExecuteCombiner extends
			Reducer<Text, ResultWritable, Text, ResultWritable> {
		private int resultNum;

		@Override
		protected void setup(
				Reducer<Text, ResultWritable, Text, ResultWritable>.Context context)
				throws IOException, InterruptedException {
			Configuration config = context.getConfiguration();
			resultNum = config.getInt(
					"ClassificationAlgorithm.randomforest.result.num", 0);
		}

		@Override
		protected void reduce(
				Text key,
				Iterable<ResultWritable> values,
				Reducer<Text, ResultWritable, Text, ResultWritable>.Context context)
				throws IOException, InterruptedException {
			Map<Integer, ResultWritable> results = countResult(resultNum,
					values);
			for (ResultWritable e : results.values()) {
				context.write(key, e);
			}
		}

	}

	private static class ExecuteReducer extends
			Reducer<Text, ResultWritable, Text, ResultWritable> {
		private int resultNum;

		@Override
		protected void setup(
				Reducer<Text, ResultWritable, Text, ResultWritable>.Context context)
				throws IOException, InterruptedException {
			Configuration config = context.getConfiguration();
			resultNum = config.getInt(
					"ClassificationAlgorithm.randomforest.result.num", 0);
		}

		@Override
		protected void reduce(
				Text key,
				Iterable<ResultWritable> values,
				Reducer<Text, ResultWritable, Text, ResultWritable>.Context context)
				throws IOException, InterruptedException {
			Map<Integer, ResultWritable> results = countResult(resultNum,
					values);
			ResultWritable finalResult = results
					.values()
					.parallelStream()
					.map(e -> {
						BigDecimal b = new BigDecimal(e.getProbability().get());
						BigDecimal n = new BigDecimal(e.getNum().get());
						double p = b.divide(n, 4, BigDecimal.ROUND_HALF_UP)
								.doubleValue();
						e.setProbability(p);
						return e;
					})
					.max((a, b) -> {
						return Double.compare(a.getProbability().get(), b
								.getProbability().get());
					}).get();
			context.write(key, finalResult);
		}
	}

	public static Map<Integer, ResultWritable> countResult(int resultNum,
			Iterable<ResultWritable> values) {
		Map<Integer, ResultWritable> results = new HashMap<Integer, ResultWritable>(
				resultNum);
		for (ResultWritable value : values) {
			int resultName = value.getResult().get();
			if (results.containsKey(resultName)) {
				ResultWritable result = results.get(resultName);
				result.addProbability(value.getProbability().get());
				result.addNum(value.getNum().get());
				results.replace(resultName, result);
			} else {
				results.put(resultName, value);
			}
		}
		return results;
	}
}
