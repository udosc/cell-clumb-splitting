package org.knime.knip.clump.boundary;

import org.knime.knip.clump.contour.Contour;

import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class DFTCurvature <T extends RealType<T> & NativeType<T>>
	implements ShapeDescription<T>{

	@Override
	public T getValueOf(long[] point) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Img<T> getValues(long[] start, long[] destination) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Img<T> getImg() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public Contour getContour(){
		//TODO
		return null;
	}

	@Override
	public double[] getValues(int start, int end) {
		// TODO Auto-generated method stub
		return null;
	}

}
