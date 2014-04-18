package org.knime.knip.clump.graph;

import java.util.Arrays;

import org.knime.core.util.Pair;

import net.imglib2.AbstractCursor;
import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.region.BresenhamLine;
import net.imglib2.type.numeric.RealType;

/**
 * 
 * @author Schlegel
 *
 * @param <T>
 */
public class SplitLine<T extends RealType<T>>
extends AbstractCursor<T>{
		
	private final Cursor<T> m_cursor;
	
	private final Point m_p1;
	
	private final Point m_p2;
		
	public SplitLine(RandomAccessible<T> ra, Point p1, Point p2){
		super( ra.numDimensions() );
		m_cursor = new BresenhamLine<T>(ra, p1, p2);
		m_p1 = p1;
		m_p2 = p2;
	}
	
	public SplitLine(RandomAccessible<T> ra, long[] p1, long[] p2){
		this(ra, Point.wrap(p1), Point.wrap(p2));
	}
	
	private SplitLine(Cursor<T> cursor, Point p1, Point p2){
		super( cursor.numDimensions() );
		m_cursor = cursor;
		m_p1 = p1;
		m_p2 = p2;
	}
	
	public Pair<Point, Point> getPoints(){
		return new Pair<Point, Point>(m_p1, m_p2);
	}
	
	public Point getP1(){
		return m_p1;
	}
	

	public Point getP2(){
		return m_p2;
	}
	
	@Override
	public T get() {
		return m_cursor.get();
	}

	@Override
	public void fwd() {
		m_cursor.fwd();
	}

	@Override
	public void reset() {
		m_cursor.reset();
	}

	@Override
	public boolean hasNext() {
		return m_cursor.hasNext();
	}

	@Override
	public void localize(long[] position) {
		m_cursor.localize(position);
	}

	@Override
	public long getLongPosition(int d) {
		return m_cursor.getLongPosition(d);
	}

	@Override
	public AbstractCursor<T> copy() {
		return new SplitLine<T>(m_cursor, m_p1, m_p2);
	}

	@Override
	public AbstractCursor<T> copyCursor() {
		final AbstractCursor<T> res = this.copy();
		res.reset();
		return res;
	}

	@Override
	public boolean equals(Object arg){
		if( arg == null || !(arg instanceof SplitLine)){
			return false;
		}
		SplitLine<T> res = (SplitLine<T>) arg;
		
		if ( res.numDimensions() != numDimensions() )
			return false;
		
		long[] p1 = new long[ res.numDimensions() ];
		res.getPoints().getFirst().localize(p1);
		long[] p2 = new long[ res.numDimensions() ];
		res.getPoints().getSecond().localize(p2);
		
		long[] m1 = new long[ numDimensions() ];
		m_p1.localize(m1);
		long[] m2 = new long[ numDimensions() ];
		m_p2.localize(m2);
		
		if( Arrays.equals(p1, m1) && Arrays.equals(p2, m2) || 
				Arrays.equals(p1, m2) && Arrays.equals(p2, m1))
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode(){
		int res = 17;
		res = 37 * res + m_p1.hashCode();
		res = 37 * res + m_p2.hashCode();
		return res;
	}


}
