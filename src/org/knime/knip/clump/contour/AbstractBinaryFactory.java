package org.knime.knip.clump.contour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.randomaccessibleinterval.unary.regiongrowing.AbstractRegionGrowing;
import net.imglib2.ops.types.ConnectedType;
import net.imglib2.type.logic.BitType;
import net.imglib2.view.Views;

/**
 * 
 * @author Udo Schlegel
 *
 * @param <T>
 */
public abstract class AbstractBinaryFactory
	implements ContourFactory<BitType>{
	
	public abstract long[][] getStructuringElement();
	
	public static ContourFactory<BitType> factory(RandomAccessibleInterval<BitType> img, long[] start, String element){
		if (element.equals(ConnectedType.EIGHT_CONNECTED.toString())) {
            return new BinaryFactory8Connected(img, start);
        } else {
        	return new BinaryFactory4Connected(img, start);
        }
	}

}
