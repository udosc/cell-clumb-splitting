package org.knime.knip.clump.dist.contour;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccess;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.knime.knip.clump.contour.Contour;
import org.knime.knip.clump.curvature.factory.CurvatureFactory;
import org.knime.knip.clump.distance.ComplexDistance;
import org.knime.knip.clump.fourier.FourierOfCurvature;
import org.knime.knip.core.data.algebra.Complex;

public class CurvatureFourier<T extends RealType<T>> 
implements ContourDistance<T>{

	
	private final CurvatureFactory<T> m_factory;
	
	private final List<Complex[]> m_descriptor;
	
	private final List<double[]> m_magnitude;
	
	private final List<Contour> m_contour;
	
	private final T m_type;
	
	private final int m_numberOfDesc;
	
	private final ComplexDistance<T> m_distance;
	
	
	public CurvatureFourier(List<Contour> templates, CurvatureFactory<T> factory, int numberOfDesc){
		m_numberOfDesc = numberOfDesc;
		m_factory = factory;
		m_contour = templates;
		m_descriptor = new ArrayList<Complex[]>( templates.size() );
		m_magnitude = new ArrayList<double[]>( templates.size() );
		m_type = factory.getType();
		m_distance = new ComplexDistance<T>();
//		m_numberOfPoints = numberOfPoints;
		for(Contour c: templates){
//			m_descriptor.add( createCoefficent( m_factory.createCurvatureImg(c), factory.getType()));
			m_descriptor.add( new FourierOfCurvature<T>( m_factory.createCurvatureImg(c) ).getDescriptors(m_numberOfDesc, true) );
//			m_descriptor.add( new FourierOfCurvature<T>( m_factory.createCurvatureImg(c) ).getMagnitudes(m_numberOfDesc, true) );
			m_magnitude.add( new FourierOfCurvature<T>( m_factory.createCurvatureImg(c)  ).getMagnitudes(m_numberOfDesc, true) );
		}
	}
	
	@Override
	public T compute(Contour arg0, T arg1) {
		double min = Double.MAX_VALUE;
		double[] ref = new FourierOfCurvature<T>( m_factory.createCurvatureImg(arg0) ).getMagnitudes(m_numberOfDesc, true);
//		Complex[] ref = new FourierOfCurvature<T>( m_factory.createCurvatureImg(arg0) ).getDescriptors(m_numberOfDesc, true);
//		final ComplexArrayDistance<T> distance = new ComplexArrayDistance<T>();
		for( double[] d: m_magnitude){
//			final double actual = distance.compute(
//					c, 
////					createCoefficent( rai, m_type),
//					new FourierOfCurvature<T>( rai ).getDescriptors(m_numberOfDesc),
//					m_type.createVariable()).getRealDouble(); 
			final double actual = distance(d, ref) ;
//			final double actual = m_distance.compute(d, ref, m_type.createVariable()).getRealDouble();
			if ( actual < min )
				min = actual;
		}
		arg1.setReal( min );
		return arg1;
	}

	@Override
	public UnaryOperation<Contour, T> copy() {
		return new CurvatureFourier<T>(m_contour, m_factory, m_numberOfDesc);
	}

	@Override
	public T getType() {
		return m_type.createVariable();
	}
//	
//	private T calculate(Complex[] arg0, Complex[] arg1){
//		
//	}

	private double distance(double[] d1, double[] d2){
		assert d1.length == d2.length : this.getClass() + " differtent vector sizes";
		double sum = 0.0d;
		for (int i = 0; i < d1.length; i++) {
			final double delta = d1[i] - d2[i];
			sum += delta * delta;
		}
		return Math.sqrt(sum);
	}
	
	private Complex[] createCoefficent(RandomAccessibleInterval<T> curvature, T type){
		RealRandomAccess<T> rra = Views.interpolate( curvature,
				new NLinearInterpolatorFactory<T>()).realRandomAccess();
		
		final double step = (curvature.dimension(0) - 1.0d)/ 
				(double) m_numberOfDesc ;
		
		final Complex[] out = new Complex[ m_numberOfDesc ];

		for(int i = 0; i < m_numberOfDesc; i++){
			rra.setPosition((i*step), 0);
			out[i] = new Complex(rra.get().getRealDouble(), 0.0d);
		}
		
		return out;
	}

	@Override
	public List<Contour> getTemplates() {
		return m_contour;
	}
	
}
