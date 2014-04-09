package org.knime.knip.clump.curvature;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.outofbounds.OutOfBoundsPeriodicFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.clump.contour.Contour;
import org.knime.knip.clump.dist.MinRAIDistance;
import org.knime.knip.clump.dist.contour.ContourDistance;
import org.knime.knip.core.ops.filters.GaussNativeTypeOp;
import org.knime.knip.core.util.ImgUtils;

/**
 * 
 * @author Udo Schlegel
 *
 */
public class CurvatureDistance<T extends RealType<T> & NativeType<T>> 
	implements ContourDistance<T>{

	private final int m_step;
	
	private final CurvatureFactory<T> m_factory;
	
	private final ExecutorService m_exec;
	
	private final double m_sigma;
	
	private final List<RandomAccessibleInterval<T>> m_templates;
	
	private final List<Contour> m_contour;
	
	public CurvatureDistance(List<Contour> templates, CurvatureFactory<T> factory, int step, ExecutorService exec, double sigma){
		m_step = step;
		m_factory = factory;
		m_exec = exec;
		m_sigma = sigma;
		m_contour = templates;
		m_templates = new ArrayList<RandomAccessibleInterval<T>>( templates.size() );
		for(Contour c: templates){
			final Img<T >res = m_factory.createCurvatureImg(c);
			m_templates.add( new GaussNativeTypeOp<T, RandomAccessibleInterval<T>>(m_exec, 
					new double[]{ m_sigma, 0.0d}, 
					new OutOfBoundsPeriodicFactory<T, RandomAccessibleInterval<T>>())
					.compute(res, ImgUtils.createEmptyCopy( res )));
		}
	}
	
	@Override
	public UnaryOperation<Contour, T> copy() {
		return new CurvatureDistance<T>(m_contour, m_factory, m_step, m_exec, m_sigma);
	}

	@Override
	public T compute(Contour contourA, T output) {
		
		final Img<T> res = m_factory.createCurvatureImg(contourA);
		RandomAccessibleInterval<T> inputA = new GaussNativeTypeOp<T, RandomAccessibleInterval<T>>(m_exec, 
				new double[]{ m_sigma, 0.0d}, 
				new OutOfBoundsPeriodicFactory<T, RandomAccessibleInterval<T>>())
				.compute(res, ImgUtils.createEmptyCopy( res ));
		
		double min  = Double.MAX_VALUE;
		
		for(RandomAccessibleInterval<T> rai: m_templates){
			double actual = new MinRAIDistance<T>(1).compute(rai, inputA, output.createVariable()).getRealDouble();
			if( actual < min)
				min = actual;
		}

		output.setReal( min );
		return output;
	}

	
	@Override
	public T getType() {
		return m_factory.getType();
	}

	@Override
	public List<Contour> getTemplates() {
		return m_contour;
	}

}
