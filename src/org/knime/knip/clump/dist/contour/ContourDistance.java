package org.knime.knip.clump.dist.contour;

import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.clump.contour.Contour;

public interface ContourDistance<T extends RealType<T>> 
	extends UnaryOperation<Contour, T> {
	
	T getType();

}
