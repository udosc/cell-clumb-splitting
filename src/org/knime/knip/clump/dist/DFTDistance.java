package org.knime.knip.clump.dist;

import net.imglib2.img.Img;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.knime.knip.clump.ops.FourierShapeDescription;
import org.knime.knip.core.data.algebra.Complex;

/**
 * 
 * @author Udo
 *
 * @param <T>
 */
public class DFTDistance<T extends RealType<T> & NativeType<T>> 
	implements ShapeDistance<T>{
	
	private final DistanceMeasure m_dist;
	
	private final int m_numberOfDesc;
	
	private final int m_usedDesc;
	
	
	public DFTDistance(DistanceMeasure dist, int numberOfDesc, int usedDesc){
		m_dist = dist;
		m_numberOfDesc = numberOfDesc;
		m_usedDesc = usedDesc;
	}

	@Override
	public T compute(Img<T> arg0, Img<T> arg1, T arg2) {
		UnaryOperation<Img<T>, Complex[]> op = new FourierShapeDescription<T>(m_numberOfDesc);
		BinaryOperation<Complex, Complex, Double> dist = new ComplexDistance(m_dist);
		Complex[] fc0 = op.compute(arg0, new Complex[]{});
		Complex[] fc1 = op.compute(arg1, new Complex[]{});
		double res = 0.0d;
		for(int i = 0; i < m_usedDesc; i++){
			res += dist.compute(fc0[i], fc1[i], new Double(0.0d));
		}
		
		arg2.setReal(res /= m_usedDesc);
		return arg2;
	}

	@Override
	public BinaryOperation<Img<T>, Img<T>, T> copy() {
		return new DFTDistance<T>(m_dist, m_numberOfDesc, m_usedDesc);
	}

	@Override
	public DistanceMeasure getDistanceMeasure() {
		return m_dist;
	}

}
