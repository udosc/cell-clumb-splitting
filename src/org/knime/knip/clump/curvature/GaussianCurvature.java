package org.knime.knip.clump.curvature;

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.collection.PointSampleList;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.operation.iterable.unary.Mean;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.knime.knip.clump.boundary.ShapeFeature;
import org.knime.knip.clump.contour.Contour;
import org.knime.knip.clump.util.DiscretHelpers;
import org.knime.knip.core.algorithm.convolvers.DirectConvolver;
import org.knime.knip.core.util.ImgUtils;

public class GaussianCurvature< T extends RealType<T> & NativeType<T>> 
implements ShapeFeatureFactory<T> {
	
	private final T m_type;
	
	private final int m_supportRadius;
	
	public GaussianCurvature(T type, int supportRadius){
		m_type = type.createVariable();
		m_supportRadius = supportRadius;
	}

	@Override
	public PointSampleList<T> createCurvatureImg(Contour contour) {
		final PointSampleList<T> out = new PointSampleList<T>(2);
		final long size = contour.size();
		
		final RandomAccess<T> d1xRandomAccess = new DirectConvolver<DoubleType, T, T>().compute(
				contour.getCoordinates(0), 
				DiscretHelpers.createFirstDerivation1D( m_supportRadius, m_type ), 
				create1DImg(size)).randomAccess();
		
		final RandomAccess<T> d1yRandomAccess = new DirectConvolver<DoubleType, T, T>().compute(
				contour.getCoordinates(1), 
				DiscretHelpers.createFirstDerivation1D( m_supportRadius, m_type ), 
				create1DImg(size)).randomAccess();
		
		final RandomAccessibleInterval<T> d2Xrai = new DirectConvolver<DoubleType, T, T>().compute(
				contour.getCoordinates(0), 
				DiscretHelpers.createSecondDerivation1D( m_supportRadius, m_type ), 
				create1DImg(size));
		
		final RandomAccessibleInterval<T> d2Yrai = new DirectConvolver<DoubleType, T, T>().compute(
				contour.getCoordinates(1), 
				DiscretHelpers.createSecondDerivation1D( m_supportRadius, m_type ), 
				create1DImg(size));
		
//		RandomAccessibleInterval<T> temp1 = new DirectConvolver<DoubleType, T, T>().compute(
//				contour.getCoordinates(0), 
//				DiscretHelpers.createFirstDerivation1D( m_supportRadius, m_type ), 
//				create1DImg(size));
		
		double mean = new Mean<T, T>().compute(Views.iterable(d2Xrai).iterator(), m_type.createVariable()).getRealDouble();
		Cursor<T> ctmp = Views.iterable( d2Xrai ).cursor();
		while( ctmp.hasNext() ){
			ctmp.fwd();
			ctmp.get().setReal( ctmp.get().getRealDouble() - mean );
		}
		

		
		mean = new Mean<T, T>().compute(Views.iterable(d2Yrai).iterator(), m_type.createVariable()).getRealDouble();
		System.out.println( mean );
		
		mean = new Mean<T, T>().compute(Views.iterable(d2Xrai).iterator(), m_type.createVariable()).getRealDouble();
		System.out.println( mean );
		
		RandomAccess<T> d2xRandomAccess = d2Xrai.randomAccess();
		RandomAccess<T> d2yRandomAccess = d2Yrai.randomAccess();
		
		for(int i = 0; i < size; i++){
			d1xRandomAccess.setPosition(i, 0);
			d2xRandomAccess.setPosition(i, 0);
			d1yRandomAccess.setPosition(i, 0);
			d2yRandomAccess.setPosition(i, 0);
			final double d1x = d1xRandomAccess.get().getRealDouble();
			final double d1y = d1yRandomAccess.get().getRealDouble();
			final double d2x = d2xRandomAccess.get().getRealDouble();
			final double d2y = d2yRandomAccess.get().getRealDouble();
			final double t = ((d1x * d2y) - (d2x * d1y));
			final double r = Math.pow(( d1x * d1x ) +  (d1y * d1y), 1.5d);
			
			final T sample = m_type.createVariable();
			sample.setReal( t / r);
			out.add(new Point( contour.get(i)), sample);
		}
		
		return out;
	}
	
	private Img<T> create1DImg(long size){
		return new ArrayImgFactory<T>().create(new long[]{ size }, m_type);
	}
}
