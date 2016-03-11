package org.martial.math.bayes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.martial.math.bayes.entities.BayesData;
import org.martial.math.bayes.entities.BayesEntity;
import org.martial.math.bayes.entities.BayesJudge;
import org.martial.math.bayes.entities.BayesPartitioner;
import org.martial.math.bayes.entities.DecompositionImpl;
import org.martial.math.bayes.entities.Decompositioner;
import org.martial.math.utils.BaseMathUtils;
import org.martial.math.utils.JobUtils;
import org.martial.math.utils.MathConfig;

public class NativeBayes {
	private MathConfig mconfig = new MathConfig();

	public Job statistical(Configuration config, List<String> inputList,
			String output) throws IOException {
		Job job = Job.getInstance(config, "randomForest-convert");
		Path out = JobUtils.reSetOutput(output, config);
		for (String input : inputList) {
			FileInputFormat.addInputPath(job, new Path(input));
		}
		FileOutputFormat.setOutputPath(job, out);
		job.setJarByClass(NativeBayes.class);
		String jar = mconfig.value("jar");
		job.setJar(jar);
		job.setMapperClass(StatisticalMapper.class);
		job.setMapOutputKeyClass(BayesEntity.class);
		job.setMapOutputValueClass(IntWritable.class);
		job.setPartitionerClass(BayesPartitioner.class);
		job.setCombinerClass(StatisticalCombiner.class);
		job.setReducerClass(StatisticalReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(BayesEntity.class);
		return job;
	}

	private static class StatisticalMapper extends
			Mapper<Object, Text, BayesEntity, IntWritable> {
		private Decompositioner deco;// 数据分解器
		private final IntWritable ONE = new IntWritable(1);

		@Override
		protected void setup(
				Mapper<Object, Text, BayesEntity, IntWritable>.Context context)
				throws IOException, InterruptedException {
			try {
				Configuration config = context.getConfiguration();
				Class<? extends Decompositioner> decoClass = config.getClass(
						"ClassificationAlgorithm.randomforest.treeMath",
						DecompositionImpl.class, Decompositioner.class);
				deco = decoClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new IOException("获取数据分解器出错：", e);
			}
		}

		@Override
		protected void map(Object key, Text value,
				Mapper<Object, Text, BayesEntity, IntWritable>.Context context)
				throws IOException, InterruptedException {
			List<BayesEntity> list = deco.execute(value);
			for (BayesEntity e : list) {
				context.write(e, ONE);
				// 先验数量
				e.setPrior(BayesEntity.PRIOR);
				context.write(e, ONE);
			}
			// 总数
			context.write(BayesEntity.getBase(), ONE);
		}

	}

	private static class StatisticalCombiner extends
			Reducer<BayesEntity, IntWritable, BayesEntity, IntWritable> {

		@Override
		protected void reduce(
				BayesEntity key,
				Iterable<IntWritable> values,
				Reducer<BayesEntity, IntWritable, BayesEntity, IntWritable>.Context context)
				throws IOException, InterruptedException {

			int n = 0;
			for (IntWritable value : values) {
				n += value.get();
			}
			context.write(key, new IntWritable(n));
		}

	}

	private static class StatisticalReducer extends
			Reducer<BayesEntity, IntWritable, Text, BayesEntity> {
		private String[] priors;// 先验概率取值集合

		@Override
		protected void reduce(
				BayesEntity key,
				Iterable<IntWritable> values,
				Reducer<BayesEntity, IntWritable, Text, BayesEntity>.Context context)
				throws IOException, InterruptedException {
			int n = 0;
			for (IntWritable value : values) {
				n += value.get();
			}
			key.setNum(n);
			Text priorKey = new Text();
			if (key.getPrior().toString().equals(BayesEntity.PRIOR)) {
				// 总数
				for (String prior : priors) {
					priorKey.set(prior);
					context.write(priorKey, key);
				}
			} else {
				priorKey.set(key.getPrior().toString());
				context.write(priorKey, key);
			}
		}

	}

	private static class MathMapper extends
			Mapper<Text, BayesEntity, Text, BayesEntity> {

		@Override
		protected void map(Text key, BayesEntity value,
				Mapper<Text, BayesEntity, Text, BayesEntity>.Context context)
				throws IOException, InterruptedException {
			context.write(key, value);
		}

	}

	private static class MathReducer extends
			Reducer<Text, BayesEntity, BayesData, Text> {
		private int priorsSize;

		@Override
		protected void reduce(Text key, Iterable<BayesEntity> values,
				Reducer<Text, BayesEntity, BayesData, Text>.Context context)
				throws IOException, InterruptedException {
			List<BayesEntity> list = new ArrayList<BayesEntity>();
			int all = 0;
			Map<String, BayesEntity> map = new HashMap<String, BayesEntity>();
			int priorNum = 0;
			for (BayesEntity value : values) {
				if (value.getPrior().toString().equals(BayesEntity.PRIOR)) {
					if (value.getName().toString().equals(BayesEntity.NAME)) {
						all = value.getNum().get();
					} else {
						map.put(value.getName().toString(), value);
					}
				} else {
					priorNum += value.getNum().get();
					list.add(value);
				}
			}
			double priorProb = BaseMathUtils.probability(priorNum, all);// P(result=?)
			BayesData data = new BayesData();
			Text probText = new Text();
			for (BayesEntity e : list) {
				BayesEntity posterior = map.get(e.getName().toString());
				// P(name=?|result=?)
				double currentProb = BaseMathUtils.probability(
						e.getNum().get(), priorNum, 0, priorsSize);
				// P(name=?)
				double posteriorProb = BaseMathUtils.probability(posterior
						.getNum().get(), all);
				// P(result=?|name=?)
				double probability = BaseMathUtils.probability(priorProb
						* currentProb, posteriorProb);
				probText.set("prob:" + probability);
				context.write(data, key);
			}
		}

	}

	private static class JudgeMapper extends
			Mapper<BayesData, Text, BayesData, Text> {
		private String[] results;// 指定想得到的判定结果

		@Override
		protected void setup(
				Mapper<BayesData, Text, BayesData, Text>.Context context)
				throws IOException, InterruptedException {
			Configuration config = context.getConfiguration();
			results = config
					.getStrings("ClassificationAlgorithm.nativeBayes.results");
		}

		@Override
		protected void map(BayesData key, Text value,
				Mapper<BayesData, Text, BayesData, Text>.Context context)
				throws IOException, InterruptedException {
			if (key.getPrior().toString().equals(BayesData.PRIOR_DATA)) {
				for (String result : results) {
					key.setPrior(result);
					context.write(key, value);
				}
			}
		}

	}

	private static class JudgeReducer extends
			Reducer<BayesData, Text, Text, BayesJudge> {

		@Override
		protected void reduce(BayesData key, Iterable<Text> values,
				Reducer<BayesData, Text, Text, BayesJudge>.Context context)
				throws IOException, InterruptedException {
			Text prior = key.getPrior();
			double probability = 0d;
			List<String> ids = new ArrayList<String>();
			for (Text value : values) {
				String line = value.toString();
				if (line.contains("prob:")) {
					probability = Double.parseDouble(line.substring(5));
				} else {
					ids.add(line);
				}
			}
			BayesJudge judge = new BayesJudge();
			for (String id : ids) {
				judge.setId(id);
				judge.setProbability(probability);
				context.write(prior, judge);
			}
		}

	}
}
