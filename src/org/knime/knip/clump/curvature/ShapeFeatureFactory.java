package org.knime.knip.clump.curvature;

import net.imglib2.collection.PointSampleList;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.clump.boundary.ShapeFeature;
import org.knime.knip.clump.contour.Contour;

public interface ShapeFeatureFactory<T extends RealType<T>> {
	

	PointSampleList<T> createCurvatureImg(Contour contour);
	
}
