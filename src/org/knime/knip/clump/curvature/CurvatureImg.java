package org.knime.knip.clump.curvature;

import net.imglib2.Interval;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPositionable;
import net.imglib2.collection.PointSampleList;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.knime.knip.clump.contour.Contour;

public class CurvatureImg<T extends RealType<T>> extends PointSampleList<T> 
	implements RandomAccessibleInterval<T>{
	
	private final Img<T> m_img;
	
	private final Contour m_contour;
	
	CurvatureImg(Img<T> img, Contour contour){
		super( 2 );
		m_img = img;
		m_contour = contour;
	}

	@Override
	public RandomAccess<T> randomAccess() {
		return Views.extendPeriodic( m_img ).randomAccess();
	}

	@Override
	public RandomAccess<T> randomAccess(Interval interval) {
		return Views.extendPeriodic( m_img ).randomAccess(interval);
	}

	@Override
	public int numDimensions() {
		return m_img.numDimensions();
	}

	@Override
	public long min(int d) {
		if ( d != 0 )
			throw new RuntimeException();
		return m_img.min(d);
	}

	@Override
	public void min(long[] min) {
		m_img.min(min);
	}

	@Override
	public void min(Positionable min) {
		m_img.min(min);
	}

	@Override
	public long max(int d) {
		if ( d != 0 )
			throw new RuntimeException();
		return m_img.max(d);
	}

	@Override
	public void max(long[] max) {
		m_img.max(max);
		
	}

	@Override
	public void max(Positionable max) {
		m_img.max(max);
	}

	@Override
	public double realMin(int d) {
		return m_img.realMin(d);
	}

	@Override
	public void realMin(double[] min) {
		m_img.realMin(min);
	}

	@Override
	public void realMin(RealPositionable min) {
		m_img.realMin(min);
	}

	@Override
	public double realMax(int d) {
		return m_img.realMax(d);
	}

	@Override
	public void realMax(double[] max) {
		m_img.realMax(max);
	}

	@Override
	public void realMax(RealPositionable max) {
		m_img.realMax(max);
	}

	@Override
	public void dimensions(long[] dimensions) {
		m_img.dimensions(dimensions);		
	}

	@Override
	public long dimension(int d) {
		if ( d != 0 )
			throw new RuntimeException();
		return m_img.dimension(d);
	}

	public Contour getContour() {
		return m_contour;
	}

	public T getType() {
		return m_img.firstElement();
	}

}
