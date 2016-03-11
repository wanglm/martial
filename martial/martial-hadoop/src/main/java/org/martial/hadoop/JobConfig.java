package org.martial.hadoop;

import java.io.IOException;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JobConfig {
	private static Logger LOG = LogManager.getLogger(JobConfig.class);

	public static Properties getProperties() {
		Properties prop = null;
		try {
			prop = new Properties();
			prop.load(JobConfig.class.getResourceAsStream("/jobs.properties"));
		} catch (IOException e) {
			LOG.error("读取job配置文件出错：", e);
		}
		return prop;
	}

	public static String getValue(String key) {
		return getProperties().getProperty(key);
	}

	public static Configuration getHadoopConfig() {
		Configuration conf = new Configuration();
		conf.addResource("core-site.xml");
		conf.addResource("hdfs-site.xml");
		conf.addResource("mapred-site.xml");
		conf.addResource("yarn-site.xml");
		return conf;
	}

}
