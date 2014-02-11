package org.knime.knip.clump.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.BresenhamLine;
import net.imglib2.img.Img;
import net.imglib2.labeling.LabelingType;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.knime.core.util.Pair;
import org.knime.knip.clump.contour.Contour;
import org.knime.knip.clump.curvature.KCosineCurvature;
import org.knime.knip.clump.dist.ContourDistance;
import org.knime.knip.clump.split.SplittingPoints;
import org.knime.knip.clump.util.MyUtils;

public class GraphSplitting<T extends RealType<T> & NativeType<T>, L extends Comparable<L>> {
	
	private static final double NOT_CONNECTED = -1.0d;
	
	private final List<Contour> m_templates;
	
	private Edge[][] m_weights;
	
	private Contour[][] m_contour;
	
	private Pair<Point, Point>[][] m_split;
	
	private Contour m_cell;
	
	private List<Node> m_nodes;
	
	private Img<BitType> m_img;
	
	private final ContourDistance<T> m_distance;
	
	public GraphSplitting(ContourDistance<T> distance, Img<BitType> img, Contour... templates){
		m_templates = Arrays.asList( templates );
		m_img = img;
		m_distance = distance;
	}
	
	public GraphSplitting(ContourDistance<T> distance, Img<BitType> img,List<Contour> templates){
		m_templates =  templates;
		m_img = img;
		m_distance = distance;
	}
	
	public List<SplitLine> compute(Contour contour, SplittingPoints<T> split, double factor){
		List<SplitLine> out = new LinkedList<SplitLine>();
		m_cell = contour;
		
		List<long[]> points = split.compute(contour, new LinkedList<long[]>());
		init( points );
		

		RandomAccessibleInterval<T> curvature = new KCosineCurvature<T>(m_distance.getType(), 5).createCurvatureImg(contour);
				
		for(int i = 0; i < m_weights.length; i++){
			for(int j = 0; j < m_weights[i].length; j++){
				
							
				if ( i == j || m_contour[i][j].size() < 5 ){
					continue;
				}
				

//				
//				final int is = contour.indexOf(start);
//				final int ie = contour.indexOf(end);
			
//				final RandomAccessibleInterval<T> part = Views.interval(
//						Views.extendPeriodic( curvature ),new long[]{ is }, new long[] { ie });
//				List<Contour> list = getContours(i, j);
//				for( Contour c: list){
//
//				}
				
				double res = calculateSimilarity( m_contour[i][j] );
				res *=  ( m_contour[i][j].size() ) / Math.abs( (double)m_cell.size() ) ;
				if ( res < m_weights[i][j].getWeight()){
					m_weights[i][j].setWeight( res + factor * calculateDistance(i, j) );
				}
				
				//Check if a contour lying inside the cell fits better
				for(int k = i+1; k < j; k++){
					for(int l = k + 1; l < j; l++){
						double tmp = calculateSimilarity( createContour(i, k, l, j));
						double dist = tmp *(( m_contour[i][k].size() ) / Math.abs( (double)curvature.dimension(0) ) + ( m_contour[l][j].size() ) / Math.abs( (double)curvature.dimension(0) ))  ;
						if( dist < m_weights[i][k].getWeight() + m_weights[l][j].getWeight() ){
							m_weights[i][k].setWeight( tmp * ( m_contour[i][k].size() ) / Math.abs( (double)curvature.dimension(0) )) ;
							m_weights[l][j].setWeight( tmp * ( m_contour[l][j].size() ) / Math.abs( (double)curvature.dimension(0) )) ;
//							m_split[][]
						}
					}
				}
				
				
//						System.out.println( i + ", " + j + ": " + w.getRealDouble() 
//								+ " / " +  boundary.dimension(0) / (double)clump.getSize());
				
//				System.out.println( part.dimension(0));
//				System.out.println( curvature.dimension(0));

				
				
//				final double temp = new EuclideanDistance().compute( 
//						MyUtils.toDoubleArray(start), 
//						MyUtils.toDoubleArray(end));
//				distances[i][j] =  temp;
//										
//				if( temp < minDist )
//					minDist = temp;
//				else if( temp > maxDist )
//					maxDist = temp;


			}
		}
		
		Collection<Edge> path = new Floyd<T>(m_weights, m_nodes).getMinPath();
		double s1 = Edge.calcPath(path);
		double s2 = calculateSimilarity( m_cell );
		
		if ( s1 < s2 ){
			for( Edge e: new Floyd<T>(m_weights, m_nodes).getMinPath()){
				out.add(new SplitLine( e.getSource().getPosition(), e.getDestination().getPosition(), e.getWeight() ));
			}
			return out;
		} else {
			return null;
		}
		

	}
	
	public void validate(Img<BitType> img, int tolarate){
		RandomAccess<BitType> ra = img.randomAccess();
		for(int i=0; i < m_weights.length;i++){
			for(int j=0; j < m_weights[i].length; j++){
				Cursor<BitType> cursor = 
						new BresenhamLine<BitType>(ra, 
								new Point(m_nodes.get(i).getPosition()), 
								new Point(m_nodes.get(j).getPosition()));
				int res = 0;
				while( cursor.hasNext() ){
					if ( !cursor.next().get() ){
						if( ++res > tolarate ){
							m_weights[i][j].setWeight( NOT_CONNECTED );
							break;
						}
					}
				}
				
			}
		}
	}
	
