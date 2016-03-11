package org.martial.math.recommend;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.mahout.cf.taste.hadoop.RecommendedItemsWritable;
import org.apache.mahout.cf.taste.hadoop.item.VectorAndPrefsWritable;
import org.apache.mahout.cf.taste.hadoop.item.VectorOrPrefWritable;
import org.apache.mahout.math.VarLongWritable;
import org.apache.mahout.math.VectorWritable;
import org.martial.math.utils.JobUtils;
import org.martial.math.utils.MathConfig;

/**
 * 推荐服务简单实现
 * 
 * @author ming
 *
 */
public class Recommends {
	private MathConfig mconfig = new MathConfig();

	public void recommend(Configuration config, String inputPath,
			String outputPath, String userVectorOutPath, String coOutputPath,
			String recommondUser, String recommondItem) throws IOException,
			ClassNotFoundException, InterruptedException {
		String recommondInput = mconfig.value("recommondVectors");

		// 分割用户向量
		Job baseUserVector = getUserVector(config, inputPath, userVectorOutPath);
		ControlledJob job1 = new ControlledJob(config);
		job1.setJob(baseUserVector);
		// 物品共现矩阵
		Job cooccurrence = getCooccurrence(config, userVectorOutPath,
				coOutputPath);
		ControlledJob job2 = new ControlledJob(config);
		job2.setJob(cooccurrence);
		job2.addDependingJob(job1);
		// 收集以上两步的向量
		Job userVector = collectUserVector(config, userVectorOutPath,
				recommondUser);
		ControlledJob job3 = new ControlledJob(config);
		job3.setJob(userVector);
		job3.addDependingJob(job1);

		Job ItemVector = collectItemVector(config, coOutputPath, recommondItem);
		ControlledJob job4 = new ControlledJob(config);
		job4.setJob(ItemVector);
		job4.addDependingJob(job2);

		Job allVector = recommondInput(config, recommondUser, recommondItem,
				recommondInput);
		ControlledJob job5 = new ControlledJob(config);
		job5.setJob(allVector);
		job5.addDependingJob(job3);
		job5.addDependingJob(job4);

		// 最后完成推荐
		Job simpleRecommend = Job.getInstance(config, "simpleRecommend");
		FileInputFormat.addInputPath(simpleRecommend, new Path(recommondInput));
		Path out = JobUtils.reSetOutput(outputPath, config);
		FileOutputFormat.setOutputPath(simpleRecommend, out);
		simpleRecommend.setJar(mconfig.value("jar"));
		simpleRecommend.setJarByClass(SimpleRecommend.class);
		simpleRecommend.setMapperClass(SimpleRecommend.RecommedMapper.class);
		simpleRecommend
				.setCombinerClass(SimpleRecommend.RecommedCombiner.class);
		simpleRecommend.setReducerClass(SimpleRecommend.RecommedReducer.class);
		simpleRecommend.setMapOutputKeyClass(VarLongWritable.class);
		simpleRecommend.setMapOutputValueClass(VectorWritable.class);
		simpleRecommend.setOutputKeyClass(VarLongWritable.class);
		simpleRecommend.setOutputValueClass(RecommendedItemsWritable.class);
		simpleRecommend.setInputFormatClass(SequenceFileInputFormat.class);
		ControlledJob job6 = new ControlledJob(config);
		job6.setJob(simpleRecommend);
		job6.addDependingJob(job5);

		JobControl jcontrol = new JobControl("recommond");
		jcontrol.addJob(job1);
		jcontrol.addJob(job2);
		jcontrol.addJob(job3);
		jcontrol.addJob(job4);
		jcontrol.addJob(job5);
		jcontrol.addJob(job6);

		Thread t = new Thread(jcontrol);
		t.start();
		// 这个等待线程执行完毕的方式好像不太正规的样子
		while (!jcontrol.allFinished()) {
			System.out.println("任务还在执行中，请稍后。。。");
			Thread.sleep(10_000);
		}
		jcontrol.stop();
		System.out.println("任务完成！");
	}

