package org.knime.knip.clump.distance;

import org.knime.knip.clump.util.MyUtils;
import org.knime.knip.core.data.algebra.Complex;

import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.numeric.RealType;

public class ComplexDistance<T extends RealType<T>> 
	implements BinaryOperation<Complex[], Complex[], T> {

	
	@Override
	public T compute(Complex[] arg0, Complex[] arg1, T arg2) {
		assert arg0.length == arg1.length;
		
		if( arg0.length != arg1.length){
			throw new IllegalAccessError("The length of the arrays are different");
		}
		
		double res = 0.0d;
		for(int i = 0; i < arg0.length; i++){
			res += Math.sqrt( Math.pow(arg0[i].re() - arg1[i].re(), 2) + 
					Math.pow(arg0[i].im() - arg1[i].im(), 2));
		}
		
		arg2.setReal( res );
		return arg2;
	}

	@Override
	public BinaryOperation<Complex[], Complex[], T> copy() {
		return new ComplexDistance<T>();
	}

}
