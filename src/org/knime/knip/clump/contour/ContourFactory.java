package org.knime.knip.clump.contour;


import net.imglib2.type.numeric.RealType;

public interface ContourFactory<T extends RealType<T>> {

	Contour createContour();
}
