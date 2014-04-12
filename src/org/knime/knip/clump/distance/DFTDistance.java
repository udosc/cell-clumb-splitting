package org.knime.knip.clump.distance;

import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.algorithm.InplaceFFT;
import org.knime.knip.core.data.algebra.Complex;

/**
 * 
 * @author Udo
 *
 */
public class DFTDistance<T extends RealType<T>> 
	implements BinaryOperation<Complex[], Complex[], T>{

	private final T m_type;
	
	private final int m_numberOfDesc;
	
	public DFTDistance(int n, T type){
		m_type = type.createVariable();
		m_numberOfDesc = n;
	}

	@Override
	public T compute(Complex[] arg0, Complex[] arg1, T out) {
//		assert arg0.length == arg1.length;
		
//		final Complex[] transformed0 = InplaceFFT.fft( arg0 );
//		final Complex[] transformed1 = InplaceFFT.fft( arg1 );
		
		double res = 0.0d;
		
		for( int i = 0; i < m_numberOfDesc; i++){
//			res += dist(
////					new double[]{transformed0[i].re(), transformed0[i].im()}, 
////					new double[]{transformed1[i].re(), transformed1[i].im()});
//					new double[]{arg0[i].re(), arg1[i].im()}, 
//					new double[]{arg0[i].re(), arg1[i].im()});
			res += dist( arg0[i].getMagnitude(), arg1[i].getMagnitude());
		}
		out.setReal(res / arg0.length );
		return out;
	}

	@Override
	public BinaryOperation<Complex[], Complex[], T> copy() {
		return new DFTDistance<T>(m_numberOfDesc, m_type);
	}
	
	private double dist(double arg0, double arg1){
		return Math.sqrt( (arg0 - arg1) * (arg0 - arg1));
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
