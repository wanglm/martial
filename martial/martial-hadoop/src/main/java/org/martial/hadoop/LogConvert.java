package org.martial.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;
import org.martial.math.randomforest.Convertors;

public class LogConvert {

	public static void main(String... args) {
		try {
			Configuration config = HadoopConfig.INSRANCE.getConfiguration();
			Convertors convertor = new Convertors();
			String input="data/log";
			String output="randomForest/train/input";
			Job job = convertor.convert(config,input,output);
			job.submit();
		} catch (IOException | ClassNotFoundException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
