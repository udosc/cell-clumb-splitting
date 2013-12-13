package org.knime.knip.clump.graph;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.region.BresenhamLine;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/** 
 * 
 * @author Udo
 *
 * @param <T>
 * @param <R>
 */
public class PrintGraph<T extends RealType<T> & NativeType<T>, R extends RealType<R>> 
	implements UnaryOperation<Graph<T>, RandomAccess<R>>{

	private final R m_value;
	
	public PrintGraph(R value){
		m_value = value;
	}
	
	@Override
	public RandomAccess<R> compute(Graph<T> input, RandomAccess<R> output) {
		Collection<Edge> path = new Floyd<T>( input ).getMinPath();
		Map<Node, Integer> degrees = input.getDegrees(path);
		Stack<Edge> complex = new Stack<Edge>();
		for(Edge edge: path){
			draw(output, edge, m_value);
//			if ( degrees.get( edge.getSource() ) <= 1 )
//				draw(output, edge, m_value);
//			else
//				complex.add( edge );
		}
		while( !complex.isEmpty() ){
			List<Edge> res = new LinkedList<Edge>();
			Edge edge = complex.pop();
			res.add( edge );
			for(Edge other: complex){
				if( edge.getSource().getIndex() == other.getDestination().getIndex() ||
						other.getSource().getIndex() == edge.getDestination().getIndex() )
					res.add(other);
			}
			complex.removeAll(res);
			
			drawCenter(output, res, m_value);
		}
		return output;
	}

	@Override
	public UnaryOperation<Graph<T>, RandomAccess<R>> copy() {
		return new PrintGraph<T, R>(m_value);
	}
	
	private void drawComplex(RandomAccess<R> ra, List<Edge> edges, R value){

	}
	
	private void drawCenter(RandomAccess<R> ra, List<Edge> edges, R value){
		long[] center = new long[ edges.get(0).getNumberDimension() ];
		final double count =  1.0d / edges.size();
		for( Edge e: edges){
			for( int i = 0; i < center.length; i++){
				center[i] += count * e.getSource().getPosition()[i];
			}
		}
		
		for( Edge e: edges){
			draw( ra, 
					new Point( center ),
					new Point(e.getSource().getPosition()), 
					value);
		}
	}
	
	private void draw(RandomAccess<R> ra, Point p1, Point p2, R value) {
		Cursor<R> cursor = 
				new BresenhamLine<R>(ra, p1, p2);
		while( cursor.hasNext() ){
			cursor.next().set( m_value );
		}
	}
	
	private void draw(RandomAccess<R> ra, Edge edge, R value) {
		draw(ra, 
				new Point(edge.getSource().getPosition()),
				new Point(edge.getDestination().getPosition()), 
				value);
	}



}
