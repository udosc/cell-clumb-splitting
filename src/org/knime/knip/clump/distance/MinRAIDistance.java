package org.knime.knip.clump.distance;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccess;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.knime.knip.base.exceptions.KNIPRuntimeException;
import org.knime.knip.clump.util.MyUtils;

/**
 * 
 * @author Udo Schlegel
 *
 */
public class MinRAIDistance<T extends RealType<T> & NativeType<T>> 
	implements BinaryOperation<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, T>{

	private int m_step;
	
	public MinRAIDistance(int step){
		m_step = step;
	}
	
	@Override
	public BinaryOperation<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, T> copy() {
		return new MinRAIDistance<T>(m_step);
	}

	@Override
	public T compute(RandomAccessibleInterval<T> inputA, RandomAccessibleInterval<T> inputB, T output) {
		final long sizeA = inputA.dimension(0);
		
		final int sizeB = MyUtils.numElements( inputB );
		
		double min = Double.MAX_VALUE;
		
//		final  T mean = 
//				new Mean<T, T>().compute(inputB.iterator(), inputB.firstElement().createVariable());
//		
//		Cursor<T> cursor = inputB.cursor();
//		while( cursor.hasNext() ){
//			cursor.next().sub( mean );
//		}
		
//		System.out.println( new Sum<T, T>().compute(inputB.iterator(), inputB.firstElement().createVariable().createVariable() ));
		
		if( sizeA <= sizeB ){
			RealRandomAccess<T> rra = Views.interpolate( inputB,
					new NLinearInterpolatorFactory<T>()).realRandomAccess();
			
			final double step = (inputA.dimension(0) - 1.0d)/ 
					(double) sizeB ;
			
			final double[] res = new double[ sizeB ];
			//Working with 1-dimensional data so fixing dim 1 to 0
//			rra.setPosition(0, 1);
			for(int i = 0; i < sizeB; i++){
				rra.setPosition((i*step), 0);
				res[i] = rra.get().getRealDouble();
			}
			
			for(int i = 0; i < sizeA ; i+=1){
				double dist = dist(
						MyUtils.toDoubleArray( Views.interval( Views.extendPeriodic( inputA ), new long[]{ i }, new long[]{ i + sizeB -1 }) ), 
//						MyUtils.toDoubleArray( inputB ));
						res);
				
				if ( dist < min )
					min = dist;
			}
						
//			min = dist(res, MyUtils.toDoubleArray(inputB));
		} else {
			for(int i = 0; i < sizeA ; i+=1){
//				if ( sA ==  96)
//					System.out.println();
				double res = dist(
						MyUtils.toDoubleArray( Views.interval( Views.extendPeriodic( inputA ), new long[]{ i }, new long[]{ i + sizeB -1 }) ), 
						MyUtils.toDoubleArray( inputB ));
				
				if ( res < min )
					min = res;
			}
		}

//		double[] arrayA = MyUtils.toDoubleArray( inputA );
//		double[] arrayB = MyUtils.toDoubleArray( inputB );

//		out.setReal( 1.0d / Math.min(arrayA.length, arrayB.length) );
		
//		if( arrayA.length == arrayB.length ){
//			//output.setReal( m_dist.compute(arrayA, arrayB) );
//			output.setReal( dist(arrayA, arrayB) );
//			return output;
//		}
//		
//		double min = Double.MAX_VALUE;
//		for(int i = 0; i < arrayA.length - arrayB.length; i++){
//			double res = 
//					dist(Arrays.copyOfRange(arrayA, i, arrayB.length  + i), arrayB);
////			res /= (double)arrayB.length;
//			if ( res < min )
//				min = res;
//		}
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

	
	private double dist(double[] arg0, double[] arg1){
		if ( arg0.length != arg1.length )
			throw new KNIPRuntimeException(
				this.getClass().getCanonicalName() + ": " + arg0.length + " != " + arg1.length);
		double out = 0.0d;
		for( int i = 0; i < arg0.length; i++){
			out += ( arg0[i] - arg1[i]) * ( arg0[i] - arg1[i]);
		}
		return Math.sqrt( out );
	}

}
