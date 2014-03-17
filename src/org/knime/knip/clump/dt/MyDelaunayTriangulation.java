package org.knime.knip.clump.dt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.imglib2.Point;
import net.imglib2.ops.operation.UnaryOperation;

import org.knime.core.util.Pair;
import org.knime.knip.clump.dt.jdt.DelaunayTriangulation;
import org.knime.knip.clump.dt.jdt.JDTPoint;
import org.knime.knip.clump.dt.jdt.Triangle;
import org.knime.knip.clump.graph.Edge;

/**
 * 
 * @author Schlegel
 *
 */
public class MyDelaunayTriangulation
implements UnaryOperation<Collection<long[]>, Collection<Pair<Point, Point>>>{

	@Override
	public Collection<Pair<Point, Point>> compute(Collection<long[]> arg0,
			Collection<Pair<Point, Point>> arg1) {
		
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
	public UnaryOperation<Collection<long[]>, Collection<Pair<Point, Point>>> copy() {
		return new MyDelaunayTriangulation();
	}
	
    private Collection<Pair<Point, Point>> removeDuplicates(Collection<Pair<Point, Point>> list){
    	final Collection<Pair<Point, Point>> out = new LinkedList<Pair<Point, Point>>();
    	for(Pair<Point, Point> e: list){
    		if( !out.contains(e ) )
    			out.add( e );
    	}
    	return out;
    }

	
	private List<JDTPoint> toPoint(Collection<long[]> points){
		List<JDTPoint> out = new ArrayList<JDTPoint>( points.size() );
		for(long[] p: points)
			out.add( new JDTPoint(p[0], p[1]) );
		return out;
	}
}
