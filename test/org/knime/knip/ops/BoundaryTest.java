package org.knime.knip.ops;
import static org.junit.Assert.assertArrayEquals;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.logic.BitType;

import org.junit.Test;
import org.knime.knip.clump.boundary.ShapeBoundary;

/**
 * @author Udo Schlegel
 *
 */
public class BoundaryTest {

	@Test
	public void testBoundary() {
		
		Img<BitType> image = new ArrayImgFactory<BitType>()
				.create(new long[] { 3, 3 }, new BitType());

		boolean[][] img = new boolean[3][3];
		img[0] = new boolean[] { false, false, true };
		img[1] = new boolean[] { false, true, false };
		img[2] = new boolean[] { false, false, true };

		RandomAccess<BitType> cursor = image.randomAccess();
		for (int i = 0; i < img.length; i++) {
			for (int j = 0; j < img[i].length; j++) {
				cursor.setPosition(i, 0);
				cursor.setPosition(j, 1);
				cursor.get().set(img[i][j]);
			}
		}
		
		long[] result = new ShapeBoundary(image).
				nextBoundary(image.randomAccess(), new long[]{1,1}, new long[]{0,2});
		
		assertArrayEquals(new long[]{ 2, 2 }, result);
	}

}
