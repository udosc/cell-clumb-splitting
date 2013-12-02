package org.knime.knip.clump.boundary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.knime.knip.clump.util.MyUtils;
import org.knime.knip.core.ops.filters.GaussNativeTypeOp;
import org.knime.knip.core.util.ImgUtils;

/**
 * 
 * @author Udo Schlegel
 *
 */
public class Curvature<T extends RealType<T> & NativeType<T>>
	implements ShapeDescription<T>, Iterable<T>{
	
	private final List<T> m_curvature;
		
	private final Contour m_contour;
	
	private Img<T> m_img;
		
	
	public Curvature(Contour contour, int order, T type){
		m_contour = contour;
		m_curvature = new ArrayList<T>( contour.length());
		m_img = new ArrayImgFactory<T>().create(new long[]{ contour.length(), 1 }, type);
		
		
		Cursor<T> c = m_img.cursor();
		
		for(int i = 0; i < contour.length(); i++){
			T t = type.createVariable();
			t.setReal(  curvature(i, order) );
//			t.setReal( cTemp.next().getRealDouble() );
			m_curvature.add(i, t);
			c.next().set(t);
		}
	}
	
	
	public Img<T> gaussian(double sigma, ExecutorService exectutor){
		//Gaussion fitering to remove noise
		Img<T> res = ImgUtils.createEmptyCopy(m_img);
		new GaussNativeTypeOp<T, RandomAccessibleInterval<T>>(exectutor, 
				new double[]{ sigma, 0.0d}, 
				new OutOfBoundsMirrorFactory<T, RandomAccessibleInterval<T>>(OutOfBoundsMirrorFactory.Boundary.DOUBLE))
				.compute(m_img, res);
		m_img = res;
		return m_img;
	}
	
	private double curvature(int position, int order){
		
		long[] current = m_contour.get( position );
		
		long[] next = m_contour.get( position + order < m_contour.length() ?
				position + order : (position + order ) - m_contour.length() );
		
		long[] prev = m_contour.get(position - order  >= 0 ?
				position - order  : (position - order  ) + m_contour.length() );
		
//		double dprev = ClumpUtils.distance(prev, current);
//		double dnext = ClumpUtils.distance(next, current);
//		
//		double tprev =  0.5 * Math.PI;
//		double tnext =  0.5 * Math.PI;
//		if (  Math.abs( prev[1] - current[1]) != 0L )
//			tprev = Math.atan( Math.abs(prev[0] - current[0]) / Math.abs( prev[1] - current[1]) );
//		if (  Math.abs( Math.abs( next[1] - current[1])) != 0L )
//			tnext = Math.atan( Math.abs(next[0] - current[0]) / Math.abs( next[1] - current[1]) );
//		
//		double tmean = 0.5 * ( tprev + tnext);
//		double diffPrev = Math.abs( tprev - tmean);
//		double diffNext = Math.abs( tnext - tmean);
//		
//		return ( diffPrev/(2*dprev)) + (diffNext/(2*dnext));
		
		return  1 - Math.abs(MyUtils.calcCos(m_contour.get(position), next, prev) );
		
		
	}
	
	public Img<T> calc(int order){
		Img<T> out = ImgUtils.createEmptyCopy(m_img);
		Cursor<T> c = out.cursor();
		
		
//		Cursor<T> cdx1 = Views.iterable( new DirectConvolver<UnsignedIntType, T, T>().compute(
//				Views.extendZero(m_contour.getCoordinates(0)), 
//				createFirstDerivation(), 
//				ImgUtils.createEmptyCopy(m_img))).cursor();
//		
//		Cursor<T> cdy1 = Views.iterable( new DirectConvolver<UnsignedIntType, T, T>().compute(
//				Views.extendZero(m_contour.getCoordinates(1)), 
//				createFirstDerivation(), 
//				ImgUtils.createEmptyCopy(m_img))).cursor();
//		
//		Cursor<T> cdx2 = Views.iterable( new DirectConvolver<UnsignedIntType, T, T>().compute(
//				Views.extendZero(m_contour.getCoordinates(0)), 
//				createSecondDerivation(), 
//				ImgUtils.createEmptyCopy(m_img))).cursor();
//		
//		Cursor<T> cdy2 = Views.iterable( new DirectConvolver<UnsignedIntType, T, T>().compute(
//				Views.extendZero(m_contour.getCoordinates(1)), 
//				createSecondDerivation(), 
//				ImgUtils.createEmptyCopy(m_img))).cursor();
//		
//		while(c.hasNext()){
//			final double dx1 = cdx1.next().getRealDouble();
//			final double dy1 = cdy1.next().getRealDouble();
//			double tt = (dx1 * cdy2.next().getRealDouble()) - (cdx2.next().getRealDouble() * dy1);
//			double rr = Math.pow(( dx1 * dx1 + dy1 * dy1), 1.5d);
//			c.next().setReal( 
//					 tt / rr );
//		}
		
		for(int i = 0; i < m_contour.length(); i++){
			long[] current = m_contour.get( i );
			
			long[] forward = m_contour.get( i + order < m_contour.length() ?
					i + order : (i + order ) - m_contour.length() );
			
			long[] before = m_contour.get(i - order  >= 0 ?
					i - order  : (i - order  ) + m_contour.length() );
			
//			double a0 = current[0];
			double a1 =  ( forward[0] - before[0] ) / 2.0d;
			double a2 =   ( ( forward[0] + before[0] ) / 2.0d ) - current[0];
			
//			double b0 = current[1];
			double b1 =  ( forward[1] - before[1] ) / 2.0d;
			double b2 =   ( ( forward[1] + before[1] ) / 2.0d ) - current[1];
			
			c.next().setReal( ( 2.0d * ( a1 * b2 - a2 * b1 )) / Math.pow(a1 * a1 + b1 * b1, 1.5d));
			

		}
		
		return out;
	}
	
	private Img<T> createFirstDerivation(){
		Img<T> out = new ArrayImgFactory<T>().create(
				new long[]{3, 1}, m_img.firstElement().createVariable());
		Cursor<T> c = out.cursor();
		for(double d: new double[]{1, 0, -1}){
			c.next().setReal(d);
		}
		return out;
	}
	
	private Img<T> createSecondDerivation(){
		Img<T> out = new ArrayImgFactory<T>().create(
				new long[]{3, 1}, m_img.firstElement().createVariable());
		Cursor<T> c = out.cursor();
		for(double d: new double[]{1, -2, 1}){
			c.next().setReal(d);
		}
		return out;
	}
	
	@Override
	public Img<T> getImg(){
		return m_img;
	}
	

	@Override
	public T getValueOf(long[] point) {
		RandomAccess<T> ra = m_img.randomAccess();
		ra.setPosition(point);
		return ra.get();
	}

	@Override
	public Img<T> getValues(long[] start, long[] end) {
		//TODO using the shape boundary directly
		List<long[]> points = m_contour.getPointsInbetween(start, end);
		Img<T>  out = new ArrayImgFactory<T>().create(
				new long[]{ points.size(), 1}, m_img.firstElement());
		
		Cursor<T> c = Views.iterable( out ).cursor();
		for(int i = 0; i < points.size(); i++){
			c.next().set( 
					m_curvature.get( m_contour.indefOf(points.get(i))  ));
		}
		return out;
	}
	
	@Override
	public T getType(){
		return m_img.firstElement().createVariable();
	}
	
	public long[] getPosition(long index){
		return m_contour.get((int)index);
	}
	
	public RandomAccess<DoubleType> print(RandomAccess<DoubleType> ra){
		for(int i= 0; i < m_curvature.size(); i++){
			ra.setPosition( m_contour.get(i) );
			ra.get().set( m_curvature.get(i).getRealDouble() );
		}
		return ra;
	}

	@Override
	public int getSize() {
		return m_contour.length();
	}

	public T getCurvature(int pos){
		if( pos >= m_curvature.size() )
			pos %= m_curvature.size();
		if ( pos < 0 )
			pos = m_curvature.size() + pos - 1;
		return m_curvature.get( pos );
	}
	
	@Override
	public Contour getContour(){
		return m_contour;
	}


	@Override
	public Iterator<T> iterator() {
		return m_curvature.iterator();
	}


	@Override
	public double[] getValues(int start, int end) {
		double[] res = new double[ Math.abs( end - start )];
		for( int i = 0; i < res.length; i++){
			res[i] = getCurvature( start ).getRealDouble();
		}
		return res;
	}
}
