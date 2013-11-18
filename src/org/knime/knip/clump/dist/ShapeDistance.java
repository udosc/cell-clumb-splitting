package org.knime.knip.clump.dist;

import net.imglib2.img.Img;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.apache.commons.math3.ml.distance.DistanceMeasure;

public interface ShapeDistance<T extends RealType<T> & NativeType<T>> 
	extends BinaryOperation<Img<T>, Img<T>, T>{
	
	DistanceMeasure getDistanceMeasure();

}
