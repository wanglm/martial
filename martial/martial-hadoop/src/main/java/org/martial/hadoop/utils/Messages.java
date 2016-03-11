package org.martial.hadoop.utils;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.martial.hadoop.JobConfig;



public class Messages implements AutoCloseable {
	private String url;
	private List<HttpURLConnection> list;

	public Messages() {
		this.url = "http://219.234.85.196";
	}

	public Messages(String url) {
		this.url = url;
	}

	public void sentMsg(String msg) throws IOException {
		String numbers = JobConfig.getValue("admin.phone.number");
		String[] nos = numbers.split(",");
		List<HttpURLConnection> list = new ArrayList<HttpURLConnection>(
				nos.length);
		String msgUrl = URLEncoder.encode(msg, "UTF-8");
		for (String no : nos) {
			StringBuilder sb = new StringBuilder();
			sb.append(url);
			sb.append("/sendcontent?mobile=");
			sb.append(no);
			sb.append("&content=");
			sb.append(msgUrl);
			sb.append("【万普世纪】");
			URL url = new URL(sb.toString());
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.connect();
			int code = con.getResponseCode();
			if (code != HttpURLConnection.HTTP_OK) {
				throw new ConnectException("短信发送不成功，代号：" + code);
			}
			list.add(con);
		}
	}

	@Override
	public void close() throws Exception {
		if (list != null) {
			for (HttpURLConnection con : list) {
				con.disconnect();
			}
		}
	}

}
