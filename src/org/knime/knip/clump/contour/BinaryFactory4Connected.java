package org.knime.knip.clump.contour;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.randomaccessibleinterval.unary.regiongrowing.AbstractRegionGrowing;
import net.imglib2.type.logic.BitType;

public class BinaryFactory4Connected extends AbstractBinaryFactory{

	private int m_dir;
	
	public BinaryFactory4Connected(RandomAccessibleInterval<BitType> img){
		this(img, findStartingPoint(img));
	}
	
	public BinaryFactory4Connected(RandomAccessibleInterval<BitType> img, long[] start){
		super(img, start);
		m_dir = 0;
	}
	
	@Override
	protected long[] nextPoint(RandomAccess<BitType> ra, long[] center) {
		int index = ( m_dir + 3) % 4;
		for(int i = 0 ; i < 4; i++){
			index = (index + i) % 4;
			long[] res = next(center, index);
			ra.setPosition(res);
			if( ra.get().get() ){
				m_dir = index;
				return res;
			}
		}
		return null;
	}
	
	private long[] next(long[] pos, int index){
		long[] newPos = new long[ pos.length ];
		switch(index){
			case 0:
				newPos[0] = pos[0] + 1;
				newPos[1] = pos[1];
				break;
			case 1:
				newPos[0] = pos[0];
				newPos[1] = pos[1] - 1;
				break;
			case 2:
				newPos[0] = pos[0] - 1;
				newPos[1] = pos[1];
				break;
			case 3:
				newPos[0] = pos[0];
				newPos[1] = pos[1] + 1;
				break;
			default:
				throw new RuntimeException( this.getClass().getCanonicalName() + ": Unknown index: " + index);
		}
		return newPos;
	}

	@Override
	public long[][] getStructuringElement() {
		return AbstractRegionGrowing.get4ConStructuringElement(2);
	}

}
