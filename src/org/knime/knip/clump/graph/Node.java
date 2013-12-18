package org.knime.knip.clump.graph;

import java.util.LinkedList;
import java.util.List;


/**
 * 
 * @author Udo Schlegel
 *
 */
public class Node{
	
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
	public String toString(){
		return m_index + " (" + m_position + ") ";
	}
}
