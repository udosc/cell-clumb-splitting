package org.knime.knip.clump.warp;

import ij.ImagePlus;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;

import org.knime.knip.clump.ops.ConvertToImagePlus;
import org.knime.knip.clump.types.WarpingErrorEnums;
import org.knime.knip.clump.util.ImagePlusAdapter;

import trainableSegmentation.metrics.ClusteredWarpingMismatches;
import trainableSegmentation.metrics.WarpingError;
import trainableSegmentation.metrics.WarpingResults;

/**
 * 
 * @author Schlegel
 *
 */
public class ImgLib2WarpingError
implements BinaryOperation<Img<BitType>, Img<BitType>, Img<UnsignedByteType>> {
	
	private WarpingErrorEnums[] m_errors;
	
	private double m_warpingError;
	
	public ImgLib2WarpingError(WarpingErrorEnums... enums){
		m_errors = enums;
	}

	@Override
	public Img<UnsignedByteType> compute(Img<BitType> inputA, Img<BitType> inputB,
			Img<UnsignedByteType> output) {
				
		WarpingError we = new WarpingError(
				new ConvertToImagePlus<BitType>().compute(inputA, null),
				new ConvertToImagePlus<BitType>().compute(inputB, null),
				null,
				WarpingErrorEnums.calculateValue(m_errors));
		
//		return new ImagePlusAdapter<UnsignedByteType>(we., factory, type)
		
		int[] res = new int[ m_errors.length ];
		for(int i = 0; i < res.length; i++)
			res[i] = m_errors[i].getValue();
//		
//		
//		
//

//		final WarpingResults wr = we.getWarpingResults(0.1d, true, true, 100);
		final WarpingResults[] wrs = we.simplePointWarp2dMT(0.5d, true, true, -1);
		m_warpingError = wrs[0].warpingError;

//		
		output = new ImagePlusAdapter<UnsignedByteType>( 
				we.getMismatchImage(wrs[0], 200),
//				wrs[0].warpedSource,
				new ArrayImgFactory<UnsignedByteType>(),
				new UnsignedByteType()).copy();
		
		
		
		int[] classify = we.classifyMismatches2d(
					wrs[0].warpedSource,
					wrs[0].mismatches, 
					-1);
//		
//		output =  new ImagePlusAdapter<UnsignedByteType>(
//				we.getMismatchImage(wr, 100, WarpingErrorEnums.calculateValue(m_errors)),
//				new ArrayImgFactory<UnsignedByteType>(),
//				new UnsignedByteType());
		
		ClusteredWarpingMismatches mismatches = we.clusterMismatchesByError(
				wrs[0].classifiedMismatches, 
				wrs[0].mismatches, 
				classify);
		new ConvertWarpingMismatches().compute(mismatches, m_errors);
		
//		
		return output;
		
		
	}

	@Override
	public BinaryOperation<Img<BitType>, Img<BitType>, Img<UnsignedByteType>> copy() {
		return new ImgLib2WarpingError();
	}
	
	public WarpingErrorEnums[] getErrors(){
		return m_errors;
	}
	
	public double getWarpingError(){
		return m_warpingError;
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
