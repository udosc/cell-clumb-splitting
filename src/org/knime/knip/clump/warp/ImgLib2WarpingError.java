package org.knime.knip.clump.warp;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.knime.knip.clump.ops.ConvertToImagePlus;
import org.knime.knip.clump.types.WarpingErrorEnums;

import trainableSegmentation.metrics.ClassificationStatistics;
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
	
	public ImgLib2WarpingError(WarpingErrorEnums... enums){
		m_errors = enums;
	}

	@Override
	public Img<UnsignedByteType> compute(Img<BitType> inputA, Img<BitType> inputB,
			Img<UnsignedByteType> output) {
		
		Img<BitType> mask = new ArrayImgFactory<BitType>().create(new long[]{10, 10}, new BitType(true));
		for(Cursor<BitType> c = mask.cursor(); c.hasNext(); )
			c.next().set(true);
		
		WarpingError we = new WarpingError(
				new ConvertToImagePlus<BitType>().compute(inputA, null),
				new ConvertToImagePlus<BitType>().compute(inputB, null)
				);
		
		int[] res = new int[ m_errors.length ];
		for(int i = 0; i < res.length; i++)
			res[i] = m_errors[i].getValue();
		

		
		WarpingResults wr = we.getWarpingResults(0.1d, true, true, 20);

		output = new ImagePlusAdapter<UnsignedByteType>( 
				wr.classifiedMismatches,
				new ArrayImgFactory<UnsignedByteType>(),
				new UnsignedByteType()).copy();
		
//		
//		output =  new ImagePlusAdapter<UnsignedByteType>(
//				we.getMismatchImage(wr, 100, WarpingErrorEnums.calculateValue(m_errors)),
//				new ArrayImgFactory<UnsignedByteType>(),
//				new UnsignedByteType());
		
		ClusteredWarpingMismatches mismatches = we.clusterMismatchesByType( res );
		new ConvertWarpingMismatches().compute(mismatches, m_errors);
		
		return output;
		
		
	}

	@Override
	public BinaryOperation<Img<BitType>, Img<BitType>, Img<UnsignedByteType>> copy() {
		return new ImgLib2WarpingError();
	}
	
	public WarpingErrorEnums[] getErrors(){
		return m_errors;
	}

}
