package org.knime.knip.clump.dist;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.ops.operation.iterable.unary.Max;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.knime.knip.clump.util.ClumpUtils;
import org.knime.knip.core.algorithm.convolvers.DirectConvolver;
import org.knime.knip.core.util.ImgUtils;

/**
 * 
 * @author Udo Schlegel
 *
 */
public class CrossCorrelationSimilarity<T extends RealType<T> & NativeType<T>> 
	implements ShapeDistance<T>, DistanceMeasure{

	private static final long serialVersionUID = -3340573045806703033L;


	@Override
	public T compute(Img<T> template,
			Img<T> sample, T output) {
		final Img<T> res = ImgUtils.createEmptyCopy(template);
		
//		final int size = ClumpUtils.numElements( sample );
//		Cursor<T> c = sample.cursor();
//		while( c.hasNext() ){
//			c.fwd();
//			c.get().setReal( c.get().getRealDouble() / size );
//		}

		
//		final Img<T> normalized = ImgUtils.createEmptyCopy(sample);
//		final double elements = ClumpUtils.numElements(normalized);
//		Cursor<T> c = Views.iterable(normalized).cursor();
//		while( c.hasNext() ){
//			c.fwd();
//			c.get().setReal( c.get().getRealDouble() / elements );
//		}
				
		//Rotate the sample to perform a cross correlation!
		new DirectConvolver<T, T, T>().compute(
				Views.extendZero(template), 
				Views.zeroMin(Views.rotate(Views.rotate(sample, 0, 1), 0, 1)), 
				res);
		
		new Max<T, T>().compute(res.iterator(), output);
//		output = similarity2Distance(output);
		return output;
	}

	@Override
	public BinaryOperation<Img<T>, Img<T>, T> copy() {
		return new CrossCorrelationSimilarity<T>();
	}
	
    private T similarity2Distance(T value) {
    	final double res = value.getRealDouble(); 
        value.setReal( (1.0 - ( res / (1.0 + res))));
        return value;
    }

	@Override
	public double compute(double[] arg0, double[] arg1) {
		final Img<DoubleType> img1 = new ArrayImgFactory<DoubleType>().
				create(new long[]{ arg0.length, 1}, new DoubleType());
		Cursor<DoubleType> c = img1.cursor();
		int i = 0;
		while(c.hasNext()){
			c.next().setReal( arg0[i++]);
		}
		
		final Img<DoubleType> img2 = new ArrayImgFactory<DoubleType>().
				create(new long[]{ arg1.length, 1}, new DoubleType());
		c = img2.cursor();
		i = 0;
		while(c.hasNext()){
			c.next().setReal( arg1[i++]);
		}

		//TODO
		DoubleType res = new CrossCorrelationSimilarity<DoubleType>().compute(img1, img2, new DoubleType());
		return res.getRealDouble();
	}

	@Override
	public DistanceMeasure getDistanceMeasure() {
		return new EuclideanDistance();
	}
	
}
