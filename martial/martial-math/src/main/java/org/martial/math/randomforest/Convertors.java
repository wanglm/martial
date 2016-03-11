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
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.martial.math.utils.JobUtils;
import org.martial.math.utils.MathConfig;

public class Convertors {

	public Job convert(Configuration config, String input, String output)
			throws IOException {
		Job job = Job.getInstance(config, "randomForest-convert");
		Path out = JobUtils.reSetOutput(output, config);
		FileInputFormat.addInputPath(job, new Path(input));
		FileOutputFormat.setOutputPath(job, out);
		job.setJarByClass(Convertors.class);
		String jar = new MathConfig().value("jar");
		job.setJar(jar);
		job.setMapperClass(ConvertMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setReducerClass(ConvertReducer.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		return job;
	}

	public static class ConvertMapper extends Mapper<Object, Text, Text, Text> {
		private static List<String> list;
		private final int BEGIN = 3;
		private final int END = 20;
		private Random random = new Random(3);
		static {
			list = new ArrayList<String>(3);
			list.add("rich");
			list.add("pool");
			list.add("unSure");
		}

		@Override
		protected void map(Object key, Text value,
				Mapper<Object, Text, Text, Text>.Context context)
				throws IOException, InterruptedException {
			int n = random.nextInt(3);
			n = n < 0 & n > 2 ? 2 : n;
			int no = random.nextInt(100);
			String keyStr = list.get(n) + "_" + String.valueOf(no);
			Text result = new Text(keyStr);
			String[] line = value.toString().split(",");
			StringBuilder sb = new StringBuilder();
			for (int i = BEGIN; i <= END; i++) {
				if (i != 14) {
					sb.append(line[i]);
					if (i != END) {
						sb.append(",");
					}
				}
			}
			value.clear();
			value.set(sb.toString());
			context.write(result, value);
		}

	}

	public static class ConvertReducer extends
			Reducer<Text, Text, IntWritable, Text> {

		@Override
		protected void reduce(Text key, Iterable<Text> values,
				Reducer<Text, Text, IntWritable, Text>.Context context)
				throws IOException, InterruptedException {
			// 生产环境下需要考虑数据偏移的问题，key数量太少造成reduce负担过重，因此需要分散key值。
			// 在这种情况下需要重新切割处理分散的key值使其最后输出正确。
			String keyStrs[] = key.toString().split("_");
			IntWritable resultKey = new IntWritable();
			String ks = keyStrs[0];
			if (ks.equals("rich")) {
				resultKey.set(0);
			} else if (ks.equals("pool")) {
				resultKey.set(1);
			} else {
				resultKey.set(2);
			}
			for (Text value : values) {
				context.write(resultKey, value);
			}
		}

	}

}
