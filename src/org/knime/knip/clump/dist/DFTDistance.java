package org.knime.knip.clump.dist;

import net.imglib2.img.Img;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.knime.knip.clump.boundary.ShapeDescription;
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
	public BinaryOperation<ShapeDescription<T>, Img<T>, T> copy() {
		return new DFTDistance<T>(m_dist, m_numberOfDesc, m_usedDesc);
	}

	@Override
	public DistanceMeasure getDistanceMeasure() {
		return m_dist;
	}

	@Override
	public T compute(ShapeDescription<T> inputA, Img<T> inputB, T output) {
		UnaryOperation<Img<T>, Complex[]> op = new FourierShapeDescription<T>(m_numberOfDesc);
		BinaryOperation<Complex, Complex, Double> complexDist = new ComplexDistance(m_dist);
		Complex[] fc0 = op.compute(inputA.getImg(), new Complex[m_numberOfDesc]);
		Complex[] fc1 = op.compute(inputB, new Complex[m_numberOfDesc]);
		double res = 0.0d;
		for(int i = 0; i < m_usedDesc; i++){
			res += complexDist.compute(fc0[i], fc1[i], new Double(0.0d));
		}
		
		output.setReal(res /= m_usedDesc);
		return output;
	}

}
