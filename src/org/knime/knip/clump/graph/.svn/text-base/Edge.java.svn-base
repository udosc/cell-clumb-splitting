package org.knime.knip.clump.graph;


/**
 * 
 * @author Udo Schlegel
 *
 */
public class Edge {
	
	private final Node m_source; 
	
	private final Node m_destination; 
	
	private double m_weight;
	
	
	public Edge(Node source, Node destination, double weight){
		m_source = source;
		m_destination = destination;
		m_weight = weight;
	}
	

	public Node getSource(){
		return m_source;
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
	
	@Override
	public String toString(){
		return m_source + " -> " + m_destination + ": " + m_weight;
		
	}

}
