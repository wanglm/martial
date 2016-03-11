package org.martial.math.randomforest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.martial.math.randomforest.entities.RFData;
import org.martial.math.randomforest.entities.TargetIDs;
import org.martial.math.randomforest.entities.TrainNode;
import org.martial.math.randomforest.entities.TreeNodeWritable;
import org.martial.math.randomforest.entities.Trees;
import org.martial.math.randomforest.tree.ID3Tree;
import org.martial.math.randomforest.tree.TreeMaths;
import org.martial.math.utils.JobUtils;
import org.martial.math.utils.MathConfig;

public class Trains {
	private MathConfig mconfig = new MathConfig();

	public Job extract(Configuration config, List<String> inputList,
			String output) throws IOException {
		Job job = Job.getInstance(config, "randomForest-train");
		Path out = JobUtils.reSetOutput(output, config);
		for (String input : inputList) {
			FileInputFormat.addInputPath(job, new Path(input));
		}
		FileOutputFormat.setOutputPath(job, out);
		String jar = mconfig.value("jar");
		job.setJar(jar);
		job.setJarByClass(Trains.class);
		job.setMapperClass(ExtractMapper.class);
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputKeyClass(Text.class);
		job.setReducerClass(ExtractReducer.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		//job.setOutputFormatClass(SequenceFileOutputFormat.class);
		return job;
	}

	public Job Train(Configuration config, String input, String output)
			throws IOException {
		Job job = Job.getInstance(config, "randomForest-train");
		Path out = JobUtils.reSetOutput(output, config);
		FileInputFormat.addInputPath(job, new Path(input));
		FileOutputFormat.setOutputPath(job, out);
		String jar = mconfig.value("jar");
		job.setJar(jar);
		job.setJarByClass(Trains.class);
		job.setMapperClass(TrainMapper.class);
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(Text.class);
		job.setReducerClass(TrainReducer.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(TreeNodeWritable.class);
		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		return job;
	}

	private static class ExtractMapper extends
			Mapper<Object, Text, IntWritable, Text> {
		private Random r = new Random();
		private int treeNum;

		@Override
		protected void setup(
				Mapper<Object, Text, IntWritable, Text>.Context context)
				throws IOException, InterruptedException {
			Configuration config = context.getConfiguration();
			treeNum = config.getInt(
					"ClassificationAlgorithm.randomforest.treeNum", 5);
		}

		@Override
		protected void map(Object key, Text value,
				Mapper<Object, Text, IntWritable, Text>.Context context)
				throws IOException, InterruptedException {
			if (value.getLength() > 0) {
				IntWritable treeId = new IntWritable();
				int n = r.nextInt(treeNum);
				for (int i = 0; i < n; i++) {
					treeId.set(r.nextInt(treeNum));
					context.write(treeId, value);
				}
			}
		}

	}

	private static class ExtractReducer extends
			Reducer<IntWritable, Text, IntWritable, Text> {
		private Random r = new Random();
		private int featureMin;
		private int featureMax;

		@Override
		protected void setup(
				Reducer<IntWritable, Text, IntWritable, Text>.Context context)
				throws IOException, InterruptedException {
			Configuration config = context.getConfiguration();
			featureMin = config.getInt(
					"ClassificationAlgorithm.randomforest.featureMin", 0);
			featureMax = config.getInt(
					"ClassificationAlgorithm.randomforest.featureMax", 5);
		}

		@Override
		protected void reduce(IntWritable key, Iterable<Text> values,
				Reducer<IntWritable, Text, IntWritable, Text>.Context context)
				throws IOException, InterruptedException {
			int num = 0;
			int begin = r.nextInt(featureMin);
			int end = r.nextInt(featureMax);
			if (begin == 0) {
				begin = 1;
			}
			if (begin > end) {
				num = begin;
				begin = end;
				end = num;
			}
			Text newValue = new Text();
			boolean isFirst = true;
			for (Text value : values) {
				String[] line = value.toString().split(",");
				if (isFirst) {
					int len = line.length;
					if (len - end > 5) {
						end = len;
					}
					isFirst = false;
				}
				StringBuilder sb = new StringBuilder();
				sb.append(line[0]);// result
				for (int i = begin; i <= end; i++) {
					sb.append(",");
					sb.append(line[i]);
				}
				newValue.set(sb.toString());
				context.write(key, newValue);
			}
		}

	}

	private static class TrainMapper extends
			Mapper<IntWritable, Text, IntWritable, Text> {

		@Override
		protected void map(IntWritable key, Text value,
				Mapper<IntWritable, Text, IntWritable, Text>.Context context)
				throws IOException, InterruptedException {
			context.write(key, value);
		}

	}

	private static class TrainReducer extends
			Reducer<IntWritable, Text, IntWritable, TreeNodeWritable> {
		private List<Integer> names;
		private TreeMaths tm;

		@Override
		protected void setup(
				Reducer<IntWritable, Text, IntWritable, TreeNodeWritable>.Context context)
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
				tm = treeClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new IOException("获取决策树算法错误：", e);
			}

		}

		@Override
		protected void reduce(
				IntWritable key,
				Iterable<Text> values,
				Reducer<IntWritable, Text, IntWritable, TreeNodeWritable>.Context context)
				throws IOException, InterruptedException {
			int id = 0;
			List<RFData> datas = new ArrayList<RFData>();
			int featureNumber = 0;
			boolean isFirst = true;
			for (Text value : values) {
				String[] line = value.toString().split(",");
				if (isFirst) {
					featureNumber = line.length - 1;
					isFirst = false;
				}
				int result = Integer.parseInt(line[0]);
				for (int i = 1, n = featureNumber + 1; i < n; i++) {
					RFData data = new RFData();
					data.setId(id);
					data.setName(names.get(i).intValue());
					data.setResult(result);
					data.setValue(line[i]);
					datas.add(data);
				}
				id++;
			}
			Trees tree = tm.bulidTree(datas, id, featureNumber);
			TreeNodeWritable node = new TreeNodeWritable();
			List<TrainNode> nodes = tree.getNodes();
			for (int j = 0, m = nodes.size(); j < m; j++) {
				TrainNode tn = nodes.get(j);
				node.setIndex(j);
				node.setId(tn.getId());
				node.setName(tn.getName());
				node.setpId(tn.getPId());
				node.setIsLeaf(tn.getIsLeaf());
				node.setValue(tn.getValue());
				node.setResult(tn.getResult());
				node.setProbability(tn.getProbability());
				context.write(key, node);
			}
		}
	}
}
