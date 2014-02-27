package org.knime.knip.clump.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.BresenhamLine;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.knime.core.util.Pair;
import org.knime.knip.clump.contour.Contour;
import org.knime.knip.clump.curvature.KCosineCurvature;
import org.knime.knip.clump.dist.contour.ContourDistance;
import org.knime.knip.clump.split.SplittingPoints;
import org.knime.knip.clump.util.MyUtils;

public class GraphSplitting<T extends RealType<T> & NativeType<T>, L extends Comparable<L>> {
	
//	static final double NOT_CONNECTED = Double.MAX_VALUE;
		
	private Edge[][] m_weights;
	
	private Contour[][] m_contour;
	
	private double m_factor;
	
	private Contour m_cell;
	
	private List<Node> m_nodes;
	
	private Img<BitType> m_img;
	
	private final ContourDistance<T> m_distance;
	
	private final T m_type;
	
	public GraphSplitting(ContourDistance<T> distance, Img<BitType> img, double factor){
//		m_templates = Arrays.asList( templates );
		m_img = img;
		m_distance = distance;
		m_factor = factor;
		m_type = distance.getType();
	}
	
//	public GraphSplitting(ContourDistance<T> distance, Img<BitType> img, double factor, List<Contour> templates){
//		m_templates =  templates;
//		m_img = img;
//		m_distance = distance;
//		m_factor = factor;
//	}
	
