package org.knime.knip.clump.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.knime.core.util.Pair;

import net.imglib2.Point;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.numeric.RealType;

public class Post<T extends RealType<T>> 
implements UnaryOperation<List<SplitLine<T>>, List<SplitLine<T>>> {

	
	@Override
	public List<SplitLine<T>> compute(List<SplitLine<T>> arg0,
			List<SplitLine<T>> arg1) {
		
		if( arg0 == null )
			return arg1;
		
		arg1.addAll( arg0 );
		
		Map<Point, List<SplitLine<T>>> degrees = 
				new HashMap<Point, List<SplitLine<T>>>( (int)(arg0.size() * 1.2d));

		for(SplitLine<T> p: arg0){		
			List<SplitLine<T>> res0 = degrees.get( p.getP1()) == null ? 
					 new LinkedList<SplitLine<T>>() : degrees.get(p.getP1());
			List<SplitLine<T>> res1 = degrees.get( p.getP2() ) == null ? 
					new LinkedList<SplitLine<T>>() : degrees.get(p.getP2()) ;
			res0.add(p);
			res1.add(p);
			degrees.put( p.getP1(), res0);
			degrees.put( p.getP2(), res1);
		}
		
		for( Entry<Point, List<SplitLine<T>>> e: degrees.entrySet()){
			if( e.getValue().size() == 2){
				e.getValue().get(0);
				e.getValue().get(1);
			}
			
			
		}
		
		return arg1;
	}

	@Override
	public UnaryOperation<List<SplitLine<T>>, List<SplitLine<T>>> copy() {
		return new Post<T>();
	}
	

	private List<Pair<Point, Point>> transform(List<Pair<Point, Point>> origin){
		final List<Pair<Point, Point>> out = new ArrayList<Pair<Point, Point>>( origin.size() );
		final long[] pos = new long[ 2 ];
		//calculate the center
		for(Pair<Point, Point> pair: origin){
			pos[0] += pair.getFirst().getLongPosition(0);
			pos[1] += pair.getFirst().getLongPosition(1);
		}
		
		pos[0] /= origin.size();
		pos[1] /= origin.size();
		
		for(Pair<Point, Point> pair: origin){
			out.add( new Pair<Point, Point>(pair.getFirst(), Point.wrap(pos)));
		}
		
		return out;
		
	}
}
