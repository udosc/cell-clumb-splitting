package org.knime.knip.clump.dist.contour;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RealRandomAccess;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.knime.knip.clump.contour.Contour;
import org.knime.knip.clump.distance.DFTDistance;
import org.knime.knip.core.algorithm.InplaceFFT;
import org.knime.knip.core.data.algebra.Complex;

/**
 * 
 * @author Udo
 *
 * @param <T>
 */
public class MinimalContourDistance<T extends RealType<T> & NativeType<T>> 
	implements ContourDistance<T>{
		
	private final int m_numberOfDesc;
	
	private final T m_type;
	
	private final List<Contour> m_templates;
	
	private final List<Complex[]> m_descriptor;
	
	private final boolean m_normalize;
	
	public MinimalContourDistance(List<Contour> templates, T type, int numberOfDesc, boolean normalize){
		m_numberOfDesc = numberOfDesc;
		m_type = type;
		m_templates = templates;
		m_normalize = normalize;
		m_descriptor = new ArrayList<Complex[]>( templates.size() );
		for(Contour c: templates){
			m_descriptor.add( m_normalize ? createScaleNormalizedCoefficents(c, m_type.createVariable()) :
				createCoefficent(c, m_type.createVariable()));
		}
	}


	@Override
	public T compute(Contour arg0, T arg2) {
		double min = Double.MAX_VALUE;
		for( Complex[] c : m_descriptor){
			final double actual = new DFTDistance<T>( m_numberOfDesc, m_type ).compute(
					c, 
					m_normalize ? createScaleNormalizedCoefficents(arg0, m_type.createVariable()) : createCoefficent(arg0, m_type.createVariable()), 
					m_type.createVariable()).getRealDouble(); 
			if ( actual < min )
				min = actual;
		}
		arg2.setReal( min );
		return arg2;
	}



	@Override
	public UnaryOperation<Contour, T> copy() {
		return new MinimalContourDistance<T>(m_templates , m_type, m_numberOfDesc, m_normalize);
	}
	
	private Complex[] createScaleNormalizedCoefficents(Contour contour, T type){
	    final Complex[] res = InplaceFFT.fft( createCoefficent(contour, type.createVariable()) );
        final double dcMagnitude = res[0].getMagnitude();
         
        final Complex[] descriptor = new Complex[ m_numberOfDesc - 1 ];
        for (int t = 1; t < m_numberOfDesc; t++) {
           descriptor[t - 1] = new Complex( res[t].re() / dcMagnitude, res[t].im() / dcMagnitude );
        }
        
        return descriptor;
	}
	
	private Complex[] createCoefficent(Contour contour, T type){
		RealRandomAccess<T> rra = Views.interpolate( Contour.asRandomAccessibleInterval(contour, type),
				new NLinearInterpolatorFactory<T>()).realRandomAccess();
		
		final double step = (contour.size() - 1.0d)/ 
				(double) m_numberOfDesc ;
		
		final Complex[] out = new Complex[ m_numberOfDesc ];
		//Working with 1-dimensional data so fixing dim 1 to 0
//		rra.setPosition(0, 1);
		for(int i = 0; i < m_numberOfDesc; i++){
			rra.setPosition((i*step), 0);
			out[i] = new Complex(rra.get().getRealDouble(), rra.get().getImaginaryDouble());
		}
		
		return out;
	}


	@Override
	public T getType() {
		return m_type.createVariable();
	}


	@Override
	public List<Contour> getTemplates() {
		return m_templates;
	}

}