	public List<Pair<Point, Point>> compute(Contour contour, SplittingPoints<T> split){
		List<Pair<Point, Point>> out = new LinkedList<Pair<Point, Point>>();
		m_cell = contour;
		
		List<long[]> points = split.compute(contour, new LinkedList<long[]>());
		init( points );
		validate(m_img, 2);
		initNodes();
//		validate(m_img, 1);
		
//		System.out.println( this );
		
		
//		RandomAccessibleInterval<T> curvature = new KCosineCurvature<T>(m_distance.getType(), 5).createCurvatureImg(contour);
				
		for(int i = 0; i < m_weights.length; i++){
			for(int j = 0; j < m_weights[i].length; j++){
				
							
				if ( !m_weights[i][j].isValid() || m_contour[i][j].size() < 32  ){
					continue;
				}
				
				
//				if( i == 2 && j == 5){
//					System.out.print("Q");
//				}

//				
//				final int is = contour.indexOf(start);
//				final int ie = contour.indexOf(end);
			
//				final RandomAccessibleInterval<T> part = Views.interval(
//						Views.extendPeriodic( curvature ),new long[]{ is }, new long[] { ie });
//				List<Contour> list = getContours(i, j);
//				for( Contour c: list){
//
//				}
				
//				double res = calculateSimilarity( m_contour[i][j] );
				double sim =   m_distance.compute( m_contour[i][j], m_type).getRealDouble()  ;
				double dist = m_factor * calculateDistance(i, j);
				double res =   ( sim + dist ) *
						( m_contour[i][j].size() / Math.abs( (double)m_cell.size() ) );
//				if ( res < m_weights[i][j].getWeight()){
					m_weights[i][j].setWeight( res  );
//				}
				
//				//Check if a contour lying inside the cell fits better
//				for(int k = i+1; k < j; k++){
//					for(int l = k + 1; l < j; l++){
//						double tmp = calculateSimilarity( createContour(i, k, l, j));
//						double dist = tmp *(( m_contour[i][k].size() ) / Math.abs( (double)curvature.dimension(0) ) + ( m_contour[l][j].size() ) / Math.abs( (double)curvature.dimension(0) ))  ;
//						if( dist < m_weights[i][k].getWeight() + m_weights[l][j].getWeight() ){
//							m_weights[i][k].setWeight( tmp * ( m_contour[i][k].size() ) / Math.abs( (double)curvature.dimension(0) )) ;
//							m_weights[l][j].setWeight( tmp * ( m_contour[l][j].size() ) / Math.abs( (double)curvature.dimension(0) )) ;
////							m_split[][]
//						}
//					}
//				}
				
				
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
		System.out.println( this.toString()  );
		
		for(int i = 0; i < m_weights.length; i++){
			for(int j = 0; j < m_weights[i].length; j++){
		
							
				if (m_contour[i][j].size() < 5 || !m_weights[i][j].isValid() ){
					continue;
				}
				double min = Double.MAX_VALUE;
				double temp = Double.MAX_VALUE;
				int p1 = -1;
				int p2 = -1;
				//Check if a contour lying inside the cell fits better
//				for(int k = j+1; k < m_nodes.size(); k++){
//					for(int l = k + 1; l < m_nodes.size(); l++){
				for(int k: getPointsBetween(i, j)){
					for(int l: getPointsBetween(i, j)){
						if( k == l || !m_weights[k][l].isValid() )
							continue;
						Contour c = createContour(i, k, l, j);
						double tmp = m_distance.compute(c, m_type).getRealDouble() + m_factor * calculateDistance(i, j);
//						double dist = tmp 
						double dist = tmp *
								(( m_contour[i][k].size() + m_contour[l][j].size() ) / ((double) m_cell.size() ))  ;
						
						if( dist < min){
							min = dist;
							temp = tmp;
							p1 = k;
							p2 = l;
						}
						
					}
				}
				
				if( p1 > 0 && p2 > 0 && min < m_weights[i][p1].getWeight() + m_weights[p2][j].getWeight() ){
					m_weights[i][p1].setWeight( temp * ( m_contour[i][p1].size() ) / Math.abs( m_cell.size() )) ;
					m_weights[i][p1].setSplitLine(new Pair<Point, Point>( 
							new Point( m_nodes.get(i).getPosition()), new Point(m_nodes.get(j).getPosition())) );
					m_weights[i][p1].connectTo( m_weights[p2][j] );
					m_weights[p2][j].connectTo( m_weights[i][p1] );
					m_weights[p2][j].setWeight( temp * ( m_contour[p2][j].size() ) / Math.abs( m_cell.size() )) ;
					m_weights[p2][j].setSplitLine(new Pair<Point, Point>( 
							new Point( m_nodes.get(p1).getPosition()), new Point(m_nodes.get(p2).getPosition())) );
//					m_split[][]
				}

			}
		}
		
//		Floyd<T> floyd = new Floyd<T>(m_weights);
//		Collection<Pair<Point, Point>> path = floyd.getMinPath();
//		printPath(path);
		double cost = Double.MAX_VALUE;
//		Collection<Node> path = null;
		Collection<Pair<Point, Point>> path = null;
//		for(Node n: m_nodes){
		for(Iterator<Node> it = m_nodes.iterator(); it.hasNext(); ){
		//			Djiksta dj = new Djiksta(m_weights, m_nodes, n);
			Greedy minpath = new Greedy(m_weights, m_nodes, it.next());
			Collection<Pair<Point, Point>> list = minpath.compute();
//			Collection<Node> list = dj.compute();
			if (list != null && minpath.getCost() < cost && minpath.getCost() > 0.0d){
				cost = minpath.getCost();
				path = list;
			}
			
		}
//		double s1 = floyd.getPathCost();
//		double s2 = calculateSimilarity( m_cell );
		final double s2 = m_distance.compute( m_cell, m_type).getRealDouble();
		
		System.out.println("Shape Distance: " +  s2 + " - Splitted :" + cost);
		
		if ( cost < s2 ){
//			for( Pair<Point, Point> e: getSplitLines(path)){
////				out.add(new SplitLine( e.getSource().getPosition(), e.getDestination().getPosition(), e.getWeight() ));
//				out.add( e );
//			}
//			return out;
			out.addAll( path );
			return out;
		} else {
			return out;
		}
		

	}
	
	private void initNodes() {
		for(int i=0; i < m_weights.length;i++){
			for(int j=0; j < m_weights[i].length; j++){
				if(  m_weights[i][j].isValid() )
					m_nodes.get(i).getNodes().add( m_nodes.get(j) );
			}
		}
		
	}

	public void validate(Img<BitType> img, int tolarate){
		RandomAccess<BitType> ra = img.randomAccess();
		for(int i=0; i < m_weights.length;i++){
			for(int j=0; j < m_weights[i].length; j++){
				if( i == j ){
					m_weights[i][j].setValid( false );
					continue;
				}
				
				Cursor<BitType> cursor = 
						new BresenhamLine<BitType>(ra, 
								new Point(m_nodes.get(i).getPosition()), 
								new Point(m_nodes.get(j).getPosition()));
				int res = 0;
				while( cursor.hasNext() ){
					if ( !cursor.next().get() ){
						if( ++res > tolarate ){
							m_weights[i][j].setValid( false );
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
		return  calculateSpliteLineLength(m_nodes.get(i).getPosition(), m_nodes.get(j).getPosition()) ;
	}
	
	private double calculateSpliteLineLength(long[] i, long[] j){
		return MyUtils.distance(i, j);
	}
	
		
//	private double calculateSimilarity(Contour part){
//		double min = Double.MAX_VALUE;
//			double res = m_distance.compute(template, part, m_distance.getType().createVariable()).getRealDouble();
//			if( res < min )
//				min = res;
//		}
//		return min;
//	}
	
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
				
				if( i == 0 && j == 1){
					System.out.print("Q");
				}
				
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
		List<long[]> first = new ArrayList<long[]>(m_contour[start][p1].getPoints());
		List<long[]> between = getPoints(p1, p2);
		List<long[]> second = m_contour[p2][end].getPoints();
//		List<long[]> last = getPoints(end, start);
		first.addAll(between);
		first.addAll(second);
//		first.addAll(last);
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
	
	private List<Integer> getPointsBetween(int x, int y){
		List<Integer> out = new LinkedList<Integer>();
		if( x < y){
			for(int i = x;  i < y; i++)
				out.add( i );
		} else {
			for(int i = x; i < m_nodes.size(); i++)
				out.add( i );
			for( int i = 0; i < y; i++)
				out.add( i );
		}
		return out;
	}
	
	private void printPath(Collection<Pair<Point, Point>> path){
		for(Pair<Point, Point> edge: path){
			System.out.print(m_cell.indexOf( new long[] { edge.getFirst().getIntPosition(0), edge.getFirst().getIntPosition(1) })+ " -> " + 
					m_cell.indexOf( new long[] { edge.getSecond().getIntPosition(0), edge.getSecond().getIntPosition(1) }) + ", ");
//			System.out.print(edge.getWeight() + " - ");
		}
//		System.out.println(" - Total: " + m_cost);
	}
	
	public Pair<Point, Point> getSplittingLine(int i, int j){
		return m_weights[i][j].getSplitLine();
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(Edge[] dd: m_weights){
			for(Edge d: dd){
				sb.append(d.isValid() ? d.getWeight() : "-");
				sb.append( ", ");
			}
			sb.append("\r");
		}
		return sb.toString();
	}
	
	private Collection<Pair<Point, Point>> getSplitLines(Collection<Node> nodes){
		Collection<Pair<Point, Point>> out = new LinkedList<Pair<Point, Point>>();
		for(Node node: nodes){
			if( node.getPrev() != null)
				out.add( m_weights[ node.getIndex()][ node.getPrev()].getSplitLine());
		}
		return out;
	}
	

}

class Greedy{
	
	private final Node m_start;
	
	private Node m_end;
	
	private final List<Node> m_nodes;
	
	private final Edge[][] m_dist;
	
	private double m_cost;
	
	public Greedy(Edge[][] weight, List<Node> nodes, Node start){
		m_dist = weight;
		m_nodes = new ArrayList<Node>( nodes );
		m_start = start;
		m_end = start;
//		for(int i = 0; i < nodes.size(); i++){
//			m_prev[i] = nodes.get(i).copy();
//			m_prev[i].setPrev(null);
//			m_prev[i].setDistance(Double.MAX_VALUE);
////			m_dist[i] = new double[ nodes.size() ];
////			for(int j = 0; j < m_dist[i].length; j++){
////				m_dist[i][j] = weight[i][j].isValid() ? weight[i][j].getWeight() : Double.MAX_VALUE;
////			}
//		}
//		m_prev[ m_start.getIndex() ].setDistance(0.0d);
	}
	
	public Collection<Pair<Point, Point>> compute(){
		Collection<Pair<Point, Point>> out = new LinkedList<Pair<Point, Point>>();
		Node start = m_start;
		m_cost = 0.0d;
		boolean first = true;
		while( !m_nodes.isEmpty()){
			Edge res = getMinEdge( start );
			if ( res == null)
				return null;
			if( !first)
				m_nodes.remove( start );
			else 
				first = false;
				
			Edge connceted = res.getConnectedEdge(); 
			m_cost += res.getWeight();
			if(  connceted != null ){
				out.add( connceted.getSplitLine() );
				m_end = res.getSource();
				m_nodes.remove( res.getDestination() );
				m_cost += connceted.getWeight();
			}
			start = res.getDestination();
			out.add( res.getSplitLine() );
			if( start.equals( m_start ))
				break;
		}

		
		return out;
	}
	
	
	private Edge getMinEdge( Node n){
		Integer i = getMinNode(n.getIndex());
		return i == null ? null : m_dist[ n.getIndex() ][ i ];
	}
	
	private Integer getMinNode(int n){
		Integer out = null;
		double min = Double.MAX_VALUE;
		for(int i = 0; i < m_dist[n].length; i++){
			if( m_nodes.contains( m_dist[n][i].getDestination() ) && m_dist[n][i].getWeight() < min){
				out = i;
				min = m_dist[n][i].getWeight();
			}
		}
		return out;
	}
	
	private boolean relax(Node u, Node v){
		if( m_dist[ u.getIndex() ][ v.getIndex() ].isValid() ){
			final double res = u.getDistance() + m_dist[u.getIndex()][v.getIndex()].getWeight();
			if ( res < v.getDistance()){
				v.setDistance( res );
				v.setPrev( u.getIndex() );
				return true;
			}
		}
		return false;
	}
	
	public double getCost(){
		return m_cost;
	}
}

class Djiksta{
	
	private final Node m_start;
	
	private Node m_end;
	
	private final Node[] m_prev;
	
	private final Edge[][] m_dist;
	
	private double m_cost;
	
	public Djiksta(Edge[][] weight, List<Node> nodes, Node start){
		m_dist = weight;
		m_prev = new Node[ nodes.size() ];
		m_start = start;
		for(int i = 0; i < nodes.size(); i++){
			m_prev[i] = nodes.get(i).copy();
			m_prev[i].setPrev(null);
			m_prev[i].setDistance(Double.MAX_VALUE);
//			m_dist[i] = new double[ nodes.size() ];
//			for(int j = 0; j < m_dist[i].length; j++){
//				m_dist[i][j] = weight[i][j].isValid() ? weight[i][j].getWeight() : Double.MAX_VALUE;
//			}
		}
		m_prev[ m_start.getIndex() ].setDistance(0.0d);
	}
	
	public Collection<Node> compute(){
		PriorityQueue<Node> queue = new PriorityQueue<Node>( m_prev.length );
		Collection<Node> out = new LinkedList<Node>();
		
		for(Node node: m_prev)
			queue.add(node);
		while( !queue.isEmpty() ){
			Node res = queue.poll();
			for(Node connected: res.getNodes()){
				relax( m_prev[res.getIndex()], m_prev[ connected.getIndex() ]);
				Edge e = m_dist[res.getIndex()][connected.getIndex() ].getConnectedEdge();
				if (  e != null )
						System.out.print(" Q ");
			}
			out.add(res);
		}
		double min = Double.MAX_VALUE;
		Node prev = m_start;
		for(Node n: out){
			if( m_dist[ n.getIndex() ][ m_start.getIndex() ].isValid() ){
				double res = n.getDistance() + m_dist[ n.getIndex() ][ m_start.getIndex() ].getWeight();
				if( res < min ){
					min = res;
					prev = n;
				}
			}
		}
		m_cost = min;
		return getPath( prev);
	}
	
	
	public double getCost(){
		return m_cost;
	}
	
	private Collection<Node> getPath(Node n){
		Collection<Node> path = new LinkedList<Node>();
		Node node = n;
		path.add( node );
		while( node.getPrev() != null){
			node = m_prev[ node.getPrev() ] ;
			path.add( node );
		}
		return path;
	}
	
	private boolean relax(Node u, Node v){
		if( m_dist[ u.getIndex() ][ v.getIndex() ].isValid() ){
			final double res = u.getDistance() + m_dist[u.getIndex()][v.getIndex()].getWeight();
			if ( res < v.getDistance()){
				v.setDistance( res );
				v.setPrev( u.getIndex() );
				return true;
			}
		}
		return false;
	}
}
