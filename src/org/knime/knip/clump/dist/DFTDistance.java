package org.knime.knip.clump.dist;

import net.imglib2.RealRandomAccess;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.knime.knip.clump.contour.Contour;
import org.knime.knip.clump.curvature.KCosineCurvature;
import org.knime.knip.core.algorithm.InplaceFFT;
import org.knime.knip.core.data.algebra.Complex;

/**
 * 
 * @author Udo
 *
 * @param <T>
 */
public class DFTDistance<T extends RealType<T> & NativeType<T>> 
	implements ContourDistance<T>{
		
	private final int m_numberOfDesc;
	
	private final T m_type;
	
	public DFTDistance(T type, int numberOfDesc){
		m_numberOfDesc = numberOfDesc;
		m_type = type;
	}


//	public T compute(ShapeDescription<T> inputA, Img<T> inputB, T output) {
//		UnaryOperation<Img<T>, Complex[]> op = 
//				new FourierShapeDescription<T>();
//		BinaryOperation<Complex, Complex, Double> complexDist = new ComplexDistance(m_dist);
//		Complex[] fc0 = op.compute(inputA.getImg(), new Complex[m_numberOfDesc]);
//		Complex[] fc1 = op.compute(inputB, new Complex[m_numberOfDesc]);
//		double res = 0.0d;
//		for(int i = 0; i < fc0.length; i++){
//			res += complexDist.compute(
//					fc0[i], 
//					fc1[i], 
//					new Double(0.0d));
//		}
//		
//		output.setReal(res /= fc0.length);
//		return output;
//	}



	@Override
	public T compute(Contour arg0, Contour arg1, T arg2) {
	    final Complex[] res0 = InplaceFFT.fft( createCoefficent(arg0, arg2.createVariable()) );
	    final Complex[] res1 = InplaceFFT.fft( createCoefficent(arg1, arg2.createVariable()) );
        double dcMagnitude0 = res0[0].getMagnitude();
        double dcMagnitude1 = res1[1].getMagnitude();
         
        
//        out = new Complex[ transformed.length / 2 ];
//        for (int t = 1; t <= out.length; t++) {
//            out[t - 1] = 
//            		new Complex(
//            				transformed[t].re() / dcMagnitude, 
//            				transformed[t].im() / dcMagnitude);
////            System.out.println(t-1 + ": " + out[t - 1]);
//        }
        Complex[] descriptor0 = new Complex[ m_numberOfDesc - 1 ];
        Complex[] descriptor1 = new Complex[ m_numberOfDesc - 1 ];
        for (int t = 1; t < m_numberOfDesc; t++) {
           descriptor0[t - 1] = new Complex( res0[t].re() / dcMagnitude0, res0[t].im() / dcMagnitude0 );
           descriptor1[t - 1] = new Complex( res1[t].re() / dcMagnitude1, res1[t].im() / dcMagnitude1 );
        }
		
		return new ComplexDistance<T>( arg2 ).compute(descriptor0, descriptor1, arg2);
	}



	@Override
	public BinaryOperation<Contour, Contour, T> copy() {
		return new DFTDistance<T>( m_type, m_numberOfDesc );
	}
	
	private Complex[] createCoefficent(Contour contour, T type){
		RealRandomAccess<T> rra = Views.interpolate( new KCosineCurvature<T>( type.createVariable(), 5).createCurvatureImg(contour),
				new NLinearInterpolatorFactory<T>()).realRandomAccess();
		
		final double step = (contour.size() - 1.0d)/ 
				(double) m_numberOfDesc ;
		
		final Complex[] out = new Complex[ m_numberOfDesc ];
		//Working with 1-dimensional data so fixing dim 1 to 0
//		rra.setPosition(0, 1);
		for(int i = 0; i < m_numberOfDesc; i++){
			rra.setPosition((i*step), 0);
			out[i] = new Complex(rra.get().getRealDouble(), 0.0d);
		}
		
		return out;
	}


	@Override
	public T getType() {
		return m_type.createVariable();
	}

}
