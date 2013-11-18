package org.knime.knip.clump.split;

import java.util.Collection;

import net.imglib2.labeling.Labeling;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.clump.boundary.Contour;

/**
 * 
 * @author Udo Schlegel
 *
 * @param <L>
 */
public interface SplittingPoints<T extends RealType<T>, L extends Comparable<L>> 
	extends UnaryOperation<Contour, Collection<long[]>> {

}
