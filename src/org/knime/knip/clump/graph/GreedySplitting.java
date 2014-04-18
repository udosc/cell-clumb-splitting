package org.knime.knip.clump.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.imglib2.Point;
import net.imglib2.type.logic.BitType;

import org.knime.core.util.Pair;

public class GreedySplitting{
	
	private final Node m_start;
	
	private Node m_end;
	
	private final List<Node> m_nodes;
	
	private final Edge[][] m_dist;
	
	private double m_cost;
	
	
	public GreedySplitting(Edge[][] weight, List<Node> nodes, Node start){
		m_dist = weight;
		m_nodes = new ArrayList<Node>( nodes );
		m_start = start;
		m_end = start;
//		for(int i = 0; i < nodes.size(); i++){
//			m_prev[i] = nodes.get(i).copy();
//			m_prev[i].setPrev(null);
//			m_prev[i].setDistance(Double.MAX_VALUE);
////			m_dist[i] = new double[ nodes.size() ];
////			for(int j = 0; j < m_dist[i].length; j++){
////				m_dist[i][j] = weight[i][j].isValid() ? weight[i][j].getWeight() : Double.MAX_VALUE;
////			}
//		}
//		m_prev[ m_start.getIndex() ].setDistance(0.0d);
	}
	
//	public Collection<Pair<Point, Point>> compute(){
//		Collection<Pair<Point, Point>> out = new LinkedList<Pair<Point, Point>>();
//		Node start = m_start;
//		m_cost = 0.0d;
//		boolean first = true;
//		
//		final Edge<BitType> res = getMinEdge( start );
//		final Edge<BitType> connected = res.getConnectedEdge();
//		
//		m_cost = res.getWeight();
//		
//		out.add( res.getSplitLine().get(0) );
//		if( connected != null ){
//			out.add( connected.getSplitLine().get(0) );
////			m_cost += connected.getWeight();
//		}
//		return out;
//		
//		
////		while( !m_nodes.isEmpty()){
////			Edge res = getMinEdge( start );
////			if ( res == null)
////				return null;
////			if( !first)
////				m_nodes.remove( start );
////			else 
////				first = false;
////				
////			Edge connceted = res.getConnectedEdge(); 
////			m_cost += res.getWeight();
////			if(  connceted != null ){
////				out.add( connceted.getSplitLine() );
////				m_end = res.getSource();
////				m_nodes.remove( res.getDestination() );
////				m_cost += connceted.getWeight();
////			}
////			start = res.getDestination();
////			out.add( res.getSplitLine() );
////			if( start.equals( m_start ))
////				break;
////		}
//
//		
//
//	}
	
	
	private Edge getMinEdge( Node n){
		Integer i = getMinNode(n.getIndex());
		return i == null ? null : m_dist[ n.getIndex() ][ i ];
	}
	
	private Integer getMinNode(int n){
		Integer out = null;
		double min = Double.MAX_VALUE;
		for(int i = 0; i < m_dist[n].length; i++){
			if( m_nodes.contains( m_dist[n][i].getDestination() ) && m_dist[n][i].getWeight() < min){
				out = i;
				min = m_dist[n][i].getWeight();
			}
		}
		return out;
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
	
	public double getCost(){
		return m_cost;
	}
}