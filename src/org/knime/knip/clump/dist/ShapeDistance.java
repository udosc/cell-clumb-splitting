package org.knime.knip.clump.dist;

import net.imglib2.img.Img;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.knime.knip.clump.boundary.Contour;
import org.knime.knip.clump.boundary.ShapeDescription;

public interface ShapeDistance<T extends RealType<T> & NativeType<T>> 
	extends BinaryOperation<ShapeDescription<T>, Img<T>, T>{
	
	DistanceMeasure getDistanceMeasure();

}
