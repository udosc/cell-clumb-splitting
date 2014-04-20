package org.knime.knip.clump.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.region.BresenhamLine;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.knime.core.util.Pair;
import org.knime.knip.clump.contour.Contour;
import org.knime.knip.clump.dist.contour.ContourDistance;
import org.knime.knip.clump.dt.MyDelaunayTriangulation;
import org.knime.knip.clump.split.SplittingPoints;
import org.knime.knip.clump.util.MyUtils;

public class GraphSplitting<T extends RealType<T> & NativeType<T>, L extends Comparable<L>> {
	
//	static final double NOT_CONNECTED = Double.MAX_VALUE;
		
	private Edge<BitType>[][] m_weights;
	
	private Contour[][] m_contour;
	
	private double m_factor;
	
	private Contour m_cell;
	
	private List<Node> m_nodes;
	
	private Img<BitType> m_img;
	
	private final ContourDistance<T> m_distance;
	
	private final T m_type;
	
	private long m_maxSize;
	
	private Contour[] m_boundaries;
	
	private int m_solutions;
	
	private double[] m_bCosts;
	
	private List<Pair<Integer, Integer>> m_pairs;
	
	public GraphSplitting(ContourDistance<T> distance, Img<BitType> img, double factor){
//		m_templates = Arrays.asList( templates );
		m_img = img;
		m_distance = distance;
		m_factor = factor;
		m_type = distance.getType();
		for(Contour c: distance.getTemplates() ){
			if( c.size() > m_maxSize )
				m_maxSize = c.size();
		}
	}

	
	public boolean[][] createMatrix(){
		boolean[][] matrix = new boolean[ m_solutions ][];
		for(int i = 0; i < matrix.length; i++)
			matrix[i] = new boolean[ m_nodes.size()];
		int current = 0;
		m_bCosts = new double[ m_solutions ];
		m_pairs = new ArrayList<Pair<Integer, Integer>>( m_solutions );
		for(int i = 0; i < m_weights.length; i++){
			for(int j = 0; j < m_weights[i].length; j++){
				
				if( m_weights[i][j].isValid() && current < matrix.length ){
					 matrix[current] = m_weights[i][j].getBoundaries();
					 m_pairs.add(current, new Pair<Integer, Integer>(i, j));
					 if( m_weights[i][j].getConnectedEdge() == null )
						 m_bCosts[current++] = m_weights[i][j].getWeight();
					 else
						 m_bCosts[current++] = m_weights[i][j].getWeight() + m_weights[i][j].getConnectedEdge().getWeight();
				}
			}
		}
		
		return matrix;
	}
	
//	private List<Integer> getBoundaries(int i, int j){
//		List<Integer> out = new LinkedList<Integer>();
//		
//		return out;
//	}
	
	private boolean[] asBooleanArray(int i, int j, int p1, int p2){
		boolean[] b1 = asBooleanArray(i, p1);
		boolean[] b2 = asBooleanArray(p2, j);
		for(int k = 0; k < b1.length; k++)
			b1[k] |= b2[k];
		return b1;
	}
	
