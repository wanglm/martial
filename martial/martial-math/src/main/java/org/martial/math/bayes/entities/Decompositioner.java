package org.martial.math.bayes.entities;

import java.util.List;

import org.apache.hadoop.io.Text;

/**
 * 分解数据
 * 
 * @author ming
 *
 */
public interface Decompositioner {
	public List<BayesEntity> execute(Text data);
	
	
	public int size();

}
