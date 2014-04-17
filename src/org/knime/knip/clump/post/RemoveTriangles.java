package org.knime.knip.clump.post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.knime.core.util.Pair;

import net.imglib2.Point;
import net.imglib2.ops.operation.UnaryOperation;

public class RemoveTriangles implements UnaryOperation<List<Pair<Point, Point>>, List<Pair<Point, Point>>> {

	private List<Pair<Point, Point>> m_list;
	
	@Override
	public List<Pair<Point, Point>> compute(List<Pair<Point, Point>> arg0,
			List<Pair<Point, Point>> arg1) {
		
		arg1.addAll( arg0 );
		
		Map<Point, List<Pair<Point, Point>>> degrees = 
				new HashMap<Point, List<Pair<Point, Point>>>( (int)(arg0.size() * 1.2d));

		for(Pair<Point, Point> p: arg0){		
			List<Pair<Point, Point>> res0 = degrees.get( p.getFirst()) == null ? 
					 new LinkedList<Pair<Point, Point>>() : degrees.get(p.getFirst()) ;
			List<Pair<Point, Point>> res1 = degrees.get( p.getFirst()) == null ? 
					new LinkedList<Pair<Point, Point>>() : degrees.get(p.getFirst()) ;
			res0.add(p);
			res1.add(p);
			degrees.put( p.getFirst(), res0);
			degrees.put( p.getSecond(), res1);
		}
		
		for( Entry<Point, List<Pair<Point, Point>>> e: degrees.entrySet()){
			
		}
		
		return arg1;
	}

	@Override
	public UnaryOperation<List<Pair<Point, Point>>, List<Pair<Point, Point>>> copy() {
		return new RemoveTriangles();
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
