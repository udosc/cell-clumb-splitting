package org.knime.knip.clump.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.region.BresenhamLine;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.knime.core.util.Pair;
import org.knime.knip.clump.boundary.ShapeDescription;
import org.knime.knip.clump.dist.ShapeDistance;
import org.knime.knip.clump.util.MyUtils;

public class Graph2<T extends RealType<T> & NativeType<T>> {

	private final Double[][] m_weights;
	
	private List<Node> m_nodes;
	
	private ShapeDescription<T> m_shape;
	
	public Graph2(ShapeDescription<T> shape, 
			List<long[]> splittingPoints, 
			Img<BitType> source){
		m_shape = shape;
		m_weights = new Double[splittingPoints.size()][];
		m_nodes = new ArrayList<Node>( splittingPoints.size() );
		for( int i = 0; i < splittingPoints.size(); i++){
			for( int j = 0; j < splittingPoints.size(); j++){
				if(i != j && isValidate(source, 
						splittingPoints.get(i), 
						splittingPoints.get(j)))
					m_nodes.add( new Node(splittingPoints.get(i), splittingPoints.get(j)));
			}
		}
		

		
	}
	
	public Pair<long[], long[]> calc(ShapeDistance<T> dist, 
			Collection<ShapeDescription<T>> templates, double factor){
		
		double min = Double.MAX_VALUE;
		Node best = null;
		for(Node n: m_nodes){
			Pair<long[], long[]> p = n.getPoints();
			for(ShapeDescription<T> template: templates){
				double d = Double.MAX_VALUE;
				
				Img<T> boundary = m_shape.getValues(p.getFirst(), p.getSecond());
				
				if ( boundary.dimension(0) < 2  ){
					continue;
				} 
				
				T w = dist.compute(template.getImg(), 
						boundary, 
						m_shape.getType());
				
				
//				System.out.println( "1 - " + ( boundary.dimension(0) -1 ) / (double)m_shape.getSize() );
				w.mul( boundary.dimension(0) / (double)m_shape.getSize() );
			
				boundary = m_shape.getValues(p.getSecond(), p.getFirst());
				if ( boundary.dimension(0) < 2  ){
					continue;
				} 
				
				T res =  dist.compute(template.getImg(), 
						boundary, 
						m_shape.getType());
//				System.out.println( "2 - " + ( boundary.dimension(0) )/ (double)m_shape.getSize() );
				res.mul( boundary.dimension(0) / (double)m_shape.getSize() );
				
				final double distance = 
						dist.getDistanceMeasure().compute( 
								MyUtils.toDoubleArray(p.getFirst()), 
								MyUtils.toDoubleArray(p.getSecond()));
				
				d = res.getRealDouble() + w.getRealDouble() ;
				
				System.out.println(n.m_points.getFirst()[0] + "," + n.m_points.getFirst()[1] + " to " +
						n.m_points.getSecond()[0] + "," + n.m_points.getSecond()[1] +
						" Distance: " +  d );
				
				if( d < min ){
					min = d;
					best = n;
				}
			}
		}
		System.out.println( min );
		for(ShapeDescription<T> template: templates){
			T res =  dist.compute(template.getImg(), 
					m_shape.getImg(), 
					m_shape.getType());
			if ( res.getRealDouble() < min ){
				System.out.println( "!" + res.getRealDouble() );
				return null;
			}

		}
		
		if( best == null ) return null;
		return best.getPoints();
		
	}
	
	public boolean isValidate(Img<BitType> img, long[] p1, long[] p2){
		RandomAccess<BitType> ra = img.randomAccess();

		Cursor<BitType> cursor = 
				new BresenhamLine<BitType>(ra, 
						new Point(p1), 
						new Point(p2));
		while( cursor.hasNext() ){
			if ( !cursor.next().get() ){
				return false;
			}

		}
		return true;
	}
	
	class Node{
		
		private Pair<long[], long[]> m_points;
		
		public Node(long[] p1, long[] p2){
			m_points = new Pair<long[], long[]>(p1, p2);
		}
		
		public Pair<long[], long[]> getPoints(){
			return m_points;
		}
	}
}
