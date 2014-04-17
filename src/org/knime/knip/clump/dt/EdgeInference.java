package org.knime.knip.clump.dt;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.imglib2.Point;
import net.imglib2.ops.operation.UnaryOperation;

import org.knime.core.util.Pair;
import org.knime.knip.clump.contour.Contour;

public class EdgeInference 
implements UnaryOperation<Collection<Pair<Point, Point>>, Collection<Pair<Point, Point>>>{

	private final Contour m_contour;
	
	public EdgeInference(Contour c){
		m_contour = c;
	}
	
	@Override
	public Collection<Pair<Point, Point>> compute(
			Collection<Pair<Point, Point>> arg0,
			Collection<Pair<Point, Point>> arg1) {
		Map<Point, List<Pair<Point, Point>>> degrees = 
				new HashMap<Point, List<Pair<Point, Point>>>( (int)(arg0.size() * 1.2d));
		List<Point> used = new LinkedList<Point>();
//		Selection by Inference
		if ( arg0.size() == 1 )
			arg1.addAll( arg0 );
		else if ( arg0.size() > 1 ){
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
			
			//Find all concavity points with degree = 1:
			boolean modified;
			do{
				modified = false;
				for(Entry<Point, List<Pair<Point, Point>>> e: degrees.entrySet()){
					if( e.getValue() != null && e.getValue().size() == 1){
						final Pair<Point, Point> res = e.getValue().get(0);
						arg1.add( res );
						used.add( res.getFirst() );
						used.add( res.getSecond() );
						degrees.put( res.getFirst(), null);
						
						List<Pair<Point, Point>> list ;
						for( Pair<Point, Point> pair: degrees.get(res.getSecond())){
							degrees.put( pair.getSecond(), null);
						}
						
						degrees.put( res.getSecond(), null);
						modified = true;
					} 
				}
			}while( !modified );
		}
		return arg1;
	}

	@Override
	public UnaryOperation<Collection<Pair<Point, Point>>, Collection<Pair<Point, Point>>> copy() {
		return new EdgeInference(m_contour);
	}

	private Collection<Pair<Point, Point>> calcMinConvexity(Collection<Pair<Point, Point>> list){
//		Collection<Pair<Point, Point>> out = new LinkedList<Pair<Point, Point>>();
		double max = 0.0d;
		Pair<Point, Point> maxPair = null;
		for(Pair<Point, Point> p: list){
			final double res = calcConvexity(p);
			if( res > max){
				max = res;
				maxPair = p;
			}
		}
		list.remove( maxPair );
		return list;
	}
	
	private double calcConvexity(Pair<Point, Point> arg){
		double out = 0.0d;
		long[] p1 = new long[ m_contour.numDimensions() ];
		long[] p2 = new long[ m_contour.numDimensions() ];
		arg.getFirst().localize( p1 );
		arg.getSecond().localize( p2 ); 
		for( long[] pos : m_contour.getPointsInbetween(p1, p2).getPoints() ){
			out += m_contour.getUnitVector(pos, 5).phase();
		}
		return out;
	}
}
