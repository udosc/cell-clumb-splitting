package org.knime.knip.clump.ops;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ml.distance.DistanceMeasure;

import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.numeric.RealType;

/**
 * 
 * @author Schlegel
 *
 * @param <T>
 */
public class GenericDynamicTimeWarping<T extends RealType<T>> 
	implements BinaryOperation<List<T>, List<T>, T> {
	
	private final DistanceMeasure m_dist;
	
	public GenericDynamicTimeWarping(DistanceMeasure distance){
		m_dist = distance;
	}

	@Override
	public T compute(List<T> l1, List<T> l2,
			T output) {
	
		List<Double> list1 = new ArrayList<Double>( l1.size() );
		
		for(T res: l1)
			list1.add( res.getRealDouble() );
		
		List<Double> list2 = new ArrayList<Double>( l2.size() );
		
		for(T res: l2)
			list2.add( res.getRealDouble() );
		
		output.setReal( new DynamicTimeWarping(m_dist).compute(list1, list2, new Double(0.0d)) );
        return output;	
	}

	@Override
	public BinaryOperation<List<T>, List<T>, T> copy() {
		return new GenericDynamicTimeWarping<T>(m_dist);
	}

}
