package org.knime.knip.clump.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
public class Graph<T extends RealType<T> & NativeType<T>> {
	
	private static final int MIN_LENGTH = 2;

	private final Double[][] m_weights;
	
	private List<Node> m_nodes;
	
	public Graph(Collection<long[]> splittingPoints){
		m_weights = new Double[splittingPoints.size()][];
		m_nodes = new ArrayList<Node>( splittingPoints.size() );
		int i = 0;
		for(long[] point: splittingPoints){
			m_nodes.add(i, new Node(i, point));
			m_weights[i++] = new Double[splittingPoints.size()];
		}
	}
	
	public void calc(ShapeDescription<T> clump, 
			ShapeDistance<T> dist, Collection<ShapeDescription<T>> templates, double factor){
		
		double[] minDist = new double[m_weights.length];
		
		
		for(int i = 0; i < m_weights.length; i++){
			minDist[i] = Double.MAX_VALUE;
			for(int j = 0; j < m_weights[i].length; j++){
				double min = Double.MAX_VALUE;
				for(ShapeDescription<T> template: templates){
					final long[] start = m_nodes.get(i).getPosition();
					final long[] end   = m_nodes.get(j).getPosition();
					Img<T> boundary = 
							clump.getValues(start, end);
					T w = clump.getType().createVariable();
					
					if( i == 2 )
						System.out.print("");
					
					if ( boundary.dimension(0) < MIN_LENGTH  ){
						
						w.setReal(Double.MAX_VALUE);
						
					} else { 
						
						dist.compute(template.getImg(), 
								boundary, 
								w);
						
						if ( w.getRealDouble() == Double.MAX_VALUE )
							continue;
						
						
						w.mul( boundary.dimension(0) / (double)clump.getSize() );
						System.out.println( i + ", " + j + ": " + w.getRealDouble() );
						
						
						final double distance = 
							dist.getDistanceMeasure().compute( 
									MyUtils.toDoubleArray(start), 
									MyUtils.toDoubleArray(end));
						
						w.setReal( w.getRealDouble() + factor * distance );

						if( distance < minDist[i])
							minDist[i] = distance;
							

					}
					
					if( w.getRealDouble() < min)
						min = w.getRealDouble();

				}
				m_weights[i][j] = min;
			}
			m_weights[i][i] += minDist[i] * factor;
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
						res++;
						if( res >= tolarate ){
							m_weights[i][j] = Double.MAX_VALUE;
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

	public static double calcPath(Collection<Edge> path){
		double out = 0.0d;
//		System.out.println( "---------------------" );
		for(Edge e: path){
			out += e.getWeight();
//			System.out.println( e );
		}
//		System.out.println();
		return out / path.size();
	}

}
