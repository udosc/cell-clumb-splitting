package org.knime.knip.clump.split;

import java.util.List;

import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.knime.core.node.KNIMEConstants;
import org.knime.knip.base.KNIPConstants;
import org.knime.knip.base.ThreadPoolExecutorService;
import org.knime.knip.clump.boundary.Contour;
import org.knime.knip.clump.boundary.Curvature;

public class CurvatureBasedSplitting<T extends RealType<T> & NativeType<T>> 
	implements SplittingPoints<T> {

	private int m_order;
	
	private T m_type;
	
	private int m_radius;
	
	private double m_threshold;
	
	private double m_sigma;
	
	public CurvatureBasedSplitting(int order, double threshold, int radius, T type){
		m_order 	= order;
		m_radius 	= radius;
		m_type 		= type.createVariable();
		m_threshold = threshold;
	}
	
	public CurvatureBasedSplitting(int order, double threshold, int radius, T type, double sigma){
		this(order, threshold, radius, type);
		m_sigma = sigma;
	}
	
	@Override
	public List<long[]> compute(Contour input, List<long[]> output) {
		Curvature<T> curv = 
				new Curvature<T>(input, m_order, m_type);
		
		curv.gaussian(m_sigma, new ThreadPoolExecutorService(
	            KNIMEConstants.GLOBAL_THREAD_POOL.createSubPool(KNIPConstants.THREADS_PER_NODE)));

		for(int i = 0; i < input.size(); i++){
			final double res = curv.getCurvature(i).getRealDouble();
			if ( res >= m_threshold ){
				boolean isLow = false;
				for( int j = 0; j < m_radius; j++){
					if(  res < curv.getCurvature(i-j).getRealDouble() ||
							res < curv.getCurvature(i+j).getRealDouble() ){
						isLow = true;
						break;
					}
				}
				if ( !isLow )
					output.add( input.get(i) );	
			}
			
		}
		
		return output;
	}

	@Override
	public UnaryOperation<Contour, List<long[]>> copy() {
		// TODO Auto-generated method stub
		return null;
	}

}
