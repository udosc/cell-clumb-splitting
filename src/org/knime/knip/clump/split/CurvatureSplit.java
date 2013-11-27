package org.knime.knip.clump.split;

import java.util.LinkedList;
import java.util.List;

import net.imglib2.ops.operation.randomaccessibleinterval.unary.LocalMaximaForDistanceMap;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.knime.core.node.KNIMEConstants;
import org.knime.knip.base.KNIPConstants;
import org.knime.knip.base.ThreadPoolExecutorService;
import org.knime.knip.clump.boundary.Contour;
import org.knime.knip.clump.boundary.Curvature;

/**
 * 
 * @author Udo Schlegel
 *
 * @param <T>
 * @param <L>
 */
public class CurvatureSplit<T extends RealType<T> & NativeType<T>, L extends Comparable<L>>
	implements SplittingPoints<T, L>{
	
	private final int m_order;
	
	private final double m_threshold;
	
	private final double m_sigma;
	
	
	public CurvatureSplit(int order, double threshold, double sigma){
		m_order = order;
		m_threshold = threshold;
		m_sigma = sigma;
	}


	@Override
	public List<long[]> compute(Contour contour, List<long[]> output) {
		
		Curvature<DoubleType> c = 
				new Curvature<DoubleType>(contour, m_order, new DoubleType());
		c.gaussian(m_sigma, new ThreadPoolExecutorService(
	            KNIMEConstants.GLOBAL_THREAD_POOL.createSubPool(KNIPConstants.THREADS_PER_NODE)));

		
		List<long[]> points = new LocalMaximaForDistanceMap<DoubleType>( 
				LocalMaximaForDistanceMap.NeighborhoodType.EIGHT ).
			compute(c.getImg(), new LinkedList<long[]>());
		
		for(long[] p: points){
			if( c.getValueOf(p).getRealDouble() > m_threshold ){
				long[] res = 
						c.getPosition(p[0]);
				output.add( new long[] { res[0], res[1]});
			}
		}
		return output;
	}

	@Override
	public SplittingPoints<T, L> copy() {
		return new CurvatureSplit<T, L>(m_order, m_threshold, m_sigma);
	}

}
