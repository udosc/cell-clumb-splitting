package org.knime.knip.clump.boundary;

import org.knime.knip.clump.contour.Contour;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public interface ShapeFeature<T extends RealType<T>> 
	extends RandomAccessibleInterval<T>{

	Contour getContour();
	
	T getType();
}
