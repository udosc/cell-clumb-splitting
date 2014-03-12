package org.knime.knip.clump.dt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.imglib2.Point;
import net.imglib2.ops.operation.UnaryOperation;

import org.knime.core.util.Pair;

public class MyDelaunayTriangulation
implements UnaryOperation<Collection<long[]>, Collection<Pair<Point, Point>>>{

	@Override
	public Collection<Pair<Point, Point>> compute(Collection<long[]> arg0,
			Collection<Pair<Point, Point>> arg1) {
		
		org.knime.knip.clump.dt.DelaunayTriangulation dt = new org.knime.knip.clump.dt.DelaunayTriangulation( toPoint(arg0 ));
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
		return arg1;
	}

	@Override
	public UnaryOperation<Collection<long[]>, Collection<Pair<Point, Point>>> copy() {
		// TODO Auto-generated method stub
		return new MyDelaunayTriangulation();
	}
	
	private List<org.knime.knip.clump.dt.Point> toPoint(Collection<long[]> points){
		List<org.knime.knip.clump.dt.Point> out = new ArrayList<org.knime.knip.clump.dt.Point>( points.size() );
		for(long[] p: points)
			out.add( new org.knime.knip.clump.dt.Point(p[0], p[1]) );
		return out;
	}
}
