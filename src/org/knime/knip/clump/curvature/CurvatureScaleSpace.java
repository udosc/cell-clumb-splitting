package org.knime.knip.clump.curvature;

import java.util.List;
import java.util.concurrent.ExecutorService;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

public class CurvatureScaleSpace<T extends RealType<T> & NativeType<T>>  {
	
	private int m_count;
	
	public CurvatureScaleSpace(int count){
		m_count = count;
	}

	public Img<BitType> compute(Curvature<T> curvature, ExecutorService exectutor){
		
//		Curvature<DoubleType> curvature = 
//				new Curvature<DoubleType>(
//						new BinaryFactory(rai).createContour(), 5, new DoubleType());
		
		Img<BitType> output = new ArrayImgFactory<BitType>().create(
				new long[]{ curvature.getSize(), m_count}, new BitType());
		

		RandomAccess<BitType> ra = output.randomAccess();
		for( int i = 0; i < output.dimension(1); i++){
//			Img<BitType> res = ImgUtils.createEmptyCopy(curvature.getImg(), new BitType());
			
			List<Integer> points = curvature.gaussian(i+1, exectutor).getZeroCrossings();
			
//			RandomAccess<BitType> rra = Views.hyperSlice( res, 1, 0).randomAccess();
			for( Integer p: points){
				ra.setPosition(p, 0);
				ra.setPosition(m_count - i - 1, 1);
				ra.get().set( true );
			}
			
		}
		
		
		return output;
	}
}