	private boolean[] asBooleanArray(int i, int j){
		boolean[] out  = new boolean[ m_nodes.size() ];
		Edge e = m_weights[i][j].getConnectedEdge();
		for(int k = 0; k <  m_nodes.size(); k++){

//			if( k < m_nodes.size() ){
				if( i < j ){
//					if( e == null ){
						 out[k] = k >= i && k < j ? true : false;
//					 } else {		
//						out[k] = k >= i && k < j || k >= e.getSource().getIndex() && k < e.getDestination().getIndex()? true : false;
//					 }
				} else {
//					if( e == null ){
						 out[k] = k >= j && k < i ? false : true;
//					 } else {		
//						out[k] = k >= j && k < i || k >= e.getSource().getIndex() && k < e.getDestination().getIndex()? false : true;
//					 }			
				}
//			} else {
//				if( e != null ){
//					out[k] = k - m_nodes.size()  == m_weights[i][j].getIndex() || k - m_nodes.size() == m_weights[i][j].getConnectedEdge().getIndex() ? true : false;
//				}else {
//					out[k] = k - m_nodes.size()  == m_weights[i][j].getIndex() ? true : false;
//				}
//				if( out[k] )
//					out[k+1] = true;
//				k++;
//			}
		 }
		return out;
	}
	
	
	public void printMatrix(boolean[][] matrix){
		for(int i = 0; i < matrix.length; i++){
			System.out.print(i + ": ");
			for(int j = 0; j < matrix[i].length; j++){
				if( j == m_nodes.size() )
					System.out.print(" || ");
				System.out.print( matrix[i][j] + ", ");
			}
			System.out.print("\n");
		}
	}

	
	public List<Nuclei<BitType>> compute(Contour contour, SplittingPoints<T> split){
		List<Nuclei<BitType>> out = new LinkedList<Nuclei<BitType>>();
		m_cell = contour;
		
		List<long[]> points = split.compute(contour);
		init( points );
		validate(m_img, 2);
		initNodes();
		if( points.size() > 3 )
			validateDelaunayTriangualtion(points);
//		validate(m_img, 1);
		
		System.out.println( this );
		
		final double shapeDistance = m_distance.compute( m_cell, m_type).getRealDouble();
		
		System.out.println( "Shape Distance: " + shapeDistance);
		
//		RandomAccessibleInterval<T> curvature = new KCosineCurvature<T>(m_distance.getType(), 5).createCurvatureImg(contour);
				
		for(int i = 0; i < m_weights.length; i++){
			for(int j = 0; j < m_weights[i].length; j++){
				
							
				if ( !m_weights[i][j].isValid() || m_contour[i][j].size() < 32  ){
					continue;
				}
				
//				double res = calculateSimilarity( m_contour[i][j] );
//				double sim =   m_distance.compute( m_contour[i][j], m_type).getRealDouble()  ;

				double sim = m_distance.compute( new Contour(m_contour[i][j]), m_type).getRealDouble();
				
//				double dist =  calculateDistance(i, j) / m_contour[i][j].size();
				double res =   ( sim ) *
						( m_contour[i][j].size() / Math.abs( (double)m_cell.size() ) );
				
//				if ( sim > shapeDistance ){
//					m_weights[i][j].setValid( false );
//				} else {
//					m_weights[i][j].setWeight( res  );
//				}
				
				m_weights[i][j].setWeight( res  );
				
				double min = Double.MAX_VALUE;
//				double temp = Double.MAX_VALUE;
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
						

						
						if ( c.size() > m_maxSize * 1.75d || c.size() < 32 )
							continue;
						
						double tmp = m_distance.compute(c, m_type).getRealDouble();
//						tmp +=  m_factor * ((calculateDistance(i, j) + calculateDistance(k, l)) / ( m_contour[i][k].size() + m_contour[l][j].size() ));
						
						tmp *=
								(( m_contour[i][k].size() + m_contour[l][j].size() ) / ((double) m_cell.size() ))  ;
						
						if( tmp < min){
							min = tmp;
							p1 = k;
							p2 = l;
						}
						
					}
				}
				
				if( i == 0 && j == 3){
					System.out.println();
				}
				
				if( p1 > 0 && p2 > 0 && min < m_weights[i][p1].getWeight() + m_weights[p2][j].getWeight() ){
//					m_weights[i][j].setWeight( temp * ( m_contour[i][p1].size() ) / Math.abs( m_cell.size() )) ;
					m_weights[i][j].setWeight( min ) ;
//					m_weights[i][j].setSplitLine(new Pair<Point, Point>( 
//							new Point( m_nodes.get(i).getPosition()), new Point(m_nodes.get(j).getPosition())) );
					m_weights[i][j].setBoundaries( asBooleanArray(i, j, p1, p2) );
//					m_weights[i][j].getSplitLines().add(new SplitLine<BitType>(m_img, 
//							new Point( m_nodes.get(p1).getPosition()), new Point(m_nodes.get(p2).getPosition())) );
					m_weights[i][j].setValid(true);
//					m_weights[i][p1].connectTo( m_weights[p2][j] );
//					m_weights[p2][j].connectTo( m_weights[i][p1] );
//					m_weights[p2][j].setWeight( temp * ( m_contour[p2][j].size() ) / Math.abs( m_cell.size() )) ;
//					m_weights[p2][j].setSplitLine(new Pair<Point, Point>( 
//							new Point( m_nodes.get(p1).getPosition()), new Point(m_nodes.get(p2).getPosition())) );
//					m_split[][]
					List<SplitLine<BitType>> list = new LinkedList<SplitLine<BitType>>();
					list.add( getSplitLine(i, p1));
					list.add( getSplitLine(p2, j));
					out.add( new Nuclei<BitType>(asBooleanArray(i, j, p1, p2), list , min));
				} else {
					out.add( new Nuclei<BitType>(asBooleanArray(i, j), getSplitLine(i, j) , res));
				}

			}
		}
		System.out.println( this.toString()  );
		
		int x = 0;
		for(Nuclei<BitType> n: out){
			System.out.print(x++ +": ");
			for(boolean b: n.getConstrains()){
				System.out.print(b + ", ");
			}
			System.out.println();
		}
		return out;
	}
	
	public SplitLine<BitType> getSplitLine(int i, int j){
		return m_weights[i][j].getSplitLines().get(0);
	}
	
	private void initNodes() {
		for(int i=0; i < m_weights.length;i++){
			for(int j=0; j < m_weights[i].length; j++){
				if(  m_weights[i][j].isValid() )
					m_nodes.get(i).getNodes().add( m_nodes.get(j) );
			}
		}
		
	}
	
	public void validateDelaunayTriangualtion(List<long[]> points){
		for(int i=0; i < m_weights.length;i++){
			for(int j=0; j < m_weights[i].length; j++){
				m_weights[i][j].setValid(false);
			}
		}
		Collection<Pair<Point, Point>> lines = new MyDelaunayTriangulation().compute(points, new LinkedList<Pair<Point, Point>>());
		for(Pair<Point, Point> pair: lines){
			int i = getIndex(pair.getFirst());
			int j = getIndex(pair.getSecond());
			m_weights[i][j].setValid(true);
			m_weights[j][i].setValid(true);
		}
	}
	
	private int getIndex(Point point){
		int out = -1;
		for(Node n: m_nodes){
			if( n.getPosition()[0] == point.getLongPosition(0) && n.getPosition()[1] == point.getLongPosition(1) ){
				out = n.getIndex();
				return out;
			}
		}
		return out;
	}

	public void validate(Img<BitType> img, int tolarate){
		RandomAccess<BitType> ra = img.randomAccess();
		m_solutions = 0;
		for(int i=0; i < m_weights.length;i++){
			for(int j=0; j < m_weights[i].length; j++){
				if( i == j ){
					m_weights[i][j].setValid( false );
				} else {
					Cursor<BitType> cursor = 
							new BresenhamLine<BitType>(ra, 
									new Point(m_nodes.get(i).getPosition()), 
									new Point(m_nodes.get(j).getPosition()));
					int res = 0;
					boolean isValid = true;
					while( cursor.hasNext() ){
						if ( !cursor.next().get() ){
							if( ++res > tolarate ){
								m_weights[i][j].setValid( false );
								isValid = false;
								break;
							}
						}
					}
					
					if( isValid )
						m_weights[i][j].setIndex( m_solutions++ );
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
		m_boundaries = new Contour[ points.size() ];
		
		for(int i = 0; i < points.size(); i++){
			m_nodes.add(i, new Node(i, points.get(i)));
		}
		int counter = 0;
		for(int i = 0; i < points.size(); i++){
//			m_nodes.add(i, new Node(i, points.get(i)));
			m_weights[i] = new Edge[points.size()];
			m_contour[i] = new Contour[points.size()];
			long[] start = points.get(i);
			for(int j = 0; j < m_weights[i].length; j++){
				long[] end = points.get(j);
				m_weights[i][j] = new Edge<BitType>(m_nodes.get(i), m_nodes.get(j), Double.MAX_VALUE);
				m_weights[i][j].setBoundaries( asBooleanArray(i, j) );
				m_weights[i][j].getSplitLines().add(new SplitLine<BitType>(counter++, m_img,
						new Point(m_nodes.get(i).getPosition()), new Point(m_nodes.get(j).getPosition())));
				List<long[]> list = m_cell.getPointsInbetween(start, end).getPoints();
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
	

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(Edge<BitType>[] dd: m_weights){
			for(Edge<BitType> d: dd){
				sb.append(d.isValid() ? d.getWeight() : "-");
				sb.append( ", ");
			}
			sb.append("\r");
		}
		return sb.toString();
	}
	
	private List<SplitLine<BitType>> getSplitLines(Collection<Node> nodes){
		List<SplitLine<BitType>> out = new LinkedList<SplitLine<BitType>>();
		for(Node node: nodes){
			if( node.getPrev() != null)
				for(SplitLine<BitType> p: m_weights[ node.getIndex()][ node.getPrev()].getSplitLines())
					out.add( p );
		}
		return out;
	}
	
//	public List<SplitLine<BitType>> getSolutions(){
//		List<SplitLine<BitType>> out = new LinkedList<SplitLine<BitType>>();
//		boolean[][] temp = createMatrix();
//		
//		if( m_bCosts.length == 0 || temp.length == 0)
//			return null;
//		
////		MinPath min = new MinPath(m_bCosts, temp);
////		min.printMatrix();
//		Solutions solutions = new Solutions(m_bCosts, temp);
////		List<Integer> solution = min.getSolution();
//		List<Integer> solution = solutions.calc();
//		if ( solution == null )
//			return null;
//		for(Integer i: solution){
//			List<SplitLine<BitType>> list = m_weights[m_pairs.get(i).getFirst()][m_pairs.get(i).getSecond()].getSplitLines();
//			for( SplitLine<BitType> line : list)
//				out.add( line);
//		}
//		return out;
//	}
	public List<SplitLine<BitType>> getSolutions(List<Nuclei<BitType>> nuclei){
		List<SplitLine<BitType>> out = new LinkedList<SplitLine<BitType>>();

		Solution<BitType> s = new Solution<BitType>( nuclei, m_boundaries.length, m_factor);
		
//		MinPath min = new MinPath(m_bCosts, temp);
//		min.printMatrix();
//		Solutions solutions = new Solutions(m_bCosts, temp);
//		List<Integer> solution = min.getSolution();
		List<Nuclei<BitType>> solution = s.calc();
		if ( solution == null )
			return null;
		for(Nuclei<BitType> i: solution){
			//List<SplitLine<BitType>> list = m_weights[m_pairs.get(i).getFirst()][m_pairs.get(i).getSecond()].getSplitLines();
			for( SplitLine<BitType> line : i.getSplitLines() )
				out.add( line );
		}
		return out;
	}
	

}

//class Greedy{
//	
//	private final Node m_start;
//	
//	private Node m_end;
//	
//	private final List<Node> m_nodes;
//	
//	private final Edge[][] m_dist;
//	
//	private double m_cost;
//	
//	
//	public Greedy(Edge[][] weight, List<Node> nodes, Node start){
//		m_dist = weight;
//		m_nodes = new ArrayList<Node>( nodes );
//		m_start = start;
//		m_end = start;
////		for(int i = 0; i < nodes.size(); i++){
////			m_prev[i] = nodes.get(i).copy();
////			m_prev[i].setPrev(null);
////			m_prev[i].setDistance(Double.MAX_VALUE);
//////			m_dist[i] = new double[ nodes.size() ];
//////			for(int j = 0; j < m_dist[i].length; j++){
//////				m_dist[i][j] = weight[i][j].isValid() ? weight[i][j].getWeight() : Double.MAX_VALUE;
//////			}
////		}
////		m_prev[ m_start.getIndex() ].setDistance(0.0d);
//	}
//	
//	public Collection<Pair<Point, Point>> compute(){
//		Collection<Pair<Point, Point>> out = new LinkedList<Pair<Point, Point>>();
//		Node start = m_start;
//		m_cost = 0.0d;
//		boolean first = true;
//		while( !m_nodes.isEmpty()){
//			Edge res = getMinEdge( start );
//			if ( res == null)
//				return null;
//			if( !first)
//				m_nodes.remove( start );
//			else 
//				first = false;
//				
//			Edge connceted = res.getConnectedEdge(); 
//			m_cost += res.getWeight();
//			if(  connceted != null ){
//				out.add( connceted.getSplitLine() );
//				m_end = res.getSource();
//				m_nodes.remove( res.getDestination() );
//				m_cost += connceted.getWeight();
//			}
//			start = res.getDestination();
//			out.add( res.getSplitLine() );
//			if( start.equals( m_start ))
//				break;
//		}
//
//		
//		return out;
//	}
//	
//	
//	private Edge getMinEdge( Node n){
//		Integer i = getMinNode(n.getIndex());
//		return i == null ? null : m_dist[ n.getIndex() ][ i ];
//	}
//	
//	private Integer getMinNode(int n){
//		Integer out = null;
//		double min = Double.MAX_VALUE;
//		for(int i = 0; i < m_dist[n].length; i++){
//			if( m_nodes.contains( m_dist[n][i].getDestination() ) && m_dist[n][i].getWeight() < min){
//				out = i;
//				min = m_dist[n][i].getWeight();
//			}
//		}
//		return out;
//	}
//	
//	private boolean relax(Node u, Node v){
//		if( m_dist[ u.getIndex() ][ v.getIndex() ].isValid() ){
//			final double res = u.getDistance() + m_dist[u.getIndex()][v.getIndex()].getWeight();
//			if ( res < v.getDistance()){
//				v.setDistance( res );
//				v.setPrev( u.getIndex() );
//				return true;
//			}
//		}
//		return false;
//	}
//	
//	public double getCost(){
//		return m_cost;
//	}
//			
//}



class Solution<T extends RealType<T>>{
	
	private final List<Nuclei<T>> m_nuclei;
	
	private final List<List<Integer>> m_steps;
	
	private final List<Pair<List<Integer>, Double>> m_solution;
	
	private final int m_length;
	
	private double m_final;
	
	private double m_factor;
	
	public Solution(List<Nuclei<T>> nuclei, int length, double factor){
		m_nuclei = nuclei;
		m_length = length;
		m_factor = factor;
		m_steps = new LinkedList<List<Integer>>();
		m_solution = new LinkedList<Pair<List<Integer>, Double>>();
	}
	
	public List<Nuclei<T>> calc(){
		initSteps();
		for(int i = 1; i < m_length; i++){
			nextStep(i);
		}
		List<Nuclei<T>> out = new LinkedList<Nuclei<T>>();
		m_final = Double.MAX_VALUE;
		for(Pair<List<Integer>, Double> pair: m_solution){
			System.out.println(pair.getFirst() + ": " + pair.getSecond());
//			double res = Double.MAX_VALUE;
			if( pair.getSecond() <  m_final){
				out = getNuclei( pair.getFirst() );
				m_final = pair.getSecond();
			}
		}
		System.out.println("Solution: " + out + ": " + m_final);
		return out;
	}
	
	private List<Nuclei<T>> getNuclei(List<Integer> list){
		List<Nuclei<T>> out = new LinkedList<Nuclei<T>>();
		for(Integer i: list){
			out.add( m_nuclei.get(i));
		}
		return out;
	}
	
	private void initSteps(){
		m_steps.clear();
		for(int i = 0; i < m_nuclei.size(); i++){
			if ( m_nuclei.get(i).getConstrains()[0] ){
				List<Integer> res = new LinkedList<Integer>();
				res.add( i );
				m_steps.add( res );
			}
		}
	}
	
	private double calc(List<Integer> list){
		double out = 0.0d;
		final Set<Integer> used = new HashSet<Integer>();
		for(Integer i: list){
			out += m_nuclei.get(i).getSimilarity();
			for(SplitLine<T> line: m_nuclei.get(i).getSplitLines()){
				if( !used.contains( line.getIndex()) ){
					out += line.euclideanDistance() * m_factor;
					used.add( line.getIndex() );
				}
			}
		}

		
		return out;
	}
	
	private void nextStep(int index){
//		List<Integer> list;
//		while(( list = m_steps.peek() ) != null){
		List<List<Integer>> newOnes = new LinkedList<List<Integer>>();
		for(List<Integer> list: m_steps){
			boolean[] array = toArray(list);
			
			if( array[index]){
				newOnes.add(list);
			}
			
			for(int r = 0; r < m_nuclei.size(); r++){
				if( m_nuclei.get(r).getConstrains()[index] && isValid(array, r) ){
					List<Integer> res = new LinkedList<Integer>( list );
					res.add(r);
					
					boolean[] nArray = toArray(res);
					
					if( isSolution( nArray) ){
						m_solution.add(new Pair<List<Integer>, Double>(res, calc(res)));
					} else{
						newOnes.add( res ); 
					}
				}
			}

		}
		m_steps.clear();
		m_steps.addAll( newOnes );
	}
	
	private boolean isSolution(boolean[] array){
		boolean out = true;
		for(boolean b: array)
			out &= b;
		return out;
	}
	
	private boolean[] toArray(List<Integer> list){
		boolean[] out = new boolean[ m_length ];
		for(Integer i: list){
			merge(out, i);
		}
		return out;
	}
		
	private boolean isValid(boolean[] array, int index){
		for(int i = 0; i <  m_nuclei.get(index).getConstrains().length; i++){
			if ( m_nuclei.get(index).getConstrains()[i] && array[i])
				return false;
		}
		return true;
	}
	
	private boolean[] merge(boolean[] array, int index){
		for(int i = 0; i < m_nuclei.get(index).getConstrains().length; i++){
			 array[i] = m_nuclei.get(index).getConstrains()[i] || array[i];
		}
		return array;
	}
}

class Solutions{
	
	private double[] m_cost;
		
	private boolean[][] m_constraints;
	
	private double[][] m_distances;
	
	private int m_length;
	
	private List<List<Integer>> m_steps;
	
	private List<Pair<List<Integer>, Double>> m_solution;
	
	double m_final;
	
	
	public Solutions(double[] cost, boolean[][] constrain){
		m_length = constrain[0].length;
		m_cost = new double[ cost.length ];
		for( int i = 0; i < m_cost.length; i++){
			m_cost[i] = cost[i];
		}
		m_constraints = constrain;
		m_steps = new LinkedList<List<Integer>>();
		m_solution = new LinkedList<Pair<List<Integer>, Double>>();
	}
	
	public List<Integer> calc(){
		initSteps();
		for(int i = 1; i < m_length; i++){
			nextStep(i);
		}
		List<Integer> out = null;
		m_final = Double.MAX_VALUE;
		for(Pair<List<Integer>, Double> pair: m_solution){
			System.out.println(pair.getFirst() + ": " + pair.getSecond());
//			double res = Double.MAX_VALUE;
			if( pair.getSecond() <  m_final){
				out = pair.getFirst();
				m_final = pair.getSecond();
			}
		}
		System.out.println("Solution: " + out + ": " + m_final);
		return out;
	}
	
	private void initSteps(){
		m_steps.clear();
		for(int i = 0; i < m_constraints.length; i++){
			if ( m_constraints[i][0] ){
				List<Integer> res = new LinkedList<Integer>();
				res.add( i );
				m_steps.add( res );
			}
		}
	}
	
	private double calc(List<Integer> list){
		double out = 0.0d;
		for(Integer i: list){
			out += m_cost[i];
		}
		return out;
	}
	
	private void nextStep(int index){
//		List<Integer> list;
//		while(( list = m_steps.peek() ) != null){
		List<List<Integer>> newOnes = new LinkedList<List<Integer>>();
		for(List<Integer> list: m_steps){
			boolean[] array = toArray(list);
			
			if( array[index]){
				newOnes.add(list);
			}
			
//			if( isSolution(array))
//				m_solution.add(list);
//			else{
				for(int r = 0; r < m_constraints.length; r++){
					if( m_constraints[r][index] && isValid(array, r) ){
						List<Integer> res = new LinkedList<Integer>( list );
						res.add(r);
						
						boolean[] nArray = toArray(res);
						
						if( isSolution( nArray) ){
							m_solution.add(new Pair<List<Integer>, Double>(res, calc(res)));
						} else{
							newOnes.add( res ); 
						}
						

					}
				}
//			}
		}
		m_steps.clear();
		m_steps.addAll( newOnes );
	}
	
	private boolean isSolution(boolean[] array){
		boolean out = true;
		for(boolean b: array)
			out &= b;
		return out;
	}
	
	private boolean[] toArray(List<Integer> list){
		boolean[] out = new boolean[ m_length ];
		for(Integer i: list){
			merge(out, i);
		}
		return out;
	}
		
	private boolean isValid(boolean[] array, int index){
		for(int i = 0; i < m_constraints[index].length; i++){
			if ( m_constraints[index][i] && array[i])
				return false;
		}
		return true;
	}
	
	private boolean[] merge(boolean[] array, int index){
		for(int i = 0; i < m_constraints[index].length; i++){
			 array[i] = m_constraints[index][i] || array[i];
		}
		return array;
	}
	
}

class MinPath{
	
	private double[] m_cost;
	
	private int[] m_prev;
	
	private boolean[][] m_constraints;
	
	private int m_length;
	
	private List<Integer> m_solutions;
	
	public MinPath(double[] cost, boolean[][] constrain ){
		m_length = constrain[0].length;
		m_cost = new double[ cost.length ];
		for( int i = 0; i < m_cost.length; i++){
			m_cost[i] = 0.0d;
		}
		m_prev = new int[ cost.length ];
		m_constraints = constrain;
		m_solutions = calc(cost);
	}
	
	private List<Integer> calc(double[] cost){
		for(int i = 1; i < cost.length; i++){
			double res = Double.MAX_VALUE;
			for(int j = 0; j < i; j++){
				if( isValid(i, j)){
					double temp = cost[i] + m_cost[j];
					if( temp < res){
						res = temp;
						m_prev[i] = j;
					}
				}
			}
			m_cost[i] = res;
			m_constraints[i] = merge(i, m_prev[i]);
		}
		List<Integer> nodes = new LinkedList<Integer>();
		double min = Double.MAX_VALUE;
		for(int i= 0; i < m_cost.length;i++){
			if( isSolution(m_constraints[i]) && m_cost[i] < min ){
				min = m_cost[i];
				nodes.clear();
				addNodes(i, nodes);
			}
		}
		for(Integer i: nodes){
			System.out.println("solution: "+i);
		}
		
		return nodes;
		
		
	}
	
	public List<Integer> getSolution(){
		return m_solutions;
	}
	
	private List<Integer> addNodes(int index, List<Integer> list){
		list.add( index );
		if( m_prev[index] != index ){
//			list.add(m_prev[index]);
			addNodes(m_prev[index], list);
		}
		return list;
	}
	
	private boolean[] merge(int i, int j){
		boolean[] out = new boolean[ m_length];
		for(int n = 0; n < m_constraints[i].length; n++){
			 out[n] = m_constraints[i][n] || m_constraints[j][n];
		}
		return out;
	}
	
	private boolean isSolution(boolean[] array){
		boolean out = true;
		for(boolean b: array)
			out &= b;
		return out;
	}
	
	private boolean isValid(int i, int j){
		for(int n = 0; n < m_constraints[i].length; n++){
			if ( m_constraints[i][n] && m_constraints[j][n])
				return false;
		}
		return true;
	}
	
	public void printMatrix(){
		for(int i = 0; i < m_constraints.length; i++){
			for(int j = 0; j < m_constraints[i].length; j++){
				System.out.print( m_constraints[i][j] + "; ");
			}
			System.out.print("\n");
		}
	}
}

