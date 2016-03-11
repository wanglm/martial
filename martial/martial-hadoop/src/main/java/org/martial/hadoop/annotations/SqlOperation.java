package org.martial.hadoop.annotations;

import org.martial.hadoop.enums.SQLTypes;

public @interface SqlOperation {
	SQLTypes value() default SQLTypes.SELECT;
}
