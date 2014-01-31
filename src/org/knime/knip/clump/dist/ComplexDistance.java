package org.knime.knip.clump.dist;

import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.data.algebra.Complex;

/**
 * 
 * @author Udo
 *
 */
public class ComplexDistance<T extends RealType<T>> 
	implements BinaryOperation<Complex[], Complex[], T>{

	private final T m_type;
	
	public ComplexDistance(T type){
		m_type = type.createVariable();
	}

	@Override
	public T compute(Complex[] arg0, Complex[] arg1, T out) {
		assert arg0.length == arg1.length;
		
		double res = 0.0d;
		
		for( int i = 0; i < arg0.length; i++){
			res += dist(
					new double[]{arg0[i].re(), arg0[i].im()}, 
					new double[]{arg1[i].re(), arg1[i].im()});
		}
		out.setReal(res / arg0.length);
		return out;
	}

	@Override
	public BinaryOperation<Complex[], Complex[], T> copy() {
		return new ComplexDistance<T>(m_type);
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
