package org.knime.knip.clump.boundary;

import java.awt.Polygon;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.knime.knip.clump.util.ClumpUtils;

import net.imglib2.AbstractCursor;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Sampler;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.pointset.AbstractPointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedIntType;

/**
 * 
 * @author Udo Schlegel
 *
 */
public class Contour 
	extends AbstractPointSet{
	
	private final List<long[]> m_points;
	
	
	public Contour(List<long[]> points) {
		super();
		
		minBounds = new long[ points.get(0).length ];
		maxBounds = new long[ points.get(0).length ];
		
		m_points = points;
		
		for(long[] pos: m_points){
			for(int i = 0; i < pos.length; i++){
				if( pos[i] > maxBounds[i])
					maxBounds[i] = pos[i];
				else if( pos[i] < minBounds[i])
					minBounds[i] = pos[i];
			}
		}
	}
	
	public int indefOf(long[] point){
		return ClumpUtils.indexOf(m_points, point);
	}
	
	public long[] get(int index){
		return m_points.get(index);
	}
	
	public int length(){
		return m_points.size();
	}
	
	public Iterable<long[]> getIterator(){
		return m_points;
	}
	
	public RandomAccessibleInterval<UnsignedIntType> getCoordinates(int dimension){
		Img<UnsignedIntType> out = new ArrayImgFactory<UnsignedIntType>().create(
				new long[]{m_points.size(), 1}, new UnsignedIntType());
		Cursor<UnsignedIntType> c = out.cursor();
		
		for(int i=0; i < m_points.size(); i++){
			c.fwd();
			c.get().set( m_points.get(i)[dimension]);
		}
		
		return out;
	}
	
	public List<long[]> getPointsInbetween(long[] start, long[] end){
		
		//TODO
		List<long[]> out = new LinkedList<long[]>();
		int i = ClumpUtils.indexOf(m_points, start);
		
		if( i == -1 ) 
			throw new RuntimeException(start + " not found in the contour!");
		
		int j = ClumpUtils.indexOf(m_points, end);
		
		if ( j == -1 )
			throw new RuntimeException(end + " not found in the contour!");
		
		if( i == j ){
			//Return the whole contour
			out.addAll( m_points );
		} else if( i <= j ){
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
	
	public Polygon createPolygon(){
		final int n = m_points.size();
		int[] xpoints = new int[n];
		int[] ypoints = new int[n];
		for(int i = 0; i < n; i++){
			xpoints[i] = (int) m_points.get(i)[0];
			ypoints[i] = (int) m_points.get(i)[1];
		}
		return new Polygon(xpoints, ypoints, n);
	}
	
	@Override
	public PointSetIterator iterator(){
		return new ContourIterator( m_points.get(0).length);
	}

	@Override
	public long[] getOrigin() {
		return m_points.get(0);
	}

	@Override
	public void translate(long[] delta) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int numDimensions() {
		return m_points.get(0).length;
	}

	@Override
	public boolean includes(long[] point) {
		return ClumpUtils.indexOf(m_points, point) >= 0;
	}

	@Override
	public long size() {
		return m_points.size();
	}

	@Override
	public PointSet copy() {
		return new Contour(m_points);
	}

	@Override
	protected long[] findBoundMin() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected long[] findBoundMax() {
		throw new UnsupportedOperationException();
	}
	
	private class ContourIterator extends AbstractCursor<long[]>
		implements PointSetIterator{

		private int index = -1;
		
		private final int n;
		
		public ContourIterator(int n) {
			super(n);
			this.n = n;
		}

		@Override
		public long[] get() {
			return m_points.get(index);
		}

		@Override
		public void fwd() {
			index++;
		}

		@Override
		public void reset() {
			index = 0;
		}

		@Override
		public boolean hasNext() {
			return index < m_points.size()-1;
		}

		@Override
		public void localize(long[] position) {
			for(int i = 0; i < position.length; i++)
				position[i] = m_points.get(index)[i];
		}

		@Override
		public long getLongPosition(int d) {
			return index;
		}

		@Override
		public AbstractCursor<long[]> copy() {
			return new ContourIterator(n);
		}

		@Override
		public AbstractCursor<long[]> copyCursor() {
			return new ContourIterator(n);
		}
		


	}
}