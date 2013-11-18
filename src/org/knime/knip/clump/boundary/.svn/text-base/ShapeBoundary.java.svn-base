package org.knime.knip.clump.boundary;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.logic.BitType;
import net.imglib2.view.Views;

import org.knime.knip.clump.util.ClumpUtils;

/**
 * 
 * @author Udo Schlegel
 *
 */
public class ShapeBoundary {
	
	final Img<BitType> m_image;
	
	final List<long[]> m_points;

	
	public ShapeBoundary(Img<BitType> in){
		m_points 		= extractShape(in);
		m_image 		= createImage();
	}

	private List<long[]> extractShape(Img<BitType> in){
		List<long[]> points = new LinkedList<long[]>();
		long[] start = findStartingPoint(in);

		RandomAccess<BitType> raI = Views.extendZero(in).randomAccess();
		long[] pos = new long[ in.numDimensions() ];
		pos[0] = start[0];
		pos[1] = start[1];
		
		long[] prevPos = new long[ in.numDimensions() ];
		prevPos[0] = pos[0]-1;
		prevPos[1] = pos[1];

		long[] tmp = new long[ in.numDimensions() ];
		do{
			tmp = nextBoundary(raI, pos,  prevPos);
			
			prevPos = pos;
			pos = tmp;

			points.add(pos);
		} while( !Arrays.equals(start, pos));
		

		return points;
	}

	public long[] nextBoundary(RandomAccess<BitType> ra, long[] center, long[] start){
		long[] out = Arrays.copyOf(start, start.length);
		do{
			out = nextPoint(ra, center, out);
			ra.setPosition(out);
		}while( !Arrays.equals(start, out) && !ra.get().get() );
		return out;
	}
	
	private long[] nextPoint(RandomAccess<BitType> ra, long[] center, long[] prevPos){
		assert center.length == prevPos.length;
		if( Math.abs( center[0] - prevPos[0] ) > 1 ||
				Math.abs( center[0] - prevPos[0] ) > 1 )
			throw new RuntimeException();
		long[] newPos = new long[ center.length ];
		if( center[0] == prevPos[0]){
			newPos[1] = prevPos[1];
			if ( center[1] < prevPos[1]){
				newPos[0] = prevPos[0] - 1;
			} else if ( center[1] > prevPos[1])
				newPos[0] = prevPos[0] + 1;
		} else if ( center[0] > prevPos[0]){
			if ( center[1] > prevPos[1]){
				newPos[0] = prevPos[0] + 1;
				newPos[1] = prevPos[1];
			} else {
				newPos[0] = prevPos[0];
				newPos[1] = prevPos[1] - 1;
			} 
		} else {
			if ( center[1] >= prevPos[1]){
				newPos[0] = prevPos[0];
				newPos[1] = prevPos[1] + 1;
			} else {
				newPos[0] = prevPos[0] - 1;
				newPos[1] = prevPos[1];
			} 
		}
		return newPos;
	}


	private long[] findStartingPoint(RandomAccessibleInterval<BitType> img){
		long[] pos = new long[img.numDimensions()];
		Cursor<BitType> cursor = Views.iterable(img).cursor();
		do{
			cursor.fwd();
		} while( !cursor.get().get() );
		cursor.localize(pos);
		return pos;
	}
	


	
	private Img<BitType> createImage(){
		long[] max = new long[ m_points.get(0).length ];
		for(long[] p: m_points){
			for(int i = 0; i < p.length; i++){
				max[i] = Math.max(p[i], max[i]);
			}
		}
		for(int i = 0; i < max.length; i++){
			max[i]++;
		}
		Img<BitType> out = new ArrayImgFactory<BitType>().create(max, new BitType());
		RandomAccess<BitType> ra = out.randomAccess();
		for(long[] p: m_points){
			ra.setPosition(p);
			ra.get().set(true);
		}
		return out;
	}

	public Img<BitType> getImage() {
		return m_image;
	}

	public List<long[]> getPoints() {
		return m_points;
	}
	
	public int indefOf(long[] point){
		return ClumpUtils.indexOf(m_points, point);
	}
	
	public List<long[]> getPointsBetween(long[] start, long[] end){
	
		//TODO
		List<long[]> out = new LinkedList<long[]>();
		int i = ClumpUtils.indexOf(m_points, start);
		
		if( i == -1 ) 
			throw new RuntimeException(start + " not found in the contour!");
		
		int j = ClumpUtils.indexOf(m_points, end);
		
		if ( j == -1 )
			throw new RuntimeException(end + " not found in the contour!");
		
		if( i <= j ){
			while( i <= j ){
				out.add( m_points.get(i++));
			}
//			out = m_points.subList(i, j+1);
		} else {
			while( i < m_points.size() ){
				out.add( m_points.get(i++));
			}
			for(int k = 0; k < j; k++){
				out.add( m_points.get(k));
			}

		}
		return out;
	}

}
