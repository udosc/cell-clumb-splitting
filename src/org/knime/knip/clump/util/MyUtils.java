package org.knime.knip.clump.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.knime.knip.clump.graph.Edge;
import org.knime.knip.clump.graph.Node;
import org.knime.knip.core.util.ImgUtils;

/**
 * 
 * @author Schlegel
 *
 */
public class MyUtils {
	
	private MyUtils(){ };

	public static<T extends RealType<T>> int numElements(RandomAccessibleInterval<T> ra){
		int out = 1;
		for(int i = 0; i < ra.numDimensions(); i++){
			out *= ra.dimension(i);
		}
		return out;
	}
	
	public static int indexOf(Collection<long[]> list, long[] element){
		int index = 0;
		for(long[] l: list){
			if( Arrays.equals(l, element) )
				return index;
			else
				index++;
		}
		return -1;
	}
	
	public static<T extends RealType<T>> double[] toDoubleArray(RandomAccessibleInterval<T> list){
		
		int size = 1;
		for(int i = 0; i < list.numDimensions(); i++)
			size *= list.dimension(i);
		
		double[] res = new double[ size ];
		Cursor<T> c = Views.iterable(list).cursor();
		int i = 0;
		while( c.hasNext() )
			res[i++] = c.next().getRealDouble();
		return res;
	}
	
	public static double[] toDoubleArray(List<? extends Number> list){
		double[] res = new double[list.size()];
		for(int i = 0; i < res.length; i++)
			res[i] = (Double) list.get(i);
		return res;
	}
	
	public static double[] toDoubleArray(long[] array){
		double[] res = new double[array.length];
		for(int i = 0; i < array.length; i++)
			res[i] = array[i];
		return res;
	}
	
	
	public static List<Node> getNodes(Collection<Edge> path){
		List<Node> out = new LinkedList<Node>();
		for(Edge e: path){
			out.add( e.getSource() );
		}
		return out;
	}
	
	
	public static<T extends RealType<T>> List<long[]> getPoints(Collection<Edge> path){
		List<long[]> out = new LinkedList<long[]>();
		for(Edge e: path){
			out.add( e.getSource().getPosition() );
		}
		return out;
	}
	
	public static double calcCos(long[] center, long[] p1, long[] p2){
		
		long scalar = 0L;
		
		
		for(int i = 0; i < center.length; i++){		
			scalar += ( p1[i] - center[i] ) * ( p2[i] - center[i]  );
		}
		
		double d = scalar / (distance(center, p1) * distance(center, p2));
//
//		if( crossProduct(center, p1, p2) < 0.0d )
//			System.out.println( d );
		
//		return Math.abs( d ) * Math.signum( 
//				crossProduct(center, p1, p2));
		
		return ( 1 - Math.abs( d ) ) * 
				Math.signum( crossProduct(center, p1, p2));
		
//		return crossProduct(center, p1, p2);

	}
	//See cormen - algorithm page 1046/46
	//result > 0: p0p1 is clockwise from p0p2
	//result < 0: p0p1 is anticlockwise from p0p2
	public static double crossProduct(long[] p0, long[] p1, long[] p2){
		return (( p1[0] - p0[0]) * ( p2[1] - p0[1])) -
				(( p2[0] - p0[0]) * (p1[1] - p0[1]));
	}
	
	public static double calcAngle(long[] center, long[] p1, long[] p2){
		return Math.acos(
				calcCos(center, p1, p2));
	}
	
	
	
	public static<S extends RealType<S>, T extends RealType<T>> Img<S> createCopy(Img<T> img, S type){
		Img<S> out = ImgUtils.createEmptyCopy(img, type);
		Cursor<S> cOut = out.cursor();
		Cursor<T> cIn = img.cursor();
		while( cOut.hasNext() && cIn.hasNext()){
			cOut.fwd();
			cIn.fwd();
			cOut.get().setReal( cIn.get().getRealDouble());
		}
		return out;
	}
	
	
	public static double distance(long[] p1, long[] p2){
		assert p1.length == p2.length;
		double sum = 0.0d;
		for (int i = 0; i < p1.length; i++) {
			final double delta = p1[i] - p2[i];
			sum += delta * delta;
		}
		return Math.sqrt(sum);
	}
	
}