	private List<Contour> getContours(int i, int j){
		List<Contour> out = new LinkedList<Contour>();
		out.add( m_contour[i][j] );
		
		for(int k = i+1; k < j; k++){
			for(int l = k + 1; l < j; l++){
				out.add( createContour(i, k, l, j));
			}
		}
		
//		for(int k = i; k < m_nodes.size(); ++k){
//			for(int l = k; l < m_nodes.size(); ++l){
//				out.add( createContour(i, k, l, j) );
//			}
//		}
		return out;
	}
	
	private double calculateDistance(int i, int j){
		return calculateDistance(m_nodes.get(i).getPosition(), m_nodes.get(j).getPosition());
	}
	
	private double calculateDistance(long[] i, long[] j){
		return MyUtils.distance(i, j);
	}
	
		
	private double calculateSimilarity(Contour part){
		double min = Double.MAX_VALUE;
		for(Contour template: m_templates){
			double res = m_distance.compute(template, part, m_distance.getType().createVariable()).getRealDouble();
			if( res < min )
				min = res;
		}
		return min;
	}
	
	private void init(List<long[]> points){
		m_weights = new Edge[ points.size() ][];
		m_contour = new Contour[ points.size() ][];
//		m_distances = new Double[splittingPoints.size()][];
		m_nodes = new ArrayList<Node>( points.size() );

		
		for(int i = 0; i < points.size(); i++){
			m_nodes.add(i, new Node(i, points.get(i)));
		}
		
		for(int i = 0; i < points.size(); i++){
//			m_nodes.add(i, new Node(i, points.get(i)));
			m_weights[i] = new Edge[points.size()];
			m_contour[i] = new Contour[points.size()];
			long[] start = points.get(i);
			for(int j = 0; j < m_weights[i].length; j++){
				long[] end = points.get(j);
				m_weights[i][j] = new Edge(m_nodes.get(i), m_nodes.get(j), Double.MAX_VALUE);
				m_weights[i][j].setSplitLine(
						new Pair<Point, Point>( new Point(m_nodes.get(i).getPosition()), new Point(m_nodes.get(j).getPosition())));
				List<long[]> list = m_cell.getPointsInbetween(start, end);
//				list.addAll( getPoints(end, start));
				m_contour[i][j] = new Contour( list );
			}
		}
	}
	
//	public RandomAccess<LabelingType<L>> draw(RandomAccess<LabelingType<L>> ra){
//		Integer k = 0;
//		for(int i = 0; i < m_contour.length; i++){
//			for(int j = 0; j < m_contour[i].length; j++){
//				for( long[] pos: m_contour[i][j].getIterator()){
//					ra.setPosition(pos);
//					if( ra.get().getLabeling() == null){
//						List<L> list = new LinkedList<L>();
//						list.add((L) k);
//						ra.get().setLabeling(list);
//					} else {
//						List<L> list = ra.get().getLabeling();
//						list.add((L) k);
//						ra.get().setLabeling(list);	
//					}
//				}
//				k++;
//			}
//		}
//		return ra;
//	}
	
	private Contour createContour(int start, int p1, int p2, int end){
		List<long[]> first = m_contour[start][p1].getPoints();
		List<long[]> between = getPoints(p1, p2);
		List<long[]> second = m_contour[p2][end].getPoints();
		List<long[]> last = getPoints(end, start);
		first.addAll(between);
		first.addAll(second);
		first.addAll(last);
		return new Contour(first);
	}
	

//	private List<Node> getConnectedNodes(int index){
//		List<Node> out = new LinkedList<Node>();
//		for(int i = 0; i < m_weights[index].length; i++){
//			if( m_weights[index][i] != NOT_CONNECTED && index != i)
//				out.add( m_nodes.get(i) );
//		}
//		return out;
//	}
	
	private List<long[]> getPoints(int start, int end){
		return getPoints( m_nodes.get(start).getPosition(), m_nodes.get(end).getPosition());
	}
	
	private List<long[]> getPoints(long[] start, long[] end){
		Cursor<BitType> cursor = 
				new BresenhamLine<BitType>(m_img.randomAccess(), 
						new Point(start), 
						new Point(end));
		
		List<long[]> out = new LinkedList<long[]>();
		
		while( cursor.hasNext() ){
			cursor.fwd();
			long[] pos = new long[ m_img.numDimensions() ];
			cursor.localize( pos );
			out.add(pos);
		}
		
		return out;
		
	}
	

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(Edge[] dd: m_weights){
			for(Edge d: dd){
				sb.append(d.getWeight() == NOT_CONNECTED ? "-" : d.getWeight());
				sb.append( ", ");
			}
			sb.append("\r");
		}
		return sb.toString();
	}
	

}
