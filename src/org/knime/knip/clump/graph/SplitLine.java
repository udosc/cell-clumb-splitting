package org.knime.knip.clump.graph;

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
	
	private final int m_index;
	
	private final Cursor<T> m_cursor;
		
	public SplitLine(int index, RandomAccessible<T> ra, Point p1, Point p2){
		super( ra.numDimensions() );
		m_cursor = new BresenhamLine<T>(ra, p1, p2);
		m_index = index;
	}
	
	public SplitLine(int index, Cursor<T> cursor){
		super( cursor.numDimensions() );
		m_cursor = cursor;
		m_index = index;
	}
	
	public int getIndex(){
		return m_index;
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
		return new SplitLine<T>(m_index, m_cursor);
	}

	@Override
	public AbstractCursor<T> copyCursor() {
		final AbstractCursor<T> res = this.copy();
		res.reset();
		return res;
	}



}
