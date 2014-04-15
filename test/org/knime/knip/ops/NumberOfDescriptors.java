package org.knime.knip.ops;

import junit.framework.Assert;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.junit.Test;
import org.knime.knip.clump.fourier.FourierOfCurvature;
import org.knime.knip.core.data.algebra.Complex;


/** Tests if two signatures are translataion-invariant
 * 
 * @author Udo
 *
 */
public class NumberOfDescriptors {
	
	private final static int NUMBER_OF_DESCRIPTORS = 32;

	@Test
	public void testDistance() {
	
		Img<DoubleType> img = new ArrayImgFactory<DoubleType>().create( new long[]{ 200 }, new DoubleType());
		Cursor<DoubleType> c= img.cursor();
		
		double i = 0.0d;
		while( c.hasNext() ){
			c.fwd();
			c.get().setReal( i++ );
		}
		
		Img<DoubleType> ref = img.copy();
		Cursor<DoubleType> refCursor = ref.cursor();
		Cursor<DoubleType> cc = Views.interval(Views.extendPeriodic(img), new long[]{ 99 }, new long[]{ 298 }).cursor();
		while( refCursor.hasNext() ){
//			System.out.println( cc.next().getRealDouble() );
			refCursor.next().set( cc.next() );
		}
		
		
		
//		Complex[] c1 = new FourierOfCurvature<DoubleType>(img, 128).getDescriptors(NUMBER_OF_DESCRIPTORS);
//		Complex[] c2 = new FourierOfCurvature<DoubleType>(ref, 256).getDescriptors(NUMBER_OF_DESCRIPTORS);
		
		double[] d1 = new FourierOfCurvature<DoubleType>(img, 128).getMagnitudes(NUMBER_OF_DESCRIPTORS, true);
		double[] d2 = new FourierOfCurvature<DoubleType>(ref, 128).getMagnitudes(NUMBER_OF_DESCRIPTORS, true);
		
//		System.out.println("first: " + c1[0].getMagnitude() + " - " + c2[0].getMagnitude());
		for(int j = 1; j < NUMBER_OF_DESCRIPTORS; j++){
//			System.out.println( c1[j].getMagnitude() / c1[0].getMagnitude() + " - " + c2[j].getMagnitude() / c2[0].getMagnitude());
//			Assert.assertEquals(c1[j].getMagnitude() / c1[0].getMagnitude() , c2[j].getMagnitude() / c2[0].getMagnitude(), 0.01d);
			System.out.println( d1[j] + " - " + d2[j]);
			Assert.assertEquals(d1[j] , d2[j], 0.01d);
		}
	}

}
