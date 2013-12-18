package org.knime.knip.clump.contour;

import java.util.ArrayList;
import java.util.List;


import net.imglib2.Cursor;
import net.imglib2.roi.IterableRegionOfInterest;
import net.imglib2.sampler.special.ConstantRandomAccessible;
import net.imglib2.type.numeric.RealType;

/**
 * 
 * @author Udo Schlegel
 *
 */
public class WatershedFactory<T extends RealType<T>, L extends Comparable<L>> 
	implements ContourFactory<T>{
	
	private IterableRegionOfInterest m_label;
	
	private T m_type;
	
	private long m_size;
	
	public WatershedFactory(IterableRegionOfInterest label, long size, T type){
		m_label = label;
		m_size = size;
		m_type = type;
	}

	public Contour createContour(){
		
		List<long[]> points = new ArrayList<long[]>( (int) m_size );
		
		Cursor<T> c = m_label.getIterableIntervalOverROI(
				new ConstantRandomAccessible<T>(m_type, m_label.numDimensions())).localizingCursor();
		while( c.hasNext()){
			c.fwd();
			long[] pos = new long[ m_label.numDimensions() ];
			c.localize(pos);
			points.add(pos);
		}
		return new Contour(points);
	}
}
