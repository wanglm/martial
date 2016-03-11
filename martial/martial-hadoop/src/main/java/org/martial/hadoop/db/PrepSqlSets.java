package org.martial.hadoop.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PrepSqlSets {
	
	public PreparedStatement setSql(PreparedStatement ps)throws SQLException;

}
