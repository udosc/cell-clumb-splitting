package org.knime.knip.clump.graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.knime.core.util.Pair;
import org.knime.knip.clump.util.MyUtils;

import net.imglib2.Point;
import net.imglib2.RandomAccessible;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.numeric.RealType;

public class Post<T extends RealType<T>> 
implements UnaryOperation<List<SplitLine<T>>, List<SplitLine<T>>> {

	private Map<Point, List<SplitLine<T>>> m_degrees;
	
	private final RandomAccessible<T> m_source;
	
	public Post(RandomAccessible<T> source){
		m_source = source;
	}
	
	@Override
	public List<SplitLine<T>> compute(List<SplitLine<T>> arg0,
			List<SplitLine<T>> arg1) {
		
		if( arg0 == null )
			return arg1;
		
		arg1.addAll( arg0 );
		
		m_degrees = 
				new TreeMap<Point, List<SplitLine<T>>>( new Comparator<Point>() {

					@Override
					public int compare(Point o1, Point o2) {
						if( o1.getLongPosition(0) == o2.getLongPosition(0)){
							if( o1.getLongPosition(1) == o2.getLongPosition(1))
								return 0;
							else
								return o1.getIntPosition(1) - o2.getIntPosition(1);
						} else 
							return o1.getIntPosition(0) - o2.getIntPosition(0);
					}
				});

		for(SplitLine<T> p: arg0){		
			List<SplitLine<T>> res0 = m_degrees.get( p.getP1()) == null ? 
					 new LinkedList<SplitLine<T>>() : m_degrees.get(p.getP1());
			List<SplitLine<T>> res1 = m_degrees.get( p.getP2() ) == null ? 
					new LinkedList<SplitLine<T>>() : m_degrees.get(p.getP2()) ;
			res0.add(p);
			res1.add(p);
			m_degrees.put( p.getP1(), res0);
			m_degrees.put( p.getP2(), res1);
		}

		for( Entry<Point, List<SplitLine<T>>> e: m_degrees.entrySet()){
			if( e.getValue().size() == 2){
				SplitLine<T> l1 = e.getValue().get(0);
				SplitLine<T> l2 = e.getValue().get(1);
				Point p1 = MyUtils.areEqual( l1.getP1(), e.getKey()) ? l1.getP2() : l1.getP1();
				Point p2 = MyUtils.areEqual( l2.getP1(), e.getKey()) ? l2.getP2() : l2.getP1();
				if( areConnected(p1, p2) ){
					List<SplitLine<T>> res = new ArrayList<SplitLine<T>>(3);
					res.add(l1);
					res.add(l2);
					res.add(getSplitLine(p1, p2));
					
					for( SplitLine<T> line : res){
						arg1.remove( line );
					}
					
					for( SplitLine<T> line : transform(res)){
						arg1.add( line );
					}
				}
			}
			
			
		}
		
		return arg1;
	}
	
	private SplitLine<T> getSplitLine(Point p1, Point p2){
		List<SplitLine<T>> res = m_degrees.get(p1);
		for(SplitLine<T> line : res ){
			if( MyUtils.areEqual(line.getP1(), p2) ||
					MyUtils.areEqual(line.getP2(), p2) )
				return line;
		}
		return null;
	}
	
	private boolean areConnected(Point p1, Point p2){
		for(SplitLine<T> line : m_degrees.get(p1) ){
			if( MyUtils.areEqual(line.getP1(), p2) ||
					MyUtils.areEqual(line.getP2(), p2) )
				return true;
		}
		return false;
	}

	@Override
	public UnaryOperation<List<SplitLine<T>>, List<SplitLine<T>>> copy() {
		return new Post<T>(m_source);
	}
	

	private List<SplitLine<T>> transform(List<SplitLine<T>> origin){
		final List<SplitLine<T>> out = new ArrayList<SplitLine<T>>( origin.size() );
		final long[] pos = new long[ 2 ];
		//calculate the center
		for(SplitLine<T> pair: origin){
			pos[0] += pair.getP1().getLongPosition(0);
			pos[1] += pair.getP1().getLongPosition(1);
			
			System.out.println(pair.getP1().getLongPosition(0)+" : "+pair.getP1().getLongPosition(1));
		}
		
		pos[0] /= origin.size();
		pos[1] /= origin.size();
		
		System.out.println("New center: " + pos[0] + ", " + pos[1]);
		
		for(SplitLine<T> pair: origin){
			out.add( new SplitLine<T>(pair.getIndex(), m_source, pair.getP1(), Point.wrap(pos)) );
			out.add( new SplitLine<T>(pair.getIndex(), m_source, pair.getP2(), Point.wrap(pos)) );
//			out.add( new Pair<Point, Point>(pair.getFirst(), Point.wrap(pos)));
		}
		
		return out;
		
	}
}
