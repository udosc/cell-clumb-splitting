package org.knime.knip.clump.graph;

import java.util.LinkedList;
import java.util.List;


/**
 * 
 * @author Udo Schlegel
 *
 */
public class Node
	implements Comparable<Node>{
	
	private final int m_index;
	
	private final long[] m_position;
	
	private final List<Node> m_connected;
	
	private double m_dist;
	
	private Integer m_prev;
	
	private double m_curvature;
	
	public Node(int index, long[] position){
		m_index = index;
		m_position = position;
		m_connected = new LinkedList<Node>();
		m_dist = Double.MAX_VALUE;
	}
		
	public int getIndex(){
		return m_index;
	}
	
	public long[] getPosition(){
		return m_position;
	}
	
	public List<Node> getNodes(){
		return m_connected;
	}
	
	public double getDistance(){
		return m_dist;
	}
	
	public void setDistance(double dist){
		m_dist = dist;
	}
	
	public Integer getPrev(){
		return m_prev;
	}
	
	public void setPrev(Integer prev){
		m_prev = prev;
	}
	
	public Node copy(){
		Node out = new Node(m_index, m_position);
		out.getNodes().addAll( m_connected );
		return out;
	}
	
	@Override
	public int hashCode(){
		return new Integer( this.getIndex() ).hashCode();
		
	}
	
	@Override
	public boolean equals(Object obj){
		if ( obj instanceof Node ){
			return this.getIndex() ==  ((Node) obj).getIndex() ;
		} return super.equals(obj);
	}

	@Override
	public String toString(){
		return m_index + " (" + m_position + ") ";
	}

	@Override
	public int compareTo(Node o) {
		return Double.compare(m_dist, o.getDistance());
	}
}
