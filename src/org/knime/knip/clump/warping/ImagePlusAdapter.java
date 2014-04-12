package org.knime.knip.clump.warping;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.awt.image.BufferedImage;
import java.util.Iterator;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.IterableRealInterval;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RealPositionable;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.numeric.RealType;

/**
 * 
 * @author Schlegel
 *
 * @param <T>
 */
public class ImagePlusAdapter<T extends RealType<T>> 
	implements Img<T>{
	
	private Img<T> m_img;
	
	public ImagePlusAdapter(ImagePlus ij, ImgFactory<T> factory,T type){
		BufferedImage awtImage = ij.getBufferedImage();
		
		final ImageProcessor ip = ij.getProcessor();
		
		m_img = factory.create(new long[]{ awtImage.getWidth(), awtImage.getHeight()} , type.createVariable());
		
		Cursor<T> c = m_img.localizingCursor();
		int[] pos = new int[2];
		while( c.hasNext() ){
			c.fwd();
			c.localize(pos);
			c.get().setReal( ip.get(pos[0], pos[1]));
		}
		
	}

	@Override
	public RandomAccess<T> randomAccess() {
		return m_img.randomAccess();
	}

	@Override
	public RandomAccess<T> randomAccess(Interval interval) {
		return m_img.randomAccess(interval);
	}

	@Override
	public int numDimensions() {
		return m_img.numDimensions();
	}

	@Override
	public long min(int d) {
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
		return m_img.dimension(d);
	}

	@Override
	public Cursor<T> cursor() {
		return m_img.cursor();
	}

	@Override
	public Cursor<T> localizingCursor() {
		return m_img.localizingCursor();
	}

	@Override
	public long size() {
		return m_img.size();
	}

	@Override
	public T firstElement() {
		return m_img.firstElement();
	}

	@Override
	public Object iterationOrder() {
		return m_img.iterationOrder();
	}

	@Override
	public boolean equalIterationOrder(IterableRealInterval<?> f) {
		return m_img.equalIterationOrder(f);
	}

	@Override
	public Iterator<T> iterator() {
		return m_img.iterator();
	}

	@Override
	public ImgFactory<T> factory() {
		return m_img.factory();
	}

	@Override
	public Img<T> copy() {
		return m_img.copy();
	}

}
