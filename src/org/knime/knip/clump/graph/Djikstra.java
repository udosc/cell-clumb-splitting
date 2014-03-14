package org.knime.knip.clump.graph;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * 
 * @author Schlegel
 *
 */
public class Djikstra{
	
	private final Node m_start;
		
	private final Node[] m_prev;
	
	private final Edge[][] m_dist;
	
	private double m_cost;
	
	public Djikstra(Edge[][] weight, List<Node> nodes, Node start){
		m_dist = weight;
		m_prev = new Node[ nodes.size() ];
		m_start = start;
		for(int i = 0; i < nodes.size(); i++){
			m_prev[i] = nodes.get(i).copy();
			m_prev[i].setPrev(null);
			m_prev[i].setDistance(Double.MAX_VALUE);
		}
		m_prev[ m_start.getIndex() ].setDistance(0.0d);
	}
	
	public Collection<Node> compute(){
		PriorityQueue<Node> queue = new PriorityQueue<Node>( m_prev.length );
		Collection<Node> out = new LinkedList<Node>();
		
		for(Node node: m_prev)
			queue.add(node);
		while( !queue.isEmpty() ){
			Node res = queue.poll();
			for(Node connected: res.getNodes()){
				relax( m_prev[res.getIndex()], m_prev[ connected.getIndex() ]);
				Edge e = m_dist[res.getIndex()][connected.getIndex() ].getConnectedEdge();
				if (  e != null )
						System.out.print(" Q ");
			}
			out.add(res);
		}
		double min = Double.MAX_VALUE;
		Node prev = m_start;
		for(Node n: out){
			if( m_dist[ n.getIndex() ][ m_start.getIndex() ].isValid() ){
				double res = n.getDistance() + m_dist[ n.getIndex() ][ m_start.getIndex() ].getWeight();
				if( res < min ){
					min = res;
					prev = n;
				}
			}
		}
		m_cost = min;
		return getPath( prev);
	}
	
	
	public double getCost(){
		return m_cost;
	}
	
	public Collection<Node> getPath(Node n){
		Collection<Node> path = new LinkedList<Node>();
		Node node = n;
		path.add( node );
		while( node.getPrev() != null){
			node = m_prev[ node.getPrev() ] ;
			path.add( node );
		}
		return path;
	}
	
	private boolean relax(Node u, Node v){
		if( m_dist[ u.getIndex() ][ v.getIndex() ].isValid() ){
			final double res = u.getDistance() + m_dist[u.getIndex()][v.getIndex()].getWeight();
			if ( res < v.getDistance()){
				v.setDistance( res );
				v.setPrev( u.getIndex() );
				return true;
			}
		}
		return false;
	}
}