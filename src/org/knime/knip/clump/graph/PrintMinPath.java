package org.knime.knip.clump.graph;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
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
public class PrintMinPath<T extends RealType<T> & NativeType<T>, R extends RealType<R>> 
	implements UnaryOperation<List<SplitLine>, RandomAccess<R>>{

	private final R m_value;
	
	public PrintMinPath(R value){
		m_value = value;
	}
	
	@Override
	public RandomAccess<R> compute(List<SplitLine> path, RandomAccess<R> output) {
//		Collection<Edge> path = new Floyd<T>( input ).getMinPath();
		Stack<Edge> complex = new Stack<Edge>();
		if( path.size() > 2 )
			path = prune( path );
		for(SplitLine line: path){
	//			draw(output, edge, m_value);
			draw(output, line, m_value);
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
	public UnaryOperation<List<SplitLine>, RandomAccess<R>> copy() {
		return new PrintMinPath<T, R>(m_value);
	}
	
	private List<SplitLine> prune(List<SplitLine> list){
		List<SplitLine> out = new LinkedList<SplitLine>();
		final PriorityQueue<SplitLine> queue = new PriorityQueue<SplitLine>(
				10, 
				new Comparator<SplitLine>() {

					@Override
					public int compare(SplitLine o1,
							SplitLine o2) {
						return Double.compare(o2.getWeight(), o1.getWeight());
					}
		});
		queue.addAll( list );
		Set<long[]> set = new HashSet<long[]>();
		while ( !queue.isEmpty() ){
			SplitLine e = queue.poll();
			if( !set.contains( e.getP1() ) && 
					!set.contains(e.getP2() )){
				out.add( e );
				set.add( e.getP1() );
				set.add( e.getP2() );
			}
		
		}
		
		
		return out;
		

//		Map<Node, Edge> res = new TreeMap<Node, Edge>();
//		for(Edge e: list){
//			if ( res.containsKey( e.getSource() ) && 
//					e.getWeight() > res.get(e.getSource()).getWeight() ){
//				res.put( e.getSource(), e);
//			} else 
//				res.put( e.getSource(), e);
//		}
//		for( Entry<Node, Edge> e: res.entrySet())
//			out.add( e.getValue() );
//		return out;
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
	
	private void draw(RandomAccess<R> ra, SplitLine line, R value) {
		draw(ra, 
				new Point( line.getP1() ),
				new Point( line.getP2() ), 
				value);
	}



}
