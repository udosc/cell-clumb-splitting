package org.knime.knip.clump.graph;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.imglib2.Point;
import net.imglib2.RandomAccessible;
import net.imglib2.type.numeric.RealType;

import org.knime.core.util.Pair;
import org.knime.knip.core.data.algebra.Complex;


/**
 * 
 * @author Udo Schlegel
 *
 */
public class Edge<T extends RealType<T>> {
	
	private int m_index;
	
	private final Node m_source; 
	
	private final Node m_destination; 
	
	private double m_weight;
	
	private int m_numDimension;
	
	private List<SplitLine<T>> m_splitLine;
	
	private boolean m_valid;
	
	private Edge<T> m_connected;
	
	private boolean[] m_boundaries;
	
	public Edge(Node source, Node destination, double weight){
		m_source = source;
		m_destination = destination;
		m_weight = weight;
		m_numDimension = source.getPosition().length;
		m_valid = true;
		m_connected = null;
		m_splitLine = new LinkedList<SplitLine<T>>();
//		m_index =index;
	}
	
	public int getIndex(){
		return m_index;
	}
	
	public void setIndex(int index){
		m_index = index;
	}

	public Node getSource(){
		return m_source;
	}
	
	/**
	 * 
	 * @return The edge as a vector 
	 */
	public Complex getVector(){
		final long[] destination 	= m_destination.getPosition();
		final long[] source 		= m_source.getPosition();
		return new Complex( destination[0] - source[0], 
				destination[1] - source[1]);
	}
	
	/**
	 * 
	 * @return vector in reverse direction
	 */
	public Complex getReverseVector(){
		final long[] source 		= m_destination.getPosition();
		final long[] destination 	= m_source.getPosition();
		return new Complex( destination[0] - source[0], 
				destination[1] - source[1]);
	}
	
	public Node getDestination(){
		return m_destination;
	}
	
	public void setWeight(double weight){
		m_weight = weight;
	}
	
	public double getWeight(){
		return m_weight;
	}
	
	public int getNumberDimension(){
		return m_numDimension;
	}
	
	public boolean isValid(){
		return m_valid;
	}
	
	public void setValid(boolean valid){
		m_valid = valid;
	}
		
	public List<SplitLine<T>> getSplitLines(){
		return m_splitLine;
	}
	
//	public SplitLine<T> getSplitLine(RandomAccessible<T> ra){
//		return new SplitLine<T>(ra, m_source.getPoint(), m_destination.getPoint());
//	}
	
	public void connectTo(Edge<T> edge){
		m_connected = edge;
	}
	
	public Edge<T> getConnectedEdge(){
		return m_connected;
	}
	
	public boolean[] getBoundaries(){
		return m_boundaries;
	}
	
	public void setBoundaries(boolean[] boundaries){
		m_boundaries = boundaries;
	}
	
    @Override
    public int hashCode() {
        final int first = m_source == null ? 0 : m_source.hashCode();
        final int second = m_destination == null ? 0 : m_destination.hashCode();
        return first ^ (second << 2);
    }
	
	
	@Override
	public boolean equals(Object obj){
		if ( obj instanceof Edge ){
			Edge<T> e = (Edge<T>) obj;
			return this.getSource().equals( e.getSource() )
					&& this.getDestination().equals( e.getDestination() );
		} return super.equals(obj);
	}
	
	@Override
	public String toString(){
		return m_source + " -> " + m_destination + ": " + m_weight;
		
	}
	
	
	
	public static double calcPath(Collection<Edge> path){
		double out = 0.0d;
//		System.out.println( "---------------------" );
		for(Edge e: path){
			out += e.getWeight();
//			System.out.println( e );
		}
//		System.out.println();
		return out ;
	}

}
