package org.knime.knip.clump.boundary;

import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * 
 * @author Schlegel
 *
 * @param <T>
 */
public interface ShapeDescription<T extends RealType<T> & NativeType<T>> {
	
	T getValueOf(long[] point);
	
	Img<T> getValues(long[] start, long[] destination);
	
	double[] getValues(int start, int end);
	
	Img<T> getImg();
	
	T getType();
	
	int getSize();
	
	Contour getContour();
}
