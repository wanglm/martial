package org.martial.hadoop.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public interface BaseDAO {
	public static final Logger LOG = LogManager.getLogger(BaseDAO.class);
	default <T> List<T> execute(String url, String user, String passWord,
			String sql, RowSets sets, PrepSqlSets prep) throws Exception {
		List<T> list = null;
		try (Connection con = DriverManager.getConnection(url, user, passWord);
				PreparedStatement ps = prep.setSql(con.prepareStatement(sql));
				ResultSet rs = ps.executeQuery();) {
			prep.setSql(ps);
			list = new ArrayList<T>();
			while (rs.next()) {
				list.add(sets.setValue(rs));
			}

		} catch (SQLException e) {
			throw e;
		}
		return list;
	}
}
