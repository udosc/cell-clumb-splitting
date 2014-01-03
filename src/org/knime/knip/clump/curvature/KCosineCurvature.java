package org.knime.knip.clump.curvature;

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.collection.PointSampleList;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.clump.boundary.ShapeFeature;
import org.knime.knip.clump.contour.Contour;

public class KCosineCurvature<T extends RealType<T> & NativeType<T>> 
implements ShapeFeatureFactory<T>{
	
	private T m_type;
	
	private int m_order;
	
	public KCosineCurvature(T type, int order){
		m_type = type.createVariable();
		m_order = order;
	}
	
	@Override
	public PointSampleList<T> createCurvatureImg(Contour contour) {
		final PointSampleList<T> out = new PointSampleList<T>(2);

		
		for(int i = 0; i < contour.length(); i++){
			final T t = m_type.createVariable();
			final long[] pos = contour.get(i);
			t.setReal( calculateCosine(
					pos,
					contour.get(i - m_order),
					contour.get(i + m_order)) );
			out.add(new Point(pos) , t);
		}
		
		return out;
	}
	
	private double calculateCosine(long[] center, long[] p1, long[] p2){
		double scalar = 0.0d;
		
		for(int i = 0; i < center.length; i++){		
			scalar += ( p1[i] - center[i] ) * ( p2[i] - center[i]  );
		}
		
		double d = scalar / 
				(distance(center, p1) * distance(center, p2));

		
		return ( 1 - Math.abs( d ) ) * 
				Math.signum( crossProduct(center, p1, p2));
	}

	
	//See cormen - algorithm page 1046/46
	//result > 0: p0p1 is clockwise from p0p2
	//result < 0: p0p1 is anticlockwise from p0p2
	private static double crossProduct(long[] p0, long[] p1, long[] p2){
		return (( p1[0] - p0[0]) * ( p2[1] - p0[1])) -
				(( p2[0] - p0[0]) * (p1[1] - p0[1]));
	}
		
	
	private static double distance(long[] p1, long[] p2){
		assert p1.length == p2.length;
		double sum = 0.0d;
		for (int i = 0; i < p1.length; i++) {
			final double delta = p1[i] - p2[i];
			sum += delta * delta;
		}
		return Math.sqrt(sum);
	}

}
