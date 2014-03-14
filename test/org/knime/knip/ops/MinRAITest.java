package org.knime.knip.ops;

import junit.framework.Assert;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.Test;
import org.knime.knip.clump.dist.MinRAIDistance;

public class MinRAITest {
	

	@Test
	public void testDistance() {
	
		Img<DoubleType> img = new ArrayImgFactory<DoubleType>().create( new long[]{ 100 }, new DoubleType());
		Cursor<DoubleType> c= img.cursor();
		
		double i = 0.0d;
		while( c.hasNext() ){
			c.fwd();
			c.get().setReal( i++ );
		}
		
		Img<DoubleType> res = new ArrayImgFactory<DoubleType>().create( new long[]{ 10 }, new DoubleType());
		c = res.cursor();
		i = 20.0d;
		while( c.hasNext() ){
			c.fwd();
			c.get().setReal( i++ );
		}
		
		double out = new MinRAIDistance<DoubleType>( 1 ).compute(img, res, new DoubleType()).getRealDouble();
		Assert.assertEquals(0.0d, out, 0.00001d);
		
		double[] list = new double[ 10 ];
		list[0] = 95.0d;
		list[1] = 96.0d;
		list[2] = 97.0d;
		list[3] = 98.0d;
		list[4] = 99.0d;
		list[5] =  0.0d;
		list[6] =  1.0d;
		list[7] =  2.0d;
		list[8] =  3.0d;
		list[9] =  4.0d;
		int n = 0;
		c.reset();
		while( c.hasNext() ){
			c.fwd();
			c.get().setReal( list[n++] );
		}
		
		out = new MinRAIDistance<DoubleType>( 1 ).compute(img, res, new DoubleType()).getRealDouble();
		Assert.assertEquals(0.0d, out, 0.00001d);
		
		res = new ArrayImgFactory<DoubleType>().create( new long[]{ 110 }, new DoubleType());
		c = res.cursor();
		i = 10.0d;
		while( c.hasNext() ){
			c.fwd();
			c.get().setReal( i++ );
		}
		
		out = new MinRAIDistance<DoubleType>( 1 ).compute(img, res, new DoubleType()).getRealDouble();
		System.out.println( out );
	}
}
