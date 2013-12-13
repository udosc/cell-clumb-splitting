package org.knime.knip.clump.boundary;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.outofbounds.OutOfBoundsPeriodicFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.ImgUtil;
import net.imglib2.view.Views;

import org.knime.knip.clump.util.MyUtils;
import org.knime.knip.core.algorithm.convolvers.DirectConvolver;
import org.knime.knip.core.algorithm.convolvers.filter.linear.DerivativeOfGaussian;
import org.knime.knip.core.algorithm.convolvers.filter.linear.LaplacianOfGaussian;
import org.knime.knip.core.ops.filters.GaussNativeTypeOp;
import org.knime.knip.core.util.ImgUtils;

/**
 * 
 * @author Udo Schlegel
 *
 */
public class Curvature<T extends RealType<T> & NativeType<T>>
	implements ShapeDescription<T>, Iterable<T>{
	
//	private final List<T> m_curvature;
		
	private final Contour m_contour;
	
	private Img<T> m_img;
	
	private final RandomAccess<T> m_randomAccess;
		
	
	public Curvature(Contour contour, int order, T type){
		m_contour = contour;
//		m_curvature = new ArrayList<T>( contour.length());

		m_img = new ArrayImgFactory<T>().create(new long[]{ contour.length() }, type);

//		m_img = calc(2);
		
		m_randomAccess = Views.extendPeriodic( m_img ).randomAccess();
		
		
		Cursor<T> c = m_img.cursor();
		
		for(int i = 0; i < contour.length(); i++){
			T t = type.createVariable();
			t.setReal(  curvature(i, order) );
//			t.setReal( cTemp.next().getRealDouble() );
//			m_curvature.add(i, t);
			c.next().set(t);
		}
	}
	
	public Curvature(Contour contour, Img<T> curvature){
		
		assert contour.length() == curvature.dimension(0);
		
		m_contour = contour;
		m_img = curvature;
		
		m_randomAccess = Views.extendPeriodic( m_img ).randomAccess();
		
//		m_curvature = new ArrayList<T>( contour.length() );
//		Cursor<T> c = m_img.cursor();
//		for(int i = 0; i < curvature.size(); i++){
//			c.fwd();
//			m_curvature.add(i, c.get().copy() );
//		}
	}
	
	
	public Curvature<T> gaussian(double sigma, ExecutorService exectutor){
		//Gaussion fitering to remove noise
		Img<T> res = ImgUtils.createEmptyCopy(m_img);
		new GaussNativeTypeOp<T, RandomAccessibleInterval<T>>(exectutor, 
				new double[]{ sigma, 0.0d}, 
				new OutOfBoundsPeriodicFactory<T, RandomAccessibleInterval<T>>())
				.compute(m_img, res);
		return new Curvature<T>(m_contour, res);
	}
	
	private double curvature(int position, int order){
		
//		long[] current = m_contour.get( position );
//		
//		long[] next = m_contour.get( position + order < m_contour.length() ?
//				position + order : (position + order ) - m_contour.length() );
//		
//		long[] prev = m_contour.get(position - order  >= 0 ?
//				position - order  : (position - order  ) + m_contour.length() );
		return  MyUtils.calcCos(
				m_contour.get(position), 
				m_contour.get(position - order), 
				m_contour.get( position + order )) ;
		
//		double dprev = MyUtils.distance(prev, current);
//		double dnext = MyUtils.distance(next, current);
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
		

		
		
	}
	
	public Img<T> calc(int order){
		Img<T> out = ImgUtils.createEmptyCopy(m_img);
		Cursor<T> c = out.cursor();
		
		
		RandomAccess<T> d1xRandomAccess = new DirectConvolver<DoubleType, T, T>().compute(
				m_contour.getCoordinates(0), 
				createFirstDerivation1D( 5 ), 
				ImgUtils.createEmptyCopy(m_img)).randomAccess();
		
		RandomAccess<T> d1yRandomAccess = new DirectConvolver<DoubleType, T, T>().compute(
				m_contour.getCoordinates(1), 
				createFirstDerivation1D(5), 
				ImgUtils.createEmptyCopy(m_img)).randomAccess();
		
		RandomAccess<T> d2xRandomAccess = new DirectConvolver<DoubleType, T, T>().compute(
				m_contour.getCoordinates(0), 
				createSecondDerivation1D(5), 
				ImgUtils.createEmptyCopy(m_img)).randomAccess();
		
		RandomAccess<T> d2yRandomAccess = new DirectConvolver<DoubleType, T, T>().compute(
				m_contour.getCoordinates(1), 
				createSecondDerivation1D(5), 
				ImgUtils.createEmptyCopy(m_img)).randomAccess();
		
//		RandomAccess<T> cdx1 = createFirstDerivation(order, 0).randomAccess();
//		RandomAccess<T> cdx2 = createSecondDerivation(order, 0).randomAccess();
//		
//		RandomAccess<T> cdy1 = createFirstDerivation(order, 1).randomAccess();
//		RandomAccess<T> cdy2 = createSecondDerivation(order, 1).randomAccess();
		
		for(int i = 0; i < out.dimension(0); i++){
			d1xRandomAccess.setPosition(i, 0);
			d2xRandomAccess.setPosition(i, 0);
			d1yRandomAccess.setPosition(i, 0);
			d2yRandomAccess.setPosition(i, 0);
			final double d1x = d1xRandomAccess.get().getRealDouble();
			final double d1y = d1yRandomAccess.get().getRealDouble();
			final double d2x = d2xRandomAccess.get().getRealDouble();
			final double d2y = d2yRandomAccess.get().getRealDouble();
			final double t = ((d1x * d2y) - (d2x * d1y));
			final double r = Math.pow(( d1x * d1x ) +  (d1y * d1y), 1.5d);
			c.next().setReal( t / r );
		}
		
//		for(int i = 0; i < m_contour.length(); i++){
//			long[] current = m_contour.get( i );
//			
//			long[] forward = m_contour.get( i + order < m_contour.length() ?
//					i + order : (i + order ) - m_contour.length() );
//			
//			long[] before = m_contour.get(i - order  >= 0 ?
//					i - order  : (i - order  ) + m_contour.length() );
//			
////			double a0 = current[0];
//			double a1 =  ( forward[0] - before[0] ) / 2.0d;
//			double a2 =   ( ( forward[0] + before[0] ) / 2.0d ) - current[0];
//			
////			double b0 = current[1];
//			double b1 =  ( forward[1] - before[1] ) / 2.0d;
//			double b2 =   ( ( forward[1] + before[1] ) / 2.0d ) - current[1];
//			
//			c.next().setReal( ( 2.0d * ( a1 * b2 - a2 * b1 )) / Math.pow(a1 * a1 + b1 * b1, 1.5d));
//			
//
//		}
		
		return out;
	}
	
	private Img<T> createFirstDerivation(int h, int dimension){
		Img<T> out = ImgUtils.createEmptyCopy( m_img );
		RandomAccess<DoubleType> ra = m_contour.getCoordinates(dimension).randomAccess();
//		int[] x = null;
//		if( dimension == 0)
//			x = m_contour.createPolygon().xpoints;
//		else if ( dimension == 1)
//			x = m_contour.createPolygon().ypoints;
//		Img<DoubleType> img = new ArrayImgFactory<DoubleType>().create(new long[]{ x.length }, new DoubleType());
//		Cursor<DoubleType> c = img.cursor();
//		for(int i = 0; i < x.length; i++){
//			c.fwd();
//			c.get().setReal( x[i] );
//		}
//		RandomAccess<DoubleType> ra = Views.extendPeriodic( img ).randomAccess();
		RandomAccess<T> rOut = out.randomAccess();
		for(int t = 0; t < out.dimension(0); t++){
			ra.setPosition(t - 2 * h, 0);
			double prev2 = ra.get().getRealDouble();
			ra.setPosition(t - h, 0);
			double prev1 = ra.get().getRealDouble();
			ra.setPosition(t + h, 0);
			double next1 = ra.get().getRealDouble();
			ra.setPosition(t + 2 * h, 0);
			double next2 = ra.get().getRealDouble();
			rOut.setPosition(t, 0);
			rOut.get().setReal( 
					1.0d/(12.0d*h) * 
					( prev2 + 8.0d * prev1 - 8.0d * next1 + next2));
		}
		return out;
	}
	
	private Img<T> createSecondDerivation(int h, int dimension){
		Img<T> out = ImgUtils.createEmptyCopy( m_img );
		RandomAccess<DoubleType> ra = m_contour.getCoordinates(dimension).randomAccess();
//		int[] x = null;
//		if( dimension == 0)
//			x = m_contour.createPolygon().xpoints;
//		else if ( dimension == 1)
//			x = m_contour.createPolygon().ypoints;
//		Img<DoubleType> img = new ArrayImgFactory<DoubleType>().create(new long[]{ x.length }, new DoubleType());
//		Cursor<DoubleType> c = img.cursor();
//		for(int i = 0; i < x.length; i++){
//			c.fwd();
//			c.get().setReal( x[i] );
//		}
//		RandomAccess<DoubleType> ra = Views.extendPeriodic( img ).randomAccess();
		RandomAccess<T> rOut = out.randomAccess();
		for(int t = 0; t < out.dimension(0); t++){
			ra.setPosition(t - 2*h, 0);
			double prev2 = ra.get().getRealDouble();
			ra.setPosition(t - h, 0);
			double prev1 = ra.get().getRealDouble();
			ra.setPosition(t, 0);
			double actual = ra.get().getRealDouble();
			ra.setPosition(t + h, 0);
			double next1 = ra.get().getRealDouble();
			ra.setPosition(t + 2 * h, 0);
			double next2 = ra.get().getRealDouble();
			rOut.setPosition(t, 0);
			rOut.get().setReal( 
					1.0d/(12.0d*h*h) * 
					( -prev2 + 16.0d*prev1 - 30.0d*actual + 16.0d*next1 - next2));
		}
		return out;
	}
	
	private Img<T> createFirstDerivation1D(int support){
		Img<T> out = new ArrayImgFactory<T>().create(
				new long[]{support * 2 + 1, 1}, m_img.firstElement().createVariable());
		
		Cursor<DoubleType> cursor = 
				Views.hyperSlice(
						new DerivativeOfGaussian(support, 0.75d * 2 * Math.PI, 1.0d , 1),
						1,
						support).cursor();
		
		Cursor<T> outC = out.cursor();
		while( outC.hasNext() ){
			cursor.fwd();
			outC.next().setReal( cursor.get().getRealDouble() );
		}
		return out;
	}
	
	private Img<T> createSecondDerivation1D(int support ){
		Img<T> out = new ArrayImgFactory<T>().create(
				new long[]{support * 2 + 1, 1}, m_img.firstElement().createVariable());
		
		Cursor<DoubleType> cursor = 
				Views.hyperSlice(
						new LaplacianOfGaussian(support, 1.0d),
						1,
						support).cursor();
		
		Cursor<T> outC = out.cursor();
		while( outC.hasNext() ){
			cursor.fwd();
			outC.next().setReal( cursor.get().getRealDouble() );
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
					getCurvature( m_contour.indefOf(points.get(i))  ));
//			System.out.println( m_curvature.get( m_contour.indefOf(points.get(i))  ) );
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
		for(int i= 0; i < m_img.dimension(0); i++){
			ra.setPosition( m_contour.get(i) );
			m_randomAccess.setPosition(i, 0);
			ra.get().set( m_randomAccess.get().getRealDouble() );
		}
		return ra;
	}

	@Override
	public int getSize() {
		return m_contour.length();
	}

	public T getCurvature(int pos){
//		if( pos >= m_curvature.size() )
//			pos %= m_curvature.size();
//		if ( pos < 0 )
//			pos = m_curvature.size() + pos - 1;
//		return m_curvature.get( pos );
		m_randomAccess.setPosition(pos, 0);
		return m_randomAccess.get();
	}
	
	@Override
	public Contour getContour(){
		return m_contour;
	}


	@Override
	public Iterator<T> iterator() {
		return m_img.iterator();
	}
	
//	public void setCurvature(Img<T> img){
//		assert img.iterationOrder().equals( m_img.iterationOrder() );
//		m_img = img;
//		RandomAccess<T> ra = Views.hyperSlice(img, 1, 0).randomAccess();
//		for(int i = 0; i < m_curvature.size(); i++){
//			ra.setPosition(i, 0);
//			m_curvature.set(i, ra.get());
//		}
//	}
	
//	public List<Integer> getZeroCrossings(){
//		List<Integer> out = new LinkedList<Integer>();
//		for(int i = 0; i < m_curvature.size(); i++){
//			double prev =  getCurvature(i-1).getRealDouble();
//			double actual =  getCurvature(i).getRealDouble();
//			double next =  getCurvature(i+1).getRealDouble();
//			if( ( actual > next && actual > prev )
//					|| ( actual < next && actual < prev) )
//				out.add(i);
//		}
//		return out;
//	}
	
	public List<Integer> getZeroCrossings(){
		List<Integer> out = new LinkedList<Integer>();
		for(int i = 0; i < m_img.dimension(0); i++){
			double prev =  getCurvature(i-1).getRealDouble();
			double actual =  getCurvature(i).getRealDouble();
			double next =  getCurvature(i+1).getRealDouble();
			if( Math.signum(prev) != Math.signum(actual) )
				out.add( i );
		}
		return out;
	}
	

	//TODO
	@Override
	public double[] getValues(int start, int end) {
//		double[] res = new double[ Math.abs( end - start )];
//		for( int i = 0; i < res.length; i++){
//			res[i] = getCurvature( start ).getRealDouble();
//		}
//		return res;
		return MyUtils.toDoubleArray(
				this.getValues(
						m_contour.get(start), 
						m_contour.get(end)));
	}
}
