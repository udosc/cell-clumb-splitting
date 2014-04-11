package org.knime.knip.clump.dt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.imglib2.Point;
import net.imglib2.ops.operation.UnaryOperation;

import org.knime.core.util.Pair;

import delaunay.DelaunayTriangulation;
import delaunay.Pnt;
import delaunay.Simplex;

/**
 * 
 * @author Udo
 *
 */
public class ImglibDelaunayTriangulation
	implements UnaryOperation<Collection<long[]>, Collection<Pair<Point, Point>>>{

	@Override
	public Collection<Pair<Point, Point>> compute(Collection<long[]> arg0,
			Collection<Pair<Point, Point>> arg1) {
		
		if( arg0.size() > 3 ){
			final List<Pnt> res = toPnt( arg0 );
			final DelaunayTriangulation dt = new DelaunayTriangulation( 
					new Simplex( new Pnt[]{res.get(0), res.get(1), res.get(2) }));
			for(int i = 3; i < res.size(); i++)
				dt.delaunayPlace(res.get(i));
			dt.printStuff();
		}
			
		return arg1;
	}

	@Override
	public UnaryOperation<Collection<long[]>, Collection<Pair<Point, Point>>> copy() {
		// TODO Auto-generated method stub
		return null;
	}


	private List<Pnt> toPnt(Collection<long[]> points){
		List<Pnt> out = new ArrayList<Pnt>( points.size() );
		for(long[] pos: points)
			out.add( new Pnt(pos[0], pos[1]));
		return out;
	}
}
