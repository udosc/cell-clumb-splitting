package org.knime.knip.clump.ops;

import java.util.Collection;

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.BresenhamLine;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.logic.BitType;

import org.knime.core.util.Pair;

/**
 * 
 * @author Schlegel
 *
 */
public class ValidateSplitLines 
implements UnaryOperation<Collection<Pair<Point, Point>>, Collection<Pair<Point, Point>>> {
	
	
	private final RandomAccessibleInterval<BitType> m_rai;
	
	public ValidateSplitLines(RandomAccessibleInterval<BitType> rai){
		m_rai = rai;
	}
	
	@Override
	public Collection<Pair<Point, Point>> compute(
			Collection<Pair<Point, Point>> arg0,
			Collection<Pair<Point, Point>> arg1) {

		for(Pair<Point, Point> pair: arg0){
			Cursor<BitType> cursor = 
					new BresenhamLine<BitType>(m_rai.randomAccess(), 
							pair.getFirst(), 
							pair.getSecond());
			int res = 0;
			boolean isValid = true;
			while( cursor.hasNext() ){
				if ( !cursor.next().get() ){
					isValid = false;
				}
			}
			if ( isValid )
				arg1.add(pair);
		}
		return arg1;
	}

	@Override
	public UnaryOperation<Collection<Pair<Point, Point>>, Collection<Pair<Point, Point>>> copy() {
		return new ValidateSplitLines(m_rai);
	}

}
