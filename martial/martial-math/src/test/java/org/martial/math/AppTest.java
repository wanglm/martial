package org.martial.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.martial.math.randomforest.entities.RFData;
import org.martial.math.randomforest.entities.TargetIDs;
import org.martial.math.randomforest.entities.Trees;
import org.martial.math.randomforest.tree.ID3Tree;
import org.martial.math.randomforest.tree.TreeMaths;

public class AppTest {
	@Test
	public void test() {
		TreeMaths treeMath = new ID3Tree();
		List<RFData> datas = new ArrayList<RFData>(100);
		int[] names = { TargetIDs.ACT, TargetIDs.UDID, TargetIDs.IMSI,
				TargetIDs.IP, TargetIDs.NET, TargetIDs.SDK_VERSION,
				TargetIDs.VERSION, TargetIDs.APP_VERSION,
				TargetIDs.DEVICE_NAME, TargetIDs.CHANNEL };
		String data1 = "a,b,c,d,i,v,z,s,w,a";
		String data2 = "b,j,g,q,c,h,s,q,z,x";
		String data3 = "z,z,h,h,x,g,x,q,c,c";
		String data4 = "s,h,b,d,d,g,a,e,x,c";
		String data5 = "c,j,n,s,v,f,c,e,x,n";
		String data6 = "s,u,h,h,v,f,x,w,z,m";
		String data7 = "q,y,g,v,f,e,z,s,x,m";
		String data8 = "s,j,n,f,r,f,c,c,s,n";
		String data9 = "d,j,b,d,e,g,s,c,q,n";
		String data0 = "a,b,c,d,s,v,z,s,w,a";
		String data11 = "a,b,c,d,i,v,z,s,w,a";
		String data12 = "b,j,g,q,c,h,s,q,z,x";
		String data13 = "z,z,h,h,x,g,x,q,c,c";
		String data14 = "s,h,b,d,d,g,a,e,x,c";
		String data15 = "c,j,n,s,v,f,c,e,x,n";
		String data16 = "s,u,h,d,v,f,x,w,z,m";
		String data17 = "q,y,g,v,f,e,z,s,x,m";
		String data18 = "s,j,n,f,r,f,c,c,s,n";
		String data19 = "d,j,b,d,e,g,s,c,q,n";
		String data10 = "a,b,c,d,s,v,z,s,w,a";
		List<String> list = Arrays.asList(data1, data2, data3, data4, data5,
				data6, data7, data8, data9, data0, data11, data12, data13,
				data14, data15, data16, data17, data18, data19, data10);
		for (int i = 0; i < 20; i++) {
			String[] line = list.get(i).split(",");
			for (int j = 0; j < 10; j++) {
				RFData data = new RFData();
				data.setId(i);
				data.setName(names[j]);
				data.setResult(i % 3);
				data.setValue(line[j]);
				datas.add(data);
			}
		}
		// datas.forEach(System.out::println);
		Trees tree = treeMath.bulidTree(datas, 10, 10);
		System.out.println(tree.toString());
	}
}
