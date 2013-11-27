package org.knime.knip.clump.ops;

import java.util.Collection;
import java.util.Map;

import org.knime.core.util.Pair;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.ops.operation.labeling.unary.LabelingToImg;
import net.imglib2.type.logic.BitType;

/**
 * 
 * @author Schlegel
 *
 * @param <L>
 */
public class FindStartingPoint<L extends Comparable<L>> 
	implements UnaryOperation<Labeling<L>, Collection<Pair<L,long[]>>> {

	@Override
	public Collection<Pair<L, long[]>> compute(Labeling<L> arg0,
			Collection<Pair<L,long[]>> arg1) {
		final long[] dim = new long[ arg0.numDimensions() ];
		arg0.dimensions(dim);		
		for(L l: arg0.getLabels()){
			long[] start = new long[ arg0.numDimensions() ];
			if ( arg0.getRasterStart(l, start) )
				arg1.add( new Pair<L, long[]>(l, start) );
		}
		return arg1;
	}

	@Override
	public UnaryOperation<Labeling<L>, Collection<Pair<L, long[]>>> copy() {
		return new FindStartingPoint<L>();
	}

}
