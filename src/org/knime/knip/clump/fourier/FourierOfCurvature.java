package org.knime.knip.clump.fourier;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccess;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.knime.knip.clump.util.MyUtils;
import org.knime.knip.core.algorithm.InplaceFFT;
import org.knime.knip.core.data.algebra.Complex;

/**
 * 
 * @author Schlegel
 *
 * @param <T>
 */
public class FourierOfCurvature<T extends RealType<T> >{
		
		
	private final Complex[] m_descriptor;
		
	private final double m_magnitude;

	private final int m_numberOfDescriptors;
	
	public FourierOfCurvature(RandomAccessibleInterval<T> curvature){
		this(curvature, (int)Math.pow(2, 
					Math.floor( Math.log( curvature.dimension(0) ) / Math.log(2)    )));
	}
	
	public FourierOfCurvature(RandomAccessibleInterval<T> curvature, int nDesc){
		//Computes the number of descriptors for the FFT
			
		m_descriptor = new Complex[ nDesc ];
		
		m_numberOfDescriptors = nDesc;
		
		final RealRandomAccess<T> rra = Views.interpolate( curvature,
				new NLinearInterpolatorFactory<T>()).realRandomAccess();
		
		Complex[] complex = new Complex[ nDesc ];
		
		final double step = (MyUtils.numElements(curvature) - 1.0d)/ (double) nDesc ;
		
		//Working with 1-dimensional data so fixing dim 1 to 0
//				rra.setPosition(0, 1);
		for(int i = 0; i < nDesc; i++){
			rra.setPosition((i*step), 0);
			complex[i] = new Complex( rra.get().getRealDouble(), 0.0d);
		}
		
        final Complex[] transformed = InplaceFFT.fft( complex );
        m_magnitude = transformed[0].getMagnitude();
                 
        for( int i = 0; i < complex.length; i++){
        	m_descriptor[i] = new Complex(transformed[i].re(), transformed[i].im());
        }

	}
	
	
	
	public double[] lowPass(int cutOff){
		
		final double[] out = new double[ m_descriptor.length ];
		
		final Complex[] desc = getDescriptors(cutOff);
        
		// inverse fast fourier transformation
        final Complex[] res = InplaceFFT.ifft( desc );
        for (int i = 0; i < res.length; i++) {
            out[i] = res[i].re();
        }
        
        return out;
	}
	
	public double[] getMagnitudes(int cutOff, boolean invariant){
		final double[] out = new double[ cutOff ];
		final Complex[] complex = getDescriptors(cutOff +  1);
		final double magnitude = invariant ? complex[0].getMagnitude() : 1.0d;
		for(int i = 1; i <= cutOff; i++){
			out[i-1] = complex[i].getMagnitude() / magnitude;
		}
		return out;
	}
	
	//TODO
	public double[] getInvariantDescriptors(int cutOff){
		double[] magnitudes = getMagnitudes(cutOff, false);
		Complex[] desc = getDescriptors(cutOff, false);
		
//		for(int i = 0; i < cutOff; i++){
//			magnitudes[0] *= Math.exp(arg0)
//		}
		
		return null;
		
	}
	
	public Complex[] getDescriptors(int cutOff, boolean invariant) {
		final int N = m_descriptor.length;

		final Complex[] out = new Complex[ cutOff ];
		
		// delete frequencies
		for(int i = 0; i < cutOff; i++){
			final Complex element = m_descriptor[i+1]; 
			out[i] = i < cutOff ? 
					(invariant ? new Complex( element.re() / m_magnitude, element.im() / m_magnitude): element ): 
						new Complex(0.0d , 0.0d);
		}

		return out;
		
	}

	public Complex[] getDescriptors(int cutOff) {
		final int N = m_descriptor.length;

		final Complex[] out = new Complex[ N ];
		
		// delete frequencies
		for (int i = 0; i < N; i++) {
			out[i] = i < cutOff ? m_descriptor[i] : new Complex(0.0d , 0.0d);
		}

		return out;
		
	}

	public int getNumberOfDescriptors(){
		return m_numberOfDescriptors;
	}

	public double getMagnitude(){
		return m_magnitude;
	}
}
