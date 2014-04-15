package org.knime.knip.clump.split;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import net.imglib2.ops.operation.iterable.unary.Mean;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.knime.core.node.KNIMEConstants;
import org.knime.core.util.Pair;
import org.knime.knip.base.KNIPConstants;
import org.knime.knip.base.ThreadPoolExecutorService;
import org.knime.knip.clump.contour.Contour;
import org.knime.knip.clump.curvature.Curvature;
import org.knime.knip.clump.util.StandardDeviation;

/**
 * 
 * @author s
 *
 * @param <T>
 */
public class CurvatureSplittingPoints<T extends RealType<T> & NativeType<T>> 
	implements SplittingPoints<T> {

	private final int m_order;
	
	private final T m_type;
	
	private final int m_radius;
		
	private final double m_sigma;
	
	private Double m_threshold;
	
	public CurvatureSplittingPoints(int order, int radius, T type, double sigma){
		m_order 	= order;
		m_radius 	= radius;
		m_type 		= type.createVariable();
		m_sigma 	= sigma;
		m_threshold = null;
	}
	
	public CurvatureSplittingPoints(int order, int radius, T type, double sigma, Double threshold){
		this(order, radius, type, sigma);
		m_threshold = threshold;
	}
	
	@Override
	public List<long[]> compute(Contour input) {
		 List<long[]> output = new LinkedList<long[]>();
		
		final Curvature<T> curv = 
				new Curvature<T>(input, m_order, m_type);
		
		curv.gaussian(m_sigma, new ThreadPoolExecutorService(
	            KNIMEConstants.GLOBAL_THREAD_POOL.createSubPool(KNIPConstants.THREADS_PER_NODE)));
		
		if( m_threshold == null ){
			final double mean = new Mean<T, DoubleType>().
					compute(curv.getImg().iterator(), new DoubleType(0.0d)).getRealDouble();
			
			final double std = new StandardDeviation<T, DoubleType>(mean).
					compute(curv.getImg().iterator(), new DoubleType(0.0d)).getRealDouble();
			
			m_threshold = mean + std;
			
			System.out.println( "Setting the threshold to " + m_threshold);
		}

		final PriorityQueue<Pair<Integer, Double>> queue = new PriorityQueue<Pair<Integer, Double>>(
				10, 
				new Comparator<Pair<Integer, Double>>() {

					@Override
					public int compare(Pair<Integer, Double> o1,
							Pair<Integer, Double> o2) {
						return o2.getSecond().compareTo( o1.getSecond() );
					}
		});
		
		for(int i = 0; i < input.size(); i++){
//			double max = -Double.MAX_VALUE;
//			for(int r = 1; r < m_radius; r++){
//				final double tmpPrev = curv.getCurvature(i-r).getRealDouble();
//				final double tmpNext = curv.getCurvature(i+r).getRealDouble();
//				if(tmpPrev > max)
//					max = tmpPrev;
//				else if(tmpNext > max )
//					max = tmpNext;
//			}
			
			
			final double res = curv.getCurvature(i).getRealDouble();
			if ( res >= m_threshold && 
					res >= curv.getCurvature(i-1).getRealDouble() &&
					res >= curv.getCurvature(i+1).getRealDouble()){
				queue.add( new Pair<Integer, Double>(i, res) );
				output.add( curv.getPosition(i));
			}
			
		}
		
		while ( !queue.isEmpty() ){
			Pair<Integer, Double> res = queue.poll();
//			List<Pair<Integer, Double>> list = new LinkedList<Pair<Integer, Double>>();
			for( Pair<Integer, Double> pair: queue){
				if( Math.abs( pair.getFirst() - res.getFirst() ) < m_radius )
					output.remove( curv.getPosition(pair.getFirst()) );
			}
//			for( Pair<Integer, Double> pair: list){
//				queue.remove( pair );
//			}
//			output.add( curv.getPosition( res.getFirst() ));
		}
		
		return output;
	}

}
