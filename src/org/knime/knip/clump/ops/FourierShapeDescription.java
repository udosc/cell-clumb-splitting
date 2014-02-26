package org.knime.knip.clump.ops;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccess;
import net.imglib2.img.Img;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.NativeType;
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
public class FourierShapeDescription<T extends RealType<T> > 
	implements UnaryOperation<RandomAccessibleInterval<T>, Complex[]>{
		
		
		

	@Override
	public Complex[] compute(RandomAccessibleInterval<T> curvature, Complex[] out) {
		
		//Computes the number of descriptors for the FFT
		int nDesc = (int)Math.pow(2, 
				Math.ceil( Math.log( curvature.dimension(0) ) / Math.log(2)    )); 
		
		RealRandomAccess<T> rra = Views.interpolate( curvature,
				new NLinearInterpolatorFactory<T>()).realRandomAccess();
		
		Complex[] complex = new Complex[ nDesc ];
		
		final double step = (MyUtils.numElements(curvature) - 1.0d)/ (double) nDesc ;
		
		//Working with 1-dimensional data so fixing dim 1 to 0
//		rra.setPosition(0, 1);
		for(int i = 0; i < nDesc; i++){
			rra.setPosition((i*step), 0);
			complex[i] = new Complex( rra.get().getRealDouble(), 0.0d);
		}
		
        final Complex[] transformed = InplaceFFT.fft( complex );
        double dcMagnitude = transformed[0].getMagnitude();
         
        dcMagnitude = 1.0d;
        
//        out = new Complex[ transformed.length / 2 ];
//        for (int t = 1; t <= out.length; t++) {
//            out[t - 1] = 
//            		new Complex(
//            				transformed[t].re() / dcMagnitude, 
//            				transformed[t].im() / dcMagnitude);
////            System.out.println(t-1 + ": " + out[t - 1]);
//        }
        
        for( int i = 0; i < out.length; i++){
        	out[i] = new Complex(transformed[i].re(), transformed[i].im());
        }
		
        return out;
        
//		Complex[] descriptor = new Complex[256];
//		int n = 0;
//		while( c.hasNext() ){
//			final double res = c.next().getRealDouble();
//			values.add( res );
//			complex[n++] = new Complex( res, 0);
//		}
//		
//		for(int i = n; i < 256; i++)
//			complex[i] = new Complex(0,0);
		

        
//        dcMagnitude = 1.0d;
//        
//
//		
//        Complex[] nDescriptor = new Complex[256];
//        nDescriptor[0] = new Complex(0.0d, 0.0d);
//        for(int i = 0; i < 256; i++){
//        	nDescriptor[i] = i < 32 ? transformed[i] : new Complex(0,0);
//        }
//        transformed = InplaceFFT.ifft(nDescriptor);
//		return null;
	}

	@Override
	public UnaryOperation<RandomAccessibleInterval<T>, Complex[]> copy() {
		return new FourierShapeDescription<T>();
	}

}
