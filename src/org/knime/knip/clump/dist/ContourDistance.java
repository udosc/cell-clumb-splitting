package org.knime.knip.clump.dist;

import org.knime.knip.clump.contour.Contour;

import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.numeric.RealType;

public interface ContourDistance<T extends RealType<T>> 
	extends BinaryOperation<Contour, Contour, T> {
	
	T getType();

}
