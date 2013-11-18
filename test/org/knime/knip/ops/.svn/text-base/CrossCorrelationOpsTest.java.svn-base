package org.knime.knip.ops;

import junit.framework.TestCase;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.junit.Test;
import org.knime.knip.clump.dist.CrossCorrelationSimilarity;

/** 
 * 
 * @author Udo Schlegel
 *
 */
public class CrossCorrelationOpsTest extends TestCase {

	private static final int EXPECTED = 4;
	
	private UnsignedByteType m_output;
	
	@Override
	public void setUp() throws Exception {

		final Img<UnsignedByteType> input = new ArrayImgFactory<UnsignedByteType>()
				.create(new long[] { 6, 6 }, new UnsignedByteType());
		m_output = input.firstElement().createVariable();

		int[][] image = new int[6][6];
		image[0] = new int[] { 1, 1, 1, 0, 0, 0 };
		image[1] = new int[] { 0, 1, 1, 1, 0, 0 };
		image[2] = new int[] { 0, 0, 1, 1, 1, 0 };
		image[3] = new int[] { 0, 0, 1, 1, 0, 0 };
		image[4] = new int[] { 0, 1, 1, 0, 0, 0 };
		image[5] = new int[] { 1, 1, 1, 0, 0, 0 };

		int[][] kern = new int[3][3];
		kern[0] = new int[] { 1, 0, 1};
		kern[1] = new int[] { 0, 1, 0};
		kern[2] = new int[] { 1, 0, 1};
		
		RandomAccess<UnsignedByteType> cursor = input.randomAccess();
		for (int i = 0; i < image.length; i++) {
			for (int j = 0; j < image[i].length; j++) {
				cursor.setPosition(i, 0);
				cursor.setPosition(j, 1);
				cursor.get().set(image[i][j]);
			}
		}
		
		final Img<UnsignedByteType> kk = new ArrayImgFactory<UnsignedByteType>()
				.create(new long[] { 3, 3 }, new UnsignedByteType());
		RandomAccess<UnsignedByteType> kernelCursor = kk.randomAccess();
		
		for (int i = 0; i < kern.length; i++) {
			for (int j = 0; j < kern[i].length; j++) {
				kernelCursor.setPosition(i, 0);
				kernelCursor.setPosition(j, 1);
				kernelCursor.get().set(kern[i][j]);
			}
		}

		new CrossCorrelationSimilarity<UnsignedByteType>().
			compute(input, kk, m_output);

		

	}

	@Test
	public void testConvolution() {
		
		System.out.println( m_output );
		
		assertEquals(EXPECTED, m_output.get());
		
//		List<Double> list = new LinkedList<Double>();
//		
//		RandomAccess<UnsignedByteType> ra = m_image.randomAccess();
//		for (int i = 0; i < m_image.dimension(0); i++) {
//			for (int j = 0; j < m_image.dimension(1); j++) {
//				ra.setPosition(i, 0);
//				ra.setPosition(j, 1);
//				list.add(ra.get().getRealDouble());
//			}
//		}
//
//		double[] values = new double[list.size()];
//		for (int i = 0; i < values.length; i++) {
//			values[i] = list.get(i);
//			
//		}
//		double[] results = new double[] { 
//				4.0, 3.0, 4.0, 1.0, 
//				2.0, 4.0, 3.0, 3.0, 
//				2.0, 3.0, 4.0, 1.0, 
//				4.0, 3.0, 2.0, 1.0 };
//		
//
//		assertArrayEquals(results, values, 0.01d);
	}
}