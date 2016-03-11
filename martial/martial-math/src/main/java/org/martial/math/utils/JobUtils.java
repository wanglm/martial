package org.martial.math.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;

public class JobUtils {
	private static final Log LOG = LogFactory.getLog(JobUtils.class);

	public static MapWritable readSequenceFile(Configuration config, Path path,
			Writable key, Writable value) {
		MapWritable map = null;
		try (SequenceFile.Reader reader = new SequenceFile.Reader(config,
				Reader.file(path), Reader.bufferSize(8192), Reader.start(0));) {
			map = new MapWritable();
			while (reader.next(key, value)) {
				map.put(key, value);
			}
		} catch (Exception e) {
			LOG.error("读取sequencefile文件出错：", e);

		}
		return map;
	}

	public static String getDateString(Date date, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}

	public static String getToday() {
		return getDateString(new Date(), "yyyyMMdd");
	}

	public static String getToday(String pattern) {
		return getDateString(new Date(), pattern);
	}

	/**
	 * 重置输出目录
	 * 
	 * @param output
	 * @param config
	 * @throws IOException
	 */
	public static Path reSetOutput(String output, Configuration config)
			throws IOException {
		FileSystem fs = FileSystem.get(config);
		Path out = new Path(output);
		if (fs.exists(out)) {
			fs.delete(out, true);
		}
		return out;
	}

	/**
	 * 读取特征名文件，一行一个顺序对应
	 * 
	 * @param path
	 * @param fs
	 */
	public static List<String> readCache(String path, FileSystem fs, int size) {
		List<String> list = null;
		try (FSDataInputStream in = fs.open(new Path(path));
				BufferedReader fis = new BufferedReader(new InputStreamReader(
						in));) {
			String line = null;
			list = new ArrayList<String>(size);
			while ((line = fis.readLine()) != null) {
				list.add(line);
			}
		} catch (IOException ioe) {
			LOG.error("读取cache文件出错：    ", ioe);
		}
		return list;
	}

	public static int buildId(int... n) {
		StringBuilder sb = new StringBuilder();
		for (int b : n) {
			sb.append(b);
		}
		return Integer.parseInt(sb.toString());
	}

	/**
	 * 运行mapreduce任务
	 * 
	 * @param controller
	 * @param seconds
	 *            s 最大运行时间，单位是秒
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static boolean jobRun(JobControl controller, int seconds)
			throws InterruptedException, IOException {
		seconds = seconds == 0 ? 60 : seconds / 30;
		Thread t = new Thread(controller);
		t.start();
		boolean isRun = true;
		int n = 0;
		while (isRun) {
			if (controller.allFinished() || n >= seconds) {
				if (n >= seconds) {
					LOG.info("任务超时：" + (seconds / 2) + "分钟！");
				} else {
					LOG.info("任务完成!");
				}
				controller.stop();
				LOG.info("成功的任务:");
				for (ControlledJob successJob : controller
						.getSuccessfulJobList()) {
					LOG.info("id: " + successJob.getJobID() + "; name: "
							+ successJob.getJobName());
				}
				List<ControlledJob> failedList = controller.getFailedJobList();
				if (failedList.size() > 0) {
					LOG.error("失败的任务:");
					for (ControlledJob failedJob : failedList) {
						String msg = "Job name: " + failedJob.getJobName();
						LOG.error(msg);
						LOG.error("message:");
						LOG.error(failedJob.getMessage());
					}
				}
				for (ControlledJob runJob : controller.getRunningJobList()) {
					LOG.info("杀死运行中的任务...");
					runJob.killJob();
				}
				for (ControlledJob readyJob : controller.getReadyJobsList()) {
					LOG.info("杀死准备中的任务...");
					readyJob.killJob();
				}
				isRun = false;
			} else {
				LOG.info("任务运行中，已经运行" + 30 * n + "s...");
				Thread.sleep(30_000);
			}
			n++;
		}
		return !isRun;
	}
}
