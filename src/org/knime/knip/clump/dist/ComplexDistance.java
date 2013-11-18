package org.knime.knip.clump.dist;

import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.knime.knip.core.data.algebra.Complex;

import net.imglib2.ops.operation.BinaryOperation;

/**
 * 
 * @author Udo
 *
 */
public class ComplexDistance 
	implements BinaryOperation<Complex, Complex, Double>{
	
	private DistanceMeasure m_dist;
	
	public ComplexDistance(DistanceMeasure dist){
		m_dist = dist;
	}

	@Override
	public Double compute(Complex arg0, Complex arg1, Double out) {
		out = m_dist.compute(
				new double[]{arg0.re(), arg0.im()}, 
				new double[]{arg1.re(), arg1.im()});
		return out;
	}

	@Override
	public BinaryOperation<Complex, Complex, Double> copy() {
		return new ComplexDistance(m_dist);
	}

}
