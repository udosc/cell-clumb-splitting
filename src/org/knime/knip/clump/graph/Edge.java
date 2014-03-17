package org.knime.knip.clump.graph;

import java.util.Collection;

import net.imglib2.Point;

import org.knime.core.util.Pair;
import org.knime.knip.core.data.algebra.Complex;


/**
 * 
 * @author Udo Schlegel
 *
 */
public class Edge {
	
	private int m_index;
	
	private final Node m_source; 
	
	private final Node m_destination; 
	
	private double m_weight;
	
	private int m_numDimension;
	
	private Pair<Point, Point> m_splitLine;
	
	private boolean m_valid;
	
	private Edge m_connected;
	
	
	public Edge(Node source, Node destination, double weight){
		m_source = source;
		m_destination = destination;
		m_weight = weight;
		m_numDimension = source.getPosition().length;
		m_valid = true;
		m_connected = null;
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
	
	public Pair<Point, Point> getSplitLine(){
		return m_splitLine;
	}
	
	public void setSplitLine(Pair<Point, Point> splitLine){
		m_splitLine = splitLine;
	}
	
	public void connectTo(Edge edge){
		m_connected = edge;
	}
	
	public Edge getConnectedEdge(){
		return m_connected;
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
			Edge e = (Edge) obj;
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
