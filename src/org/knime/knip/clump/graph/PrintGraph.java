package org.knime.knip.clump.graph;

import java.util.Collection;
import java.util.Map;

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
		for(Edge edge: path){
//			draw(ra, edge, (L)new Integer(1338));
			if ( true )
				draw(output, edge, m_value);
			else
				//TODO
				draw(output, edge, m_value);
		}
		return output;
	}

	@Override
	public UnaryOperation<Graph<T>, RandomAccess<R>> copy() {
		return new PrintGraph<T, R>(m_value);
	}
	
	private void draw(RandomAccess<R> ra, Edge edge, R value) {
		Point r1 = new Point(edge.getSource().getPosition());
		Point r2 = new Point(edge.getDestination().getPosition());
		Cursor<R> cursor = 
				new BresenhamLine<R>(ra, r1, r2);
		while( cursor.hasNext() ){
			cursor.next().set( m_value );
		}
	}



}
