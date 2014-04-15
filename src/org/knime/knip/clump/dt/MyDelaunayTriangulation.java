package org.knime.knip.clump.dt;

import il.ac.idc.jdt.DelaunayTriangulation;
import il.ac.idc.jdt.JDTPoint;
import il.ac.idc.jdt.Triangle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.imglib2.Point;
import net.imglib2.ops.operation.UnaryOperation;

import org.knime.core.util.Pair;

/**
 * 
 * @author Schlegel
 *
 */
public class MyDelaunayTriangulation
implements UnaryOperation<List<long[]>, List<Pair<Point, Point>>>{

	@Override
	public List<Pair<Point, Point>> compute(List<long[]> arg0,
			List<Pair<Point, Point>> arg1) {
		
		
		if( arg0.size() <= 3 && arg0.size() > 1){
			arg1.add(new Pair<Point, Point>( Point.wrap( arg0.get(0)), Point.wrap( arg0.get(1))));
			if(arg0.size() == 3){
				arg1.add(new Pair<Point, Point>( Point.wrap( arg0.get(0)), Point.wrap( arg0.get(2))));
				arg1.add(new Pair<Point, Point>( Point.wrap( arg0.get(1)), Point.wrap( arg0.get(2))));
			}
		}
		DelaunayTriangulation dt = new DelaunayTriangulation( toPoint(arg0 ));
		for(Triangle t: dt.getTriangulation()){
			arg1.add( new Pair<Point, Point>( new Point( (long)(t.getA().getX()), (long)(t.getA().getY())  ),
					new Point( (long)(t.getB().getX()), (long)(t.getB().getY()) ) ) );
			if( t.getC() != null ){
				arg1.add( new Pair<Point, Point>( new Point( (long)(t.getB().getX()), (long)(t.getB().getY())  ),
						new Point( (long)(t.getC().getX()), (long)(t.getC().getY()) ) ) );
				arg1.add( new Pair<Point, Point>( new Point( (long)(t.getC().getX()), (long)(t.getC().getY())  ),
						new Point( (long)(t.getA().getX()), (long)(t.getA().getY()) ) ) );
			}
		}
		return removeDuplicates(arg1);
	}

	@Override
	public UnaryOperation<List<long[]>, List<Pair<Point, Point>>> copy() {
		return new MyDelaunayTriangulation();
	}
	
    private List<Pair<Point, Point>> removeDuplicates(List<Pair<Point, Point>> list){
    	final List<Pair<Point, Point>> out = new LinkedList<Pair<Point, Point>>();
    	for(Pair<Point, Point> e: list){
    		if( !out.contains(e ) )
    			out.add( e );
    	}
    	return out;
    }

	
	private List<JDTPoint> toPoint(List<long[]> points){
		List<JDTPoint> out = new ArrayList<JDTPoint>( points.size() );
		for(long[] p: points)
			out.add( new JDTPoint(p[0], p[1]) );
		return out;
	}
}
