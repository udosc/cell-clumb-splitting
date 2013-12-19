package org.knime.knip.clump.curvature;

import net.imglib2.Interval;
import net.imglib2.Point;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RealPositionable;
import net.imglib2.collection.PointSampleList;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.knime.knip.clump.boundary.ShapeFeature;
import org.knime.knip.clump.contour.Contour;

public class CurvatureSampleList<T extends RealType<T>> extends PointSampleList<T>{
	
	
	CurvatureSampleList(Img<T> img, Contour contour){
		super( 2 );
		RandomAccess<T> ra = img.randomAccess();
		for( int i = 0; i < contour.dimension(0); i++){
			add(new Point( contour.get(i) ), ra.get());
		}
	}

	

}
