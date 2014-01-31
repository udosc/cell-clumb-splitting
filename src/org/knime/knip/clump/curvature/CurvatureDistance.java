package org.knime.knip.clump.curvature;

import java.util.concurrent.ExecutorService;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccess;
import net.imglib2.img.Img;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.outofbounds.OutOfBoundsPeriodicFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.knime.core.node.KNIMEConstants;
import org.knime.knip.base.exceptions.KNIPRuntimeException;
import org.knime.knip.clump.contour.Contour;
import org.knime.knip.clump.dist.ContourDistance;
import org.knime.knip.clump.dist.MinRAIDistance;
import org.knime.knip.clump.util.MyUtils;
import org.knime.knip.core.ops.filters.GaussNativeTypeOp;
import org.knime.knip.core.util.ImgUtils;

/**
 * 
 * @author Udo Schlegel
 *
 */
public class CurvatureDistance<T extends RealType<T> & NativeType<T>> 
	implements ContourDistance<T>{

	private int m_step;
	
	private CurvatureFactory<T> m_factory;
	
	private ExecutorService m_exec;
	
	private double m_sigma;
	
	public CurvatureDistance(CurvatureFactory<T> factory, int step, ExecutorService exec, double sigma){
		m_step = step;
		m_factory = factory;
		m_exec = exec;
		m_sigma = sigma;
	}
	
	@Override
	public BinaryOperation<Contour, Contour, T> copy() {
		return new CurvatureDistance<T>(m_factory, m_step, m_exec, m_sigma);
	}

	@Override
	public T compute(Contour contourA, Contour contourB, T output) {
		
		Img<T> res = m_factory.createCurvatureImg(contourA);
		RandomAccessibleInterval<T> inputA = new GaussNativeTypeOp<T, RandomAccessibleInterval<T>>(m_exec, 
				new double[]{ m_sigma, 0.0d}, 
				new OutOfBoundsPeriodicFactory<T, RandomAccessibleInterval<T>>())
				.compute(res, ImgUtils.createEmptyCopy( res ));
		
		res = m_factory.createCurvatureImg(contourB);
		RandomAccessibleInterval<T> inputB = new GaussNativeTypeOp<T, RandomAccessibleInterval<T>>(m_exec, 
				new double[]{ m_sigma, 0.0d}, 
				new OutOfBoundsPeriodicFactory<T, RandomAccessibleInterval<T>>())
				.compute(res, ImgUtils.createEmptyCopy( res ));
		
//		long sizeA = Math.abs( inputA.dimension(0) );
//		long sizeB = Math.abs( inputB.dimension(0) );
//		
//		double min = Double.MAX_VALUE;
		
		return new MinRAIDistance<T>(1).compute(inputA, inputB, output);
		
//		final  T mean = 
//				new Mean<T, T>().compute(inputB.iterator(), inputB.firstElement().createVariable());
//		
//		Cursor<T> cursor = inputB.cursor();
//		while( cursor.hasNext() ){
//			cursor.next().sub( mean );
//		}
		
//		System.out.println( new Sum<T, T>().compute(inputB.iterator(), inputB.firstElement().createVariable().createVariable() ));
		
//		if( sizeA <= inputB.dimension(0) ){
//									
//			RealRandomAccess<T> rra = Views.interpolate( inputA,
//					new NLinearInterpolatorFactory<T>()).realRandomAccess();
//			
//			final double step = (sizeA - 1.0d)/ 
//					(double) sizeB ;
//			
//			final double[] res = new double[ (int)sizeA ];
//			//Working with 1-dimensional data so fixing dim 1 to 0
////			rra.setPosition(0, 1);
//			for(int i = 0; i < sizeA; i++){
//				rra.setPosition((i*step), 0);
//				res[i] = rra.get().getRealDouble();
//			}
//			
//			for(int i = 0; i < sizeA ; i+=1){
//				double dist = dist(
//						MyUtils.toDoubleArray( Views.interval( Views.extendPeriodic( inputA ), new long[]{ i }, new long[]{ i + sizeB -1 }) ), 
//						MyUtils.toDoubleArray( inputB ));
//				
//				if ( dist < min )
//					min = dist;
//			}
//						
////			min = dist(res, MyUtils.toDoubleArray(inputB));
//		} else {
//			for(int i = 0; i < sizeA ; i+=1){
////				if ( sA ==  96)
////					System.out.println();
//				double res = dist(
//						MyUtils.toDoubleArray( Views.interval( Views.extendPeriodic( inputA ), new long[]{ i }, new long[]{ i + sizeB -1 }) ), 
//						MyUtils.toDoubleArray( inputB ));
//				
//				if ( res < min )
//					min = res;
//			}
//		}
//
////		double[] arrayA = MyUtils.toDoubleArray( inputA );
////		double[] arrayB = MyUtils.toDoubleArray( inputB );
//
////		out.setReal( 1.0d / Math.min(arrayA.length, arrayB.length) );
//		
////		if( arrayA.length == arrayB.length ){
////			//output.setReal( m_dist.compute(arrayA, arrayB) );
////			output.setReal( dist(arrayA, arrayB) );
////			return output;
////		}
////		
////		double min = Double.MAX_VALUE;
////		for(int i = 0; i < arrayA.length - arrayB.length; i++){
////			double res = 
////					dist(Arrays.copyOfRange(arrayA, i, arrayB.length  + i), arrayB);
//////			res /= (double)arrayB.length;
////			if ( res < min )
////				min = res;
////		}
////		if( arrayA.length > arrayB.length ){
////			for(int i = 0; i < arrayA.length - arrayB.length; i++){
////				double res = 
////						m_dist.compute(Arrays.copyOfRange(arrayA, i, arrayB.length  + i), arrayB);
////				if ( res < min )
////					min = res;
////			}
////		}else {
////			for(int i = 0; i < arrayB.length - arrayA.length; i++){
////				double res = 
////						m_dist.compute(Arrays.copyOfRange(arrayB, i, arrayA.length  + i), arrayA);
////				if ( res < min )
////					min = res;
////			}
////		}
//		output.setReal( min );
//		return output;
	}

	
	@Override
	public T getType() {
		return m_factory.getType();
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
