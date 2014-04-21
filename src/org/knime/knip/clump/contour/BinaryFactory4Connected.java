package org.knime.knip.clump.contour;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.randomaccessibleinterval.unary.regiongrowing.AbstractRegionGrowing;
import net.imglib2.type.logic.BitType;

import org.knime.knip.core.util.PolygonTools;

public class BinaryFactory4Connected extends AbstractBinaryFactory{

	private Contour m_contour;
	
	private final RandomAccessibleInterval<BitType> m_img;
	
	private final int[] m_start;
	
	public BinaryFactory4Connected(RandomAccessibleInterval<BitType> img, long[] start){
		m_img = img;
		m_start = new int[]{ (int)start[0], (int)start[1]};
	}

	@Override
	public Contour createContour() {
		if( m_contour == null )
			m_contour = wrap( PolygonTools.extractPolygon(m_img, m_start));
		return m_contour;
	}
	
	public static Contour wrap(Polygon polygon){
		final int[] xpoints = polygon.xpoints;
		final int[] ypoints = polygon.ypoints;
		
		final List<long[]> list = new ArrayList<long[]>( polygon.npoints );
		
		for(int i = 0; i < polygon.npoints; i++){
			list.add( new long[]{ xpoints[i], ypoints[i]} );
		}
		
		return new Contour(list);
	}

	@Override
	public long[][] getStructuringElement() {
		return AbstractRegionGrowing.get4ConStructuringElement(2);
	}

}
