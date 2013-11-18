package org.knime.knip.ops;

import java.util.LinkedList;
import java.util.List;

import net.imglib2.type.numeric.real.DoubleType;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.junit.Test;
import org.knime.knip.clump.ops.GenericDynamicTimeWarping;

public class DTWTest {
	
	@Test
	public void testDTW(){
		
		double[] a1 = new double[]{ 1,	1,	2,	3,	2,	0};
		double[] a2 = new double[]{ 0,	1,	1,	2,	3,	2,	1};
		
		List<DoubleType> l1 = new LinkedList<DoubleType>();
		List<DoubleType> l2 = new LinkedList<DoubleType>();
		
		for(double d: a1)
			l1.add( new DoubleType( d ));
		
		for(double d: a2)
			l2.add(new DoubleType( d ));
		
		DoubleType d = new GenericDynamicTimeWarping<DoubleType>(new EuclideanDistance()).compute(
				l1, 
				l2, 
				new DoubleType());
		
		System.out.print(d);
	}

}
