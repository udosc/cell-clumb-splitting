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
	
	
	public Node(int index, long[] position){
		m_index = index;
		m_position = position;
		m_connected = new LinkedList<Node>();
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
		return Integer.compare(m_index, o.getIndex());
	}
}
