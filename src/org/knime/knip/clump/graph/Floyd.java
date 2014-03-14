package org.knime.knip.clump.graph;

import java.util.Collection;
import java.util.LinkedList;

import net.imglib2.Point;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.knime.core.util.Pair;

/**
 * 
 * @author Schlegel
 *
 */
public class Floyd<T extends RealType<T> & NativeType<T>> {
	
	
	private final Edge[][] m_graph;
		
	private int[][] m_next;
	
	private Double[][] m_dist;
	
	private double m_cost;
	
	private double m_pathCost;
	
	public Floyd(Edge[][] graph){
		m_graph = graph;
		calc();
	}
	
	private void calc(){
		
		final int n = m_graph.length;
		
		m_dist = new Double[n][];
		
		m_next = new int[n][];
		
		for(int i = 0; i < n; i++){
			m_dist[i] = new Double[n];
			m_next[i] = new int[n];
			for(int j = 0; j < n; j++){
				m_dist[i][j] = m_graph[i][j].isValid() ? m_graph[i][j].getWeight() : Double.MAX_VALUE;
				m_next[i][j] = m_graph[i][j].isValid() ? i : -1; 
			}
		}
		
		for( int k = 0; k < n ; k++ ){
			for( int i = 0; i < n; i++){
				for(int j = 0; j < n; j++){

					// If vertex k is on the shortest path from
	                // i to j, then update the value of dist[i][j]
					final double newDist = m_dist[i][k] + m_dist[k][j];
	                if ( newDist < m_dist[i][j]){
	                	m_dist[i][j] = newDist;
	                	m_next[i][j] = m_next[k][j];
	                }
				}
			}
		}
	}
	
	
	public Double[][] getMinPaths(){
		return m_dist;
	}
	

	public int[][] getPrecursor(){
		return m_next;
	}
	
	public boolean isConnected(Node i, Node j){
		return isConnected(i.getIndex(), j.getIndex());
	}
	
	public boolean isConnected(int i, int j){
		return m_dist[i][j] != null;
	}
	
	public Collection<Pair<Point, Point>> getMinPath(){
		Collection<Pair<Point, Point>> out = new LinkedList<Pair<Point, Point>>();
		double weight = Double.MAX_VALUE;
		for(int i = 0; i < m_graph.length; i++){
			m_cost = 0.0d;
			
			Collection<Pair<Point, Point>> path = getShortestPath(i, i);

			
			if( m_cost < weight){
				out = path;
				weight = m_cost;
			}
			
//			printPath(path);
		}
		m_pathCost = weight;
		return out;
	}
	
	public double getPathCost(){
		return m_pathCost;
	}
	
//	public List<SplitEdge> getMinPath(){
//		List<SplitEdge> out = new LinkedList<SplitEdge>();
//		double weight = Double.MAX_VALUE;
//		for(int i = 0; i < m_graph.numberOfNodes(); i++){
//			for(int j = 0; j < m_graph.numberOfNodes(); j++){
//				if( i == j ) continue;
//				Collection<SplitEdge> p1 = getShortestPath(i, j);
//				Collection<SplitEdge> p2 = getShortestPath(j, i);
//				if( SplitGraph.calcPath(p1) + SplitGraph.calcPath(p2) < weight ){
//					out.clear();
//					out.addAll(p1);
//					out.addAll(p2);
//					weight = SplitGraph.calcPath(p1) + SplitGraph.calcPath(p2);
//				}
//			}
//		}
//		return out;
//	}
	
	public Collection<Pair<Point, Point>> getShortestPath(int source, int destination){
		return shortestPath(new LinkedList<Pair<Point, Point>>(), source, destination);
	}
	
    private Collection<Pair<Point, Point>> shortestPath(Collection<Pair<Point, Point>> nodes, int source, int destination){

        int k = m_next[source][destination];
        
        //If there isn't a path
    	if( k == -1 )
    		return null;
        nodes.add( m_graph[k][destination].getSplitLine() );
        m_cost += m_graph[k][destination].getWeight();
//    	nodes.add(new Edge(m_nodes.get(k), m_nodes.get(destination), m_graph[k][destination].getWeight()));
//        nodes.add( m_graph.getEdge(k, destination) );
        if (k != source) {
        	
        	shortestPath(nodes, source, k);
        }
        return nodes;
    }

}
