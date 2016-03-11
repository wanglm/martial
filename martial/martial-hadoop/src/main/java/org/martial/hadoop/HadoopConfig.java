package org.martial.hadoop;

import org.apache.hadoop.conf.Configuration;

public enum HadoopConfig {
	INSRANCE;
	private Configuration conf;

	private HadoopConfig() {
		Configuration _conf = new Configuration();
		_conf.addResource("core-site.xml");
		_conf.addResource("hdfs-site.xml");
		_conf.addResource("mapred-site.xml");
		_conf.addResource("yarn-site.xml");
		this.conf = _conf;
	}

	public final Configuration getConfiguration() {
		return this.conf;
	}
}
