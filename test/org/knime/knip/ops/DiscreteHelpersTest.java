package org.knime.knip.ops;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.Test;
import org.knime.knip.clump.util.DiscretHelpers;

public class DiscreteHelpersTest {

	@Test
	public void testDerivations() {
		
		Img<DoubleType> img1d = DiscretHelpers.createFirstDerivation1D(3, new DoubleType());
		
		Img<DoubleType> img2d = DiscretHelpers.createSecondDerivation1D(3, new DoubleType());
		
		
		Cursor<DoubleType> c = img1d.cursor();
		while( c.hasNext() ){
			System.out.print(c.next() + "\t");
		}
		
		System.out.println("");
		
		c = img2d.cursor();
		while( c.hasNext() ){
			System.out.print(c.next() + "\t");
		}
		

	}
}