	public Job collectUserVector(Configuration config, String inputPath,
			String outputPath) throws IOException, ClassNotFoundException,
			InterruptedException {
		Job job = Job.getInstance(config, "user-collect");
		FileInputFormat.addInputPath(job, new Path(inputPath));
		Path out = JobUtils.reSetOutput(outputPath, config);
		FileOutputFormat.setOutputPath(job, out);
		job.setJarByClass(CollectorVectors.class);
		job.setJar(mconfig.value("jar"));
		job.setMapperClass(CollectorVectors.UserVectorSplitter.class);
		job.setReducerClass(CollectorVectors.NoneDoReducer.class);
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(VectorOrPrefWritable.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(VectorOrPrefWritable.class);
		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		return job;

	}

	public Job collectItemVector(Configuration config, String inputPath,
			String outputPath) throws IOException, ClassNotFoundException,
			InterruptedException {
		Job job = Job.getInstance(config, "item-collect");
		FileInputFormat.addInputPath(job, new Path(inputPath));
		Path out = JobUtils.reSetOutput(outputPath, config);
		FileOutputFormat.setOutputPath(job, out);
		job.setJarByClass(CollectorVectors.class);
		job.setJar(mconfig.value("jar"));
		job.setMapperClass(CollectorVectors.CoWrapper.class);
		job.setReducerClass(CollectorVectors.NoneDoReducer.class);
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(VectorOrPrefWritable.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(VectorOrPrefWritable.class);
		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		return job;
	}

	public Job getUserVector(Configuration config, String inputPath,
			String outputPath) throws IOException, ClassNotFoundException,
			InterruptedException {
		Job job = Job.getInstance(config, "user-vector");
		FileInputFormat.addInputPath(job, new Path(inputPath));
		Path out = JobUtils.reSetOutput(outputPath, config);
		FileOutputFormat.setOutputPath(job, out);
		job.setJarByClass(UserVectors.class);
		job.setJar(mconfig.value("jar"));
		job.setMapperClass(UserVectors.UserVectorMapper.class);
		job.setReducerClass(UserVectors.UserVectorReducer.class);
		job.setMapOutputKeyClass(VarLongWritable.class);
		job.setMapOutputValueClass(VarLongWritable.class);
		job.setOutputKeyClass(VarLongWritable.class);
		job.setOutputValueClass(VectorWritable.class);
		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		return job;
	}

	public Job getCooccurrence(Configuration config, String inputPath,
			String outputPath) throws IOException, ClassNotFoundException,
			InterruptedException {
		Job job = Job.getInstance(config, "item-cooccurrence");
		FileInputFormat.addInputPath(job, new Path(inputPath));
		Path out = JobUtils.reSetOutput(outputPath, config);
		FileOutputFormat.setOutputPath(job, out);
		job.setJarByClass(Cooccurrence.class);
		job.setJar(mconfig.value("jar"));
		job.setMapperClass(Cooccurrence.CoMapper.class);
		job.setReducerClass(Cooccurrence.coReducer.class);
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(IntWritable.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(VectorWritable.class);
		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		return job;
	}

	public Job recommondInput(Configuration config, String userInputPath,
			String itemInputPath, String outputPath) throws IOException,
			ClassNotFoundException, InterruptedException {
		Job job = Job.getInstance(config, "recommond-input");
		FileInputFormat.addInputPath(job, new Path(userInputPath));
		FileInputFormat.addInputPath(job, new Path(itemInputPath));
		Path out = JobUtils.reSetOutput(outputPath, config);
		FileOutputFormat.setOutputPath(job, out);
		job.setJarByClass(CollectorVectors.class);
		job.setJar(mconfig.value("jar"));
		job.setMapperClass(CollectorVectors.NoneDoMapper.class);
		job.setReducerClass(CollectorVectors.CollectReducer.class);
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(VectorOrPrefWritable.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(VectorAndPrefsWritable.class);
		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		return job;
	}

}
