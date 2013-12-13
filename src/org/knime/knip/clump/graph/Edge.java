package org.knime.knip.clump.graph;

import java.util.Collection;


/**
 * 
 * @author Udo Schlegel
 *
 */
public class Edge {
	
	private final Node m_source; 
	
	private final Node m_destination; 
	
	private double m_weight;
	
	private int m_numDimension;
	
	
	public Edge(Node source, Node destination, double weight){
		m_source = source;
		m_destination = destination;
		m_weight = weight;
		m_numDimension = source.getPosition().length;
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
	
	public int getNumberDimension(){
		return m_numDimension;
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
		return out / path.size();
	}

}
