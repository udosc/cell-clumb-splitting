package org.knime.knip.clump.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.region.BresenhamLine;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.clump.boundary.ShapeDescription;
import org.knime.knip.clump.dist.ShapeDistance;
import org.knime.knip.clump.util.MyUtils;

/**
 * 
 * @author Udo
 *
 */
public class Graph<T extends RealType<T> & NativeType<T>>{
	
	private final Double[][] m_weights;
		
	private List<Node> m_nodes;
	
	public Graph(Collection<long[]> splittingPoints){
		super();
		m_weights = new Double[splittingPoints.size()][];
//		m_distances = new Double[splittingPoints.size()][];
		m_nodes = new ArrayList<Node>( splittingPoints.size() );
		int i = -1;
		for(long[] point: splittingPoints){
			++i;
			m_nodes.add(i, new Node(i, point));
			m_weights[i] = new Double[splittingPoints.size()];
			for(int j = 0; j < m_weights[i].length; j++)
				m_weights[i][j] = 0.0d;
		}
	}
	
	public Graph(Collection<long[]> splittingPoints, int minContourLength){
		this( splittingPoints);
	}
	
	public void calc(ShapeDescription<T> clump, 
			ShapeDistance<T> dist, Collection<ShapeDescription<T>> templates, double factor){
		
		
		final double[][] distances = new double[ m_weights.length ][];
		for( int i = 0; i < distances.length; i++){
			distances[i] = new double[ m_weights[i].length ];
		}
	
		double minDist = Double.MAX_VALUE;
		double maxDist = 0.0d;
		
		
		for(int i = 0; i < m_weights.length; i++){
			for(int j = 0; j < m_weights[i].length; j++){
				double min = Double.MAX_VALUE;
				for(ShapeDescription<T> template: templates){
					final long[] start = m_nodes.get(i).getPosition();
					final long[] end   = m_nodes.get(j).getPosition();
					Img<T> boundary = 
							clump.getValues(start, end);
					T w = clump.getType().createVariable();
					
					
					if ( template.getSize() * 1.2d < boundary.dimension(0) ){
						
						w.setReal(Double.MAX_VALUE);
						
					} else { 
						
						dist.compute(template, 
								boundary, 
								w);
						
						if ( w.getRealDouble() == Double.MAX_VALUE )
							continue;
						
						
//						System.out.println( i + ", " + j + ": " + w.getRealDouble() 
//								+ " / " +  boundary.dimension(0) / (double)clump.getSize());
						w.mul( boundary.dimension(0) / (double)clump.getSize() );
						
						
						final double temp = dist.getDistanceMeasure().compute( 
								MyUtils.toDoubleArray(start), 
								MyUtils.toDoubleArray(end));
						distances[i][j] =  temp;
												
						if( temp < minDist )
							minDist = temp;
						else if( temp > maxDist )
							maxDist = temp;

					}
					
					if( w.getRealDouble() < min)
						min = w.getRealDouble();

				}
				m_weights[i][j] = min;
			}
		}
		
		//Min Max Normalization of the distance 
		if( factor >= 1.0d){
			for(int i = 0; i < m_weights.length; i++){
				for(int j = 0; j < m_weights[i].length; j++){
					if ( minDist != maxDist )
						distances[i][j] =  (distances[i][j] - minDist) / (maxDist - minDist);
					m_weights[i][j] = factor * m_weights[i][j] + 
							( 1.0d - factor ) + distances[i][j];
				}
	//			System.out.println();
				
			}	
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
							m_weights[i][j] = -1.0d;
							break;
						}
					}
				}
				
			}
		}
	}
	
	public Double[][] getMatrix(){
		return m_weights;
	}
	
	public Node getNode(int index){
		return m_nodes.get(index);
	}
	
	public int numberOfNodes(){
		return m_weights.length;
	}
	
	
	public Edge getEdge(int source, int destination){
		return new Edge(m_nodes.get(source), m_nodes.get(destination), m_weights[source][destination]);
	}
	
	public void deleteEdge(Edge edge){
		m_weights[ edge.getSource().getIndex() ] [ edge.getDestination().getIndex() ] = -1.0d;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(Double[] dd: m_weights){
			for(Double d: dd){
				sb.append(d == Double.MAX_VALUE ? "-" : d);
				sb.append( ", ");
			}
			sb.append("\r");
		}
		return sb.toString();
	}

		
	//TODO Use a member variable in node....
	public Map<Node, Integer> getDegrees(Collection<Edge> path){
		Map<Node, Integer> map = new HashMap<Node, Integer>(path.size() * 2);
		for( Edge e: path){
			increaseDegree(map, e.getSource());
			increaseDegree(map, e.getDestination() );
		}
		return map;
	}
	
	private Map<Node, Integer> increaseDegree(Map<Node, Integer> map, Node node){
		Integer s = map.get(node );
		if( s == null )
			map.put( node, new Integer( 1 ));
		else
			map.put( node, ++s);
		return map;
	}
	

	public List<Edge> getValidEdges(){
		List<Edge> list = new LinkedList<Edge>();
		for(int i = 0; i < m_weights.length; i++){
			for(int j = 0; j < m_weights[i].length; j++){
				if( m_weights[i][j] >= 0.0d && i != j)
					list.add( 
							new Edge(
									m_nodes.get(i),
									m_nodes.get(j),
									m_weights[i][j]));
			}
		}
		return list;
	}

}
