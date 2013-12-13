package org.knime.knip.clump.warp;

import ij.ImagePlus;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.meta.ImgPlus;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.ops.operation.UnaryOutputOperation;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

import org.knime.knip.clump.ops.ConvertToImagePlus;
import org.knime.knip.clump.types.WarpingErrorEnums;
import org.knime.knip.core.util.MiscViews;
import org.knime.knip.imagej2.core.util.IJToImg;
import org.knime.knip.imagej2.core.util.ImgToIJ;

import trainableSegmentation.metrics.ClassificationStatistics;
import trainableSegmentation.metrics.ClusteredWarpingMismatches;
import trainableSegmentation.metrics.RandError;
import trainableSegmentation.metrics.WarpingError;
import trainableSegmentation.metrics.WarpingResults;

/**
 * 
 * @author Schlegel
 *
 */
public class MyWarpingError<T extends RealType<T> & NativeType<T>>
implements BinaryOperation<Img<BitType>, Img<BitType>, Img<T>> {
	
	private WarpingErrorEnums[] m_errors;
	
	private double m_warpingError;
	
	private double m_randError;
	
	private int m_radius;
	
	private T m_type;
	
	//TODO Replace with a getter setter version...
	private ClusteredWarpingMismatches m_stat;
	
	public MyWarpingError(T type, int radius, WarpingErrorEnums... enums){
		m_errors = enums;
		m_radius = radius;
		m_type = type;
	}

	@Override
	public Img<T> compute(Img<BitType> inputA, Img<BitType> inputB,
			Img<T> output) {
				
		final UnaryOutputOperation<ImgPlus<? extends RealType<?>>, ImagePlus> op = 
				new ImgToIJ();
		
		RandError re = new RandError(op.compute(
				new ImgPlus<BitType>(inputA), new ImagePlus()), 
				op.compute(new ImgPlus<BitType>(inputB), new ImagePlus()));
		
		m_randError = re.getMetricValue(0.1d);
		
		WarpingError we = new WarpingError(
//				new ConvertToImagePlus<BitType>().compute(inputA, null),
//				new ConvertToImagePlus<BitType>().compute(inputB, null),
				op.compute(new ImgPlus<BitType>(inputA), new ImagePlus()),
				op.compute(new ImgPlus<BitType>(inputB), new ImagePlus()));
		
//		return new ImagePlusAdapter<UnsignedByteType>(we., factory, type)
		
		int[] res = new int[ m_errors.length ];
		for(int i = 0; i < res.length; i++)
			res[i] = m_errors[i].getValue();

//		final WarpingResults wr = we.getWarpingResults(0.1d, true, true, m_radius);
		final WarpingResults[] wrs = we.simplePointWarp2dMT(0.5d, true, true, m_radius);
//		m_warpingError = wrs[0].warpingError;
		m_warpingError = wrs[0].warpingError;

//		RandomAccess<T> ra = output.randomAccess();
//		for( Point3f p: wr.mismatches){
//			ra.setPosition( new long[]{ (long) p.x, (long) p.y});
//			ra.get().setReal( 1337 );
//		}
		
		
		ImagePlus imagePlus = wrs[0].classifiedMismatches;
		output = new IJToImg<T>(m_type).compute(
				imagePlus, 
				new ArrayImgFactory<T>().create(imagePlus.getDimensions(), m_type));
		
		output = MiscViews.cleanImgPlus( new ImgPlus<T>( output ) );
		
//		output = new ImagePlusAdapter<UnsignedByteType>( 
//				we.getMismatchImage(wrs[0], m_radius),
////				wrs[0].warpedSource,
//				new ArrayImgFactory<UnsignedByteType>(),
//				new UnsignedByteType()).copy();
		
		int[] classify = we.classifyMismatches2d(
					wrs[0].warpedSource,
					wrs[0].mismatches, 
					m_radius);
////		
//		output =  new ImagePlusAdapter<UnsignedByteType>(
//				we.getMismatchImage(wr, 100, WarpingErrorEnums.calculateValue(m_errors)),
//				new ArrayImgFactory<UnsignedByteType>(),
//				new UnsignedByteType());
		
		m_stat = we.clusterMismatchesByError(
				wrs[0].classifiedMismatches, 
				wrs[0].mismatches, 
				classify);
		
//		new ConvertWarpingMismatches().compute(mismatches, m_errors);
		
		
//		
		return output;
		
		
	}

	@Override
	public BinaryOperation<Img<BitType>, Img<BitType>, Img<T>> copy() {
		return new MyWarpingError<T>(m_type, m_radius, m_errors);
	}
	
	public WarpingErrorEnums[] getErrors(){
		return m_errors;
	}
	
	public double getWarpingError(){
		return m_warpingError;
	}
	
	public double getRandError(){
		return m_randError;
	}
	
	public ClusteredWarpingMismatches getMismatches(){
		return m_stat;
	}

	private ImagePlus createMask(int size){
		Img<FloatType> img = new ArrayImgFactory<FloatType>().
				create(new long[]{size, size}, new FloatType());
		
		Cursor<FloatType> c = img.cursor();
		while( c.hasNext() ){
			c.next().setReal( 1.0f );
		}
		
		return new ConvertToImagePlus<FloatType>().compute(img, null);
	}
}
