package org.knime.knip.clump.contour;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import net.imglib2.AbstractCursor;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.pointset.AbstractPointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.apache.commons.math3.complex.ComplexUtils;
import org.knime.knip.base.exceptions.KNIPRuntimeException;
import org.knime.knip.clump.util.DiscretHelpers;
import org.knime.knip.clump.util.MyUtils;
import org.knime.knip.core.data.algebra.Complex;

/**
 * 
 * @author Udo Schlegel
 *
 */
public class Contour 
	extends AbstractPointSet{
	
	private List<long[]> m_points;
	
	public Contour(List<long[]> points) {
		super();
		init(points);
	}
	
	public Contour(Contour... contour){
		super();
		final List<long[]> res = new LinkedList<long[]>();
		for( Contour c: contour)
			res.addAll( c.getPoints() );
		init( res );
	}
	
	private void init(List<long[]> points){
		
		minBounds = new long[ points.get(0).length ];
		maxBounds = new long[ points.get(0).length ];
		
		m_points = new ArrayList<long[]>(points);
		
		for(long[] pos: m_points){
			for(int i = 0; i < pos.length; i++){
				if( pos[i] > maxBounds[i])
					maxBounds[i] = pos[i];
				else if( pos[i] < minBounds[i])
					minBounds[i] = pos[i];
			}
		}
	}
	
	public Complex[] getContour2D(){
		final Complex[] out = new Complex[ m_points.size() ];
		
		if( m_points.get(0).length > 2 )
			throw new RuntimeException(Contour.class.getCanonicalName() + ": Can only convert 1D or 2D data");
		
		
		for( int i = 0; i < out.length; i++){
			out[ i ] = new Complex(
					m_points.get(i)[0],
					m_points.get(i)[1]);
		}
		
		return out;
	}
	
	public int indexOf(long[] point){
		int index = 0;
		for(long[] l: m_points){
			if( Arrays.equals(l, point) )
				return index;
			else
				index++;
		}
		return -1;
	}
	
	//TODO Test
	public long[] get(int index){
		if( index >= m_points.size() )
			index %= m_points.size() - 1 ;
		if ( index < 0 )
			index = m_points.size() + index - 1;
		return m_points.get( index );
	}
	
	public int length(){
		return m_points.size();
	}
	
	public Iterable<long[]> getIterator(){
		return m_points;
	}
	
	public RandomAccessible<DoubleType> getCoordinates(int dimension){
		final Img<DoubleType> out = new ArrayImgFactory<DoubleType>().create(
				new long[]{m_points.size() }, new DoubleType());
		
		Cursor<DoubleType> c = out.cursor();
		for(long[] p: m_points){
			c.fwd();
			c.get().set( p[dimension] );
		}
		
		return Views.extendPeriodic( out );
	}
	
	//TODO
	public boolean isSimple(){
		return new HashSet<long[]>( m_points ).size() == m_points.size();
	}
	
	public List<long[]> getPointsInbetween(int start, int end){
		List<long[]> out = new LinkedList<long[]>();
		if( start == end ){
			//Return the whole contour
			out.addAll( m_points );
		} else if( start <= end ){
			while( start <= end ){
				out.add( m_points.get(start++));
			}
//			out = m_points.subList(i, j+1);
		} else {
			while( start < m_points.size() ){
				out.add( m_points.get(start++));
			}
			for(int k = 0; k < end; k++){
				out.add( m_points.get(k));
			}

		}
		return out;
		
	}
	
	public List<long[]> getPointsInbetween(long[] start, long[] end){
		
		//TODO
		List<long[]> out = new LinkedList<long[]>();
		int i = MyUtils.indexOf(m_points, start);
		
		if( i == -1 ) 
			throw new KNIPRuntimeException(Contour.class.getCanonicalName() + ": " + start + " not found in the contour!");
		
		int j = MyUtils.indexOf(m_points, end);
		
		if ( j == -1 )
			throw new KNIPRuntimeException(Contour.class.getCanonicalName() + ": " + end + " not found in the contour!");
		
		if( i == j ){
			//Return the whole contour
			out.addAll( m_points );
		} else if( i <= j ){
			while( i <= j ){
				out.add( m_points.get(i++));
			}
//			out = m_points.subList(i, j+1);
		} else {
			while( i < m_points.size() ){
				out.add( m_points.get(i++));
			}
			for(int k = 0; k < j; k++){
				out.add( m_points.get(k));
			}

		}
		return out;
	}
	
	public List<long[]> getPoints(){
		return m_points;
	}
	
	public Complex getUnitVector(long[] pos, int support){
		return getUnitVector(indexOf( pos ), support);
	}
	
	public Complex getUnitVector(int index, int support){
		final double[] filter = MyUtils.toDoubleArray(
				DiscretHelpers.createFirstDerivation1D(support, new DoubleType()));
		
//		double[] filter = new double[]{ 0.0d, -1.0d, -2.0d, 0.0d, 2.0d, 1.0d, 0.0d}; 
		
//		for(int i = 0; i < filter.length; i++){
//			filter[i] *= (2.0d * support + 1.0d) * (2.0d * support + 1.0d);
//			filter[i] /= (2.0d * support + 1.0d);
//		}
		
		double[] x = new double[ 2 * support + 1 ];
		double[] y = new double[ 2 * support + 1 ];
		
		for(int i = 0; i < x.length; i++){
			long[] res = get(index + i - support);
			x[i] = res[0];
			y[i] = res[1];
		}
		
		double deltaX = 0.0d;
		double deltaY = 0.0d;
		for( int i = 0; i < x.length; i++){
			deltaX += x[i] * filter[i];
			deltaY += y[i] * filter[i];
		}
		
//		final double magnitute = new Complex( deltaX, deltaY).getMagnitude();
//		return new Complex( deltaX / magnitute, deltaY/magnitute );
		
		//TODO why do this lead to opposite signs?
//		org.apache.commons.math3.complex.Complex tmp  = ComplexUtils.polar2Complex( 1.0d, Math.atan( deltaY / deltaX ));
//		Complex out = new Complex( tmp.getReal(), tmp.getImaginary());
		double magnitute = Math.sqrt( deltaX * deltaX + deltaY * deltaY);
//		System.out.println( tmp.getReal() + ", " +  tmp.getImaginary() + " - " + deltaX / res + ", " + deltaY / res);
		return new Complex( deltaX / magnitute, deltaY/ magnitute);
	}
	
	public Polygon createPolygon(){
		final int n = m_points.size();
		int[] xpoints = new int[n];
		int[] ypoints = new int[n];
		for(int i = 0; i < n; i++){
			xpoints[i] = (int) m_points.get(i)[0];
			ypoints[i] = (int) m_points.get(i)[1];
		}
		return new Polygon(xpoints, ypoints, n);
	}
	
	@Override
	public PointSetIterator iterator(){
		return new ContourIterator( m_points.get(0).length);
	}

	@Override
	public long[] getOrigin() {
		return m_points.get(0);
	}

	@Override
	public void translate(long[] delta) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int numDimensions() {
		return m_points.get(0).length;
	}

	@Override
	public boolean includes(long[] point) {
		return MyUtils.indexOf(m_points, point) >= 0;
	}

	@Override
	public long size() {
		return m_points.size();
	}

	@Override
	public PointSet copy() {
		return new Contour(m_points);
	}

	@Override
	protected long[] findBoundMin() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected long[] findBoundMax() {
		throw new UnsupportedOperationException();
	}
	
	private class ContourIterator extends AbstractCursor<long[]>
		implements PointSetIterator{

		private int index = -1;
		
		private final int n;
		
		public ContourIterator(int n) {
			super(n);
			this.n = n;
		}

		@Override
		public long[] get() {
			return m_points.get(index);
		}

		@Override
		public void fwd() {
			index++;
		}

		@Override
		public void reset() {
			index = 0;
		}

		@Override
		public boolean hasNext() {
			return index < m_points.size()-1;
		}

		@Override
		public void localize(long[] position) {
			for(int i = 0; i < position.length; i++)
				position[i] = m_points.get(index)[i];
		}

		@Override
		public long getLongPosition(int d) {
			return index;
		}

		@Override
		public AbstractCursor<long[]> copy() {
			return new ContourIterator(n);
		}

		@Override
		public AbstractCursor<long[]> copyCursor() {
			return new ContourIterator(n);
		}
		


	}
}
