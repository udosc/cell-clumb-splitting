package org.knime.knip.clump.warp;

import org.knime.knip.clump.types.WarpingErrorEnums;

import trainableSegmentation.metrics.ClusteredWarpingMismatches;
import net.imglib2.ops.operation.UnaryOperation;

public class ConvertWarpingMismatches 
	implements UnaryOperation<ClusteredWarpingMismatches, WarpingErrorEnums[]> {

	@Override
	public WarpingErrorEnums[] compute(ClusteredWarpingMismatches input,
			WarpingErrorEnums[] output) {
		for(WarpingErrorEnums e: output){
			switch( e ){
				case MERGE:
					e.setNumberOfErrors( input.numOfMergers );
					break;
				case SPLIT:
					e.setNumberOfErrors( input.numOfSplits );
					break;
				case HOLE_ADDITION:
					e.setNumberOfErrors( input.numOfHoleAdditions );
					break;
				case HOLE_DELETION:
					e.setNumberOfErrors( input.numOfHoleDeletions );
					break;
				case OBJECT_ADDITION:
					e.setNumberOfErrors( input.numOfObjectAdditions );
					break;
				case OBJECT_DELETION:
					e.setNumberOfErrors( input.numOfObjectDeletions );
					break;
			}
		}
		return output;
	}

	@Override
	public UnaryOperation<ClusteredWarpingMismatches, WarpingErrorEnums[]> copy() {
		return new ConvertWarpingMismatches();
	}

}
