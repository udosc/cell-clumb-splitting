package org.knime.knip.ops;

import org.junit.Assert;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.junit.Test;

public class OutOfBoundsTest {
	
	@Test
	public void testOutOfBounds(){
		Img<DoubleType> img = new ArrayImgFactory<DoubleType>().
				create(new int[]{ 100 }, new DoubleType());
		
		Cursor<DoubleType> c = img.cursor();
		
		for(int i = 0; i < img.dimension(0); i++){
			c.fwd();
			c.get().setReal( i );
		}
		RandomAccess<DoubleType> ra = Views.extendPeriodic(img).randomAccess();
		
		for(int i = 1; i < img.dimension(0); i++){
			ra.setPosition(-i, 0);
			double expected = ra.get().get();
			
			System.out.println( expected );
			
			ra.setPosition(img.dimension(0)-i, 0);
			double actual = ra.get().get();
			Assert.assertEquals(expected, actual, 0.001d);
		}
		
//		Views.extend( img , new OutOfBoundsPeriodicFactory<DoubleType, Img<DoubleType>>());
	}

}
