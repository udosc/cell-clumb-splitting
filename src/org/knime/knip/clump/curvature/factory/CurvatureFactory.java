package org.knime.knip.clump.curvature.factory;

import net.imglib2.IterableInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.clump.contour.Contour;

public interface CurvatureFactory<T extends RealType<T>> {
	
	T getType();

	Img<T> createCurvatureImg(Contour contour);
	
	IterableInterval<T> getPointSampleList(Contour contour);
	
}
