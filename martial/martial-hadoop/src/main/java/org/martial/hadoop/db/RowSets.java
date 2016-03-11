package org.martial.hadoop.db;

import java.sql.ResultSet;

public interface RowSets {

	public <T> T setValue(ResultSet rs);

}
