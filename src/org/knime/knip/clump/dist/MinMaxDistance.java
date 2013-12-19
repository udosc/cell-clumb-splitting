package org.knime.knip.clump.dist;

import net.imglib2.Cursor;
import net.imglib2.RealRandomAccess;
import net.imglib2.img.Img;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.ops.operation.iterable.unary.Mean;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.knime.knip.base.exceptions.KNIPRuntimeException;
import org.knime.knip.clump.boundary.ShapeDescription;
import org.knime.knip.clump.contour.Contour;
import org.knime.knip.clump.util.MyUtils;

public class MinMaxDistance<T extends RealType<T> & NativeType<T>> 
	implements ShapeDistance<T>{
	
	private final DistanceMeasure m_dist;
	
	public MinMaxDistance(DistanceMeasure dist){
		m_dist = dist;
	}
	
	@Override
	public BinaryOperation<ShapeDescription<T>, Img<T>, T> copy() {
		return new MinMaxDistance<T>(m_dist);
	}

	@Override
	public T compute(ShapeDescription<T> inputA, Img<T> inputB, T output) {
		Contour cA = inputA.getContour();
		
		final int size = MyUtils.numElements( inputB );
		
		double min = Double.MAX_VALUE;
		
		final  T mean = 
				new Mean<T, T>().compute(inputB.iterator(), inputB.firstElement().createVariable());
		
		Cursor<T> cursor = inputB.cursor();
		while( cursor.hasNext() ){
			cursor.next().sub( mean );
		}
		
//		System.out.println( new Sum<T, T>().compute(inputB.iterator(), inputB.firstElement().createVariable().createVariable() ));
		
		if( cA.length() <= size ){
			RealRandomAccess<T> rra = Views.interpolate( inputA.getImg(),
					new NLinearInterpolatorFactory<T>()).realRandomAccess();
			
			final double step = (inputA.getSize() - 1.0d)/ 
					(double) size ;
			
			final double[] res = new double[ size ];
			//Working with 1-dimensional data so fixing dim 1 to 0
//			rra.setPosition(0, 1);
			for(int i = 0; i < size; i++){
				rra.setPosition((i*step), 0);
				res[i] = rra.get().getRealDouble();
			}
			min = dist(res, MyUtils.toDoubleArray(inputB));
		} else {
			for(int i = 0; i < cA.length() - size ; i+=2){
				double res = dist(
						inputA.getValues(i, i + size -1), 
						MyUtils.toDoubleArray( inputB ));
				
				if ( res < min )
					min = res;
			}
		}
		
		output.setReal( min );
		return output;
	}

	@Override
	public DistanceMeasure getDistanceMeasure() {
		return m_dist;
	}
	
	private double dist(Img<T> arg0, Img<T> arg1){
		return dist( MyUtils.toDoubleArray(arg0),
				MyUtils.toDoubleArray(arg1));
		
	}
	
	private double dist(double[] arg0, double[] arg1){
		if ( arg0.length != arg1.length )
			throw new KNIPRuntimeException(
				this.getClass().getCanonicalName() + ": " + arg0.length + " != " + arg1.length);
		double out = 0.0d;
		for( int i = 0; i < arg0.length; i++){
			double res = ( arg0[i] - arg1[i]) * ( arg0[i] - arg1[i]);
			if ( res > out )
				out = res;
		}
		return out;
	}

}