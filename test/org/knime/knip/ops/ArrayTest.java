package org.knime.knip.ops;

import java.util.Arrays;

import net.imglib2.Point;

import org.junit.Assert;
import org.junit.Test;
import org.knime.knip.clump.util.MyUtils;

public class ArrayTest {

	@Test
	public void testBoundary() {
		Point p1 = Point.wrap(new long[]{0 , 1});
		Point p2 = Point.wrap(new long[]{0 , 1});
		
		long[] l1 = new long[ 2 ];
		long[] l2 = new long[ 2 ];
		
		p1.localize(l1);
		p2.localize(l2);
		
		System.out.println( p1.equals(p2));
		System.out.println( l1.equals(l2));
		System.out.println( Arrays.equals(l1, l2));
	}
}
