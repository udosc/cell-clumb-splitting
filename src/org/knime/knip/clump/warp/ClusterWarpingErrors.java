package org.knime.knip.clump.warp;

import org.knime.knip.clump.types.WarpingErrorEnums;
import org.knime.knip.core.util.ImgUtils;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.ops.operation.randomaccessibleinterval.unary.regiongrowing.CCA;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedIntType;

/**
 * 
 * @author Schlegel
 *
 * @param <L>
 */
public class ClusterWarpingErrors<L extends Comparable<L>> 
	implements UnaryOperation<Labeling<L>, WarpingErrorEnums[]> {
		
	private int m_size;
	
	public ClusterWarpingErrors(){
		this( 0 );
	}
	
	public ClusterWarpingErrors(int size){
		m_size = size;
	}

	@Override
	public WarpingErrorEnums[] compute(Labeling<L> input,
			WarpingErrorEnums[] output) {
		Cursor<LabelingType<L>> cLabel = input.cursor();
		for(WarpingErrorEnums e: output){
			cLabel.reset();
			Img<BitType> res = new ArrayImgFactory<BitType>().create(input, new BitType());
			Cursor<BitType> cImg = res.cursor();
			while(cLabel.hasNext()){
				cImg.fwd();
				cLabel.fwd();
				cImg.get().set( cLabel.get().getLabeling().contains( e.name()) );
			}
			
			Labeling<Integer> labeling = new CCA<BitType>(CCA.get8ConStructuringElement(res.numDimensions()), new BitType(false)).
				compute( 
						res, 
						new NativeImgLabeling<Integer, UnsignedIntType>( 
								ImgUtils.createEmptyCopy(res, new UnsignedIntType())));
			
			int errors = 0;
			
			for(Integer label: labeling.getLabels()){
				if( labeling.getArea(label) > m_size)
					errors++;
			}
			
			System.out.println(e.name() + ": " + errors );
			
			
			
			e.setNumberOfErrors( errors );
			
			
		}
		return output;
	}

	@Override
	public UnaryOperation<Labeling<L>, WarpingErrorEnums[]> copy() {
		return new ClusterWarpingErrors<L>();
	}

}
