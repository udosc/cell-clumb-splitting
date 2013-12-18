package org.knime.knip.clump.graph;

import java.util.LinkedList;
import java.util.List;

import org.knime.knip.clump.contour.Contour;
import org.knime.knip.clump.contour.ContourFactory;
import org.knime.knip.core.data.algebra.Complex;

import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

public class DelaunayTriangulation<T extends RealType<T> & NativeType<T>>{
	
	private Graph<T> m_graph;
	
	private Contour m_contour;

	public DelaunayTriangulation(Graph<T> graph, Img<BitType> source, ContourFactory<T> cf){
		graph.validate( source , 1);
		m_graph = graph;
		m_contour = cf.createContour();
	}
	
//	public List<Edge> getPrunedEdges(){
//		for( Edge e: getValidEdges()){
//			Complex tangentS = m_contour.getUnitVector(  e.getSource().getIndex() , 5);
//			Complex tangentD = m_contour.getUnitVector(  e.getDestination().getIndex() , 5);
//			System.out.println( tangentS.re() * tangentD.re() + tangentS.im() + tangentD.im() );
//			if( tangentS.re() * tangentD.re() + tangentS.im() + tangentD.im() < -0.15d )
//				m_graph.deleteEdge(e);
//		}
//	}
	

	public List<Edge> getValidEdges(){
		List<Edge> list = new LinkedList<Edge>();
		final Double[][] weights = m_graph.getMatrix();
		for(int i = 0; i < weights.length; i++){
			for(int j = 0; j < weights[i].length; j++){
				if( m_graph.getMatrix()[i][j] >= 0.0d && i != j)
					list.add( 
							new Edge(
									m_graph.getNode(i),
									m_graph.getNode(j),
									weights[i][j]));
			}
		}
		return list;
	}
}
