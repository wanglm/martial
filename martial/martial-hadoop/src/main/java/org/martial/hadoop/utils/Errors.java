package org.martial.hadoop.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Errors {
	private static Logger LOG = LogManager.getLogger(Errors.class);

	public static void error(String message, Throwable t) {
		try (Messages msg = new Messages()) {
			LOG.error(message, t);
			msg.sentMsg(message);
		} catch (Exception e) {
			LOG.error("短信发送出错：", e);
		}
	}

	public static void error(String message) {
		try (Messages msg = new Messages()) {
			LOG.error(message);
			msg.sentMsg(message);
		} catch (Exception e) {
			LOG.error("短信发送出错：", e);
		}
	}

}
