package org.knime.knip.clump.util;

import java.util.Comparator;
import net.imglib2.Point;

public class PointComparator implements Comparator<Point> {

	@Override
	public int compare(Point o1, Point o2) {
		if( o1.getLongPosition(0) == o2.getLongPosition(0)){
			if( o1.getLongPosition(1) == o2.getLongPosition(1))
				return 0;
			else
				return o1.getIntPosition(1) - o2.getIntPosition(1);
		} else 
			return o1.getIntPosition(0) - o2.getIntPosition(0);
	}
}
