package org.knime.knip.clump.dist;

import java.util.Arrays;

import net.imglib2.img.Img;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.knime.knip.clump.util.ClumpUtils;

/**
 * 
 * @author Udo Schlegel
 *
 */
public class MinDistance<T extends RealType<T> & NativeType<T>> 
	implements ShapeDistance<T>{

	private final DistanceMeasure m_dist;
	
	public MinDistance(DistanceMeasure dist){
		m_dist = dist;
	}
	
	@Override
	public BinaryOperation<Img<T>, Img<T>, T> copy() {
		return new MinDistance<T>(m_dist);
	}

	@Override
	public T compute(Img<T> inputA, Img<T> inputB, T output) {
		
		double[] arrayA = ClumpUtils.toDoubleArray( inputA );
		double[] arrayB = ClumpUtils.toDoubleArray( inputB );
		
		
		
//		out.setReal( 1.0d / Math.min(arrayA.length, arrayB.length) );
		
		if( arrayA.length == arrayB.length ){
			output.setReal( m_dist.compute(arrayA, arrayB) );
			return output;
		}
		
		double min = Double.MAX_VALUE;
		for(int i = 0; i < arrayA.length - arrayB.length; i++){
			double res = 
					dist(Arrays.copyOfRange(arrayA, i, arrayB.length  + i), arrayB);
//			res /= (double)arrayB.length;
			if ( res < min )
				min = res;
		}
//		if( arrayA.length > arrayB.length ){
//			for(int i = 0; i < arrayA.length - arrayB.length; i++){
//				double res = 
//						m_dist.compute(Arrays.copyOfRange(arrayA, i, arrayB.length  + i), arrayB);
//				if ( res < min )
//					min = res;
//			}
//		}else {
//			for(int i = 0; i < arrayB.length - arrayA.length; i++){
//				double res = 
//						m_dist.compute(Arrays.copyOfRange(arrayB, i, arrayA.length  + i), arrayA);
//				if ( res < min )
//					min = res;
//			}
//		}
		output.setReal( min );
		return output;
	}

	@Override
	public DistanceMeasure getDistanceMeasure() {
		return m_dist;
	}
	
	private double dist(double[] arg0, double[] arg1){
		assert arg0.length == arg1.length;
		double out = 0.0d;
		for( int i = 0; i < arg0.length; i++){
			out += ( arg0[i] - arg1[i]) * ( arg0[i] - arg1[i]);
		}
		return Math.sqrt( out );
	}

}
