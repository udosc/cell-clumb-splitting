package org.knime.knip.clump.ops;

import ij.ImagePlus;
import ij.process.FloatProcessor;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class ConvertToImagePlus<T extends RealType<T>> 
	implements UnaryOperation<Img<T>, ImagePlus>{

	@Override
	public ImagePlus compute(Img<T> input, ImagePlus output) {
		return output = new ImagePlus("", 
				new FloatProcessor(
						(int)input.dimension(0), 
						(int)input.dimension(1), 
						toFloatArray(input)));
	}

	@Override
	public UnaryOperation<Img<T>, ImagePlus> copy() {
		return new ConvertToImagePlus<T>();
	}

	private float[] toFloatArray( RandomAccessibleInterval<T> input ){
		float[] out = new float[ (int) (input.dimension(0) * input.dimension(1)) ];
		int i = 0;
		Cursor<T> cursor = Views.iterable(input).cursor();
		while( cursor.hasNext() && i < out.length){
			out[ i++ ] = cursor.next().getRealFloat();
		}
		return out;
	}
}
