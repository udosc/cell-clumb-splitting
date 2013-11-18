package org.knime.knip.clump.boundary;

import java.util.ArrayList;
import java.util.List;

public class ChainCode {
	
	private final List<Byte> m_chainCode;
	
	private final ShapeBoundary m_shape;
	
	public ChainCode(ShapeBoundary shape){
		m_shape = shape;
		m_chainCode = createChainCode();
	}
	
	
	private List<Byte> createChainCode(){

		List<Byte> out = new ArrayList<Byte>( m_shape.getPoints().size() );
		long[] prev = null;
		for(long[] point:  m_shape.getPoints()){
			if( prev != null)
				out.add( getChainCode(prev, point));
			prev = point;
		}
		return out;
	}
	
	

	private static byte getChainCode(long[] center, long[] neighboor){
		assert center.length == neighboor.length;
		if( center[0] == neighboor[0]){
			if ( center[1] < neighboor[1]){
				return 2;
			} else if ( center[1] > neighboor[1])
				return 6;
		} else if ( center[0] > neighboor[0]){
			if ( center[1] == neighboor[1]){
				return 4;
			} else  if ( center[1] < neighboor[1]){
				return 3;
			} else
				return 5;
		} else {
			if ( center[1] == neighboor[1]){
				return 0;
			} else  if ( center[1] < neighboor[1]){
				return 1;
			} else
				return 7;
		}
		return 0;
	}
	

}
