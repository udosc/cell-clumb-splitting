package org.knime.knip.clump.util;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.operation.iterable.unary.Mean;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.knime.knip.core.algorithm.convolvers.filter.linear.DerivativeOfGaussian;
import org.knime.knip.core.algorithm.convolvers.filter.linear.LaplacianOfGaussian;

public class DiscretHelpers {
	
	private DiscretHelpers(){ }
	
	public static <T extends RealType<T> & NativeType<T>> Img<T> createFirstDerivation1D(int support, T type){
		final Img<T> out = new ArrayImgFactory<T>().create(
				new long[]{support * 2 + 1 }, type.createVariable());
		
		IntervalView<DoubleType> view = 
				Views.hyperSlice(
						new DerivativeOfGaussian(support, 0.75d * 2 * Math.PI, 1.0d , 1),
						1,
						support);
		
		final double mean = new Mean<DoubleType, T>().compute(view.iterator(), type.createVariable()).getRealDouble();
		final Cursor<DoubleType> cursor = view.cursor();
		Cursor<T> outC = out.cursor();
		while( outC.hasNext() ){
			cursor.fwd();
			outC.next().setReal( cursor.get().getRealDouble() - mean );
		}
		return out;
	}
	
	public static <T extends RealType<T> & NativeType<T>> Img<T> createSecondDerivation1D(int support, T type){
		final Img<T> out = new ArrayImgFactory<T>().create(
				new long[]{support * 2 + 1}, type.createVariable());
		
		IntervalView<DoubleType> view = 
				Views.hyperSlice(
						new LaplacianOfGaussian(support, 1.0d),
						1,
						support);
		
		final double mean = new Mean<DoubleType, T>().compute(view.iterator(), type.createVariable()).getRealDouble();
		final Cursor<DoubleType> cursor = view.cursor();
		Cursor<T> outC = out.cursor();
		while( outC.hasNext() ){
			cursor.fwd();
			outC.next().setReal( cursor.get().getRealDouble() - mean);
		}
		return out;
	}

}
