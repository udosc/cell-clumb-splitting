package org.knime.knip.clump.contour;

import java.util.Collection;

import net.imglib2.labeling.Labeling;
import net.imglib2.ops.operation.UnaryOperation;

import org.knime.core.util.Pair;

/**
 * 
 * @author Schlegel
 *
 * @param <L>
 */
public class FindStartingPoints<L extends Comparable<L>> 
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
		return new FindStartingPoints<L>();
	}

}
