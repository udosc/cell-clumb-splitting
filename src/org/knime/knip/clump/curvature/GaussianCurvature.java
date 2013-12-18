package org.knime.knip.clump.curvature;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

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
	public ShapeFeature<T> createCurvaureImg(Contour contour) {
		final Img<T> out = 
				new ArrayImgFactory<T>().create(new long[]{ contour.length() }, m_type);
		
		
		final RandomAccess<T> d1xRandomAccess = new DirectConvolver<DoubleType, T, T>().compute(
				contour.getCoordinates(0), 
				DiscretHelpers.createFirstDerivation1D( m_supportRadius, m_type ), 
				ImgUtils.createEmptyCopy(out)).randomAccess();
		
		final RandomAccess<T> d1yRandomAccess = new DirectConvolver<DoubleType, T, T>().compute(
				contour.getCoordinates(1), 
				DiscretHelpers.createFirstDerivation1D( m_supportRadius, m_type ), 
				ImgUtils.createEmptyCopy(out)).randomAccess();
		
		final RandomAccess<T> d2xRandomAccess = new DirectConvolver<DoubleType, T, T>().compute(
				contour.getCoordinates(0), 
				DiscretHelpers.createSecondDerivation1D( m_supportRadius, m_type ), 
				ImgUtils.createEmptyCopy(out)).randomAccess();
		
		final RandomAccess<T> d2yRandomAccess = new DirectConvolver<DoubleType, T, T>().compute(
				contour.getCoordinates(1), 
				DiscretHelpers.createSecondDerivation1D( m_supportRadius, m_type ), 
				ImgUtils.createEmptyCopy(out)).randomAccess();
		
		final Cursor<T> cOut = out.cursor();
		for(int i = 0; i < out.dimension(0); i++){
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
			cOut.fwd();
			cOut.get().setReal( t / r  );
		}
		
		return new CurvatureImg<T>(out, contour);
	}
}
