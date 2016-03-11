package org.martial.math.utils;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class MathConfig {
	private Properties prop;

	public MathConfig() {
		Properties _prop = new Properties();
		try {
			_prop.load(MathConfig.class
					.getResourceAsStream("/maths.properties"));
			this.prop = _prop;
		} catch (IOException e) {
			Logger log = LogManager.getLogger(MathConfig.class);
			log.error("MathConfig的配置文件读取io错误：", e);
		}
	}

	public final Properties getProperties() {
		return this.prop;
	}

	public String value(String key) {
		return this.prop.getProperty(key);
	}

}
