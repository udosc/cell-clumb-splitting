package org.knime.knip.clump.ops;

import java.util.Arrays;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.numeric.RealType;

import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.knime.knip.clump.util.ClumpUtils;

/**
 * 
 * @author Udo Schlegel
 *
 */
public class MinDistance<T extends RealType<T>> 
	implements BinaryOperation<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, T>{

	private final DistanceMeasure m_dist;
	
	public MinDistance(DistanceMeasure dist){
		m_dist = dist;
	}
	
	@Override
	public T compute(RandomAccessibleInterval<T> inputA, RandomAccessibleInterval<T> inputB, T out){
		
		double[] arrayA = ClumpUtils.toDoubleArray( inputA );
		double[] arrayB = ClumpUtils.toDoubleArray( inputB );
		
		out.setReal( 1.0d / Math.min(arrayA.length, arrayB.length) );
		
		if( arrayA.length == arrayB.length ){
			out.mul( m_dist.compute(arrayA, arrayB) );
			return out;
		}
		
		double min = Double.MAX_VALUE;
		if( arrayA.length > arrayB.length ){
			for(int i = 0; i < arrayA.length - arrayB.length; i++){
				double res = 
						m_dist.compute(Arrays.copyOfRange(arrayA, i, arrayB.length  + i), arrayB);
				if ( res < min )
					min = res;
			}
		}else {
			for(int i = 0; i < arrayB.length - arrayA.length; i++){
				double res = 
						m_dist.compute(Arrays.copyOfRange(arrayB, i, arrayA.length  + i), arrayA);
				if ( res < min )
					min = res;
			}
		}
		out.mul( min );
		return out;
	}

	@Override
	public BinaryOperation<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, T> copy() {
		return new MinDistance<T>(m_dist);
	}

}
