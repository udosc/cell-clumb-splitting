package org.knime.knip.ops;


import org.junit.Assert;
import org.junit.Test;
import org.knime.knip.clump.util.MyUtils;

/**
 * @author Udo Schlegel
 *
 */
public class AngleTest {

	@Test
	public void testBoundary() {
		
		
		double value = MyUtils.calcAngle(
				new long[]{0, 0, 0}, 
				new long[]{1, 4, -2},
				new long[]{-3, 3, 1});
		
		
		Assert.assertEquals(1.213d, value, 0.01d);
	}

}

