package org.knime.knip.clump.split;

import java.util.List;

import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.clump.boundary.Contour;

/**
 * 
 * @author Udo Schlegel
 *
 * @param <L>
 */
public interface SplittingPoints<T extends RealType<T>> 
	extends UnaryOperation<Contour, List<long[]>> {

}
