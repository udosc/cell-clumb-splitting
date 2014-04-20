package org.knime.knip.clump.contour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.randomaccessibleinterval.unary.regiongrowing.AbstractRegionGrowing;
import net.imglib2.ops.types.ConnectedType;
import net.imglib2.type.logic.BitType;
import net.imglib2.view.Views;

/**
 * 
 * @author Udo Schlegel
 *
 * @param <T>
 */
public abstract class AbstractBinaryFactory
	implements ContourFactory<BitType>{
	
	private RandomAccessibleInterval<BitType> m_img;
	
	private final long[] m_start;
	
	AbstractBinaryFactory(RandomAccessibleInterval<BitType> img){
		m_img = img;
		m_start = findStartingPoint(img);
	}
	
	AbstractBinaryFactory(RandomAccessibleInterval<BitType> img, long[] start){
		m_img = img;
		m_start = start;
	}
	
	@Override
	public Contour createContour() {
		return new Contour( extractShape(m_img));
	}
	
	private List<long[]> extractShape(RandomAccessibleInterval<BitType> in){
		List<long[]> points = new LinkedList<long[]>();

		RandomAccess<BitType> raI = Views.extendZero(in).randomAccess();
		long[] pos = new long[ in.numDimensions() ];
		pos[0] = m_start[0];
		pos[1] = m_start[1];
		

//		long[] tmp = new long[ in.numDimensions() ];
		int count = 0;
		do{
			points.add(pos);
			pos = nextPoint(raI, pos);

			
			
			if( count++ > 10000)
//				throw new RuntimeException("Contour is not closed");
				break;
				
		} while( !(m_start[0] == pos[0] && m_start[1] == pos[1]));
		

		return points;
	}
	
//	private long[] nextBoundary(RandomAccess<BitType> ra, long[] center, long[] start){
//		long[] out = Arrays.copyOf(start, start.length);
//		do{
//			out = nextPoint(ra, center);
//			ra.setPosition(out);
//		}while( !Arrays.equals(start, out) && !ra.get().get() );
//		return out;
//	}
	
	protected abstract long[] nextPoint(RandomAccess<BitType> ra, long[] center);
//	{
//		assert center.length == prevPos.length;
//		if( Math.abs( center[0] - prevPos[0] ) > 1 ||
//				Math.abs( center[0] - prevPos[0] ) > 1 )
//			throw new RuntimeException();
//		long[] newPos = new long[ center.length ];
//		if( center[0] == prevPos[0]){
//			newPos[1] = prevPos[1];
//			if ( center[1] < prevPos[1]){
//				newPos[0] = prevPos[0] - 1;
//			} else if ( center[1] > prevPos[1])
//				newPos[0] = prevPos[0] + 1;
//		} else if ( center[0] > prevPos[0]){
//			if ( center[1] > prevPos[1]){
//				newPos[0] = prevPos[0] + 1;
//				newPos[1] = prevPos[1];
//			} else {
//				newPos[0] = prevPos[0];
//				newPos[1] = prevPos[1] - 1;
//			} 
//		} else {
//			if ( center[1] >= prevPos[1]){
//				newPos[0] = prevPos[0];
//				newPos[1] = prevPos[1] + 1;
//			} else {
//				newPos[0] = prevPos[0] - 1;
//				newPos[1] = prevPos[1];
//			} 
//		}
//		return newPos;
//	}

	public abstract long[][] getStructuringElement();

	protected static long[] findStartingPoint(RandomAccessibleInterval<BitType> img){
		long[] pos = new long[img.numDimensions()];
		Cursor<BitType> cursor = Views.iterable(img).cursor();
		do{
			cursor.fwd();
		} while( !cursor.get().get() );
		cursor.localize(pos);
		return pos;
	}
	
	public static ContourFactory<BitType> factory(RandomAccessibleInterval<BitType> img, String element){
		if (element.equals(ConnectedType.EIGHT_CONNECTED.name())) {
            return new BinaryFactory8Connected(img);
        } else {
        	return new BinaryFactory4Connected(img);
        }
	}
	
	public static ContourFactory<BitType> factory(RandomAccessibleInterval<BitType> img, long[] start, String element){
		if (element.equals(ConnectedType.EIGHT_CONNECTED.name())) {
            return new BinaryFactory8Connected(img, start);
        } else {
        	return new BinaryFactory4Connected(img, start);
        }
	}

}
