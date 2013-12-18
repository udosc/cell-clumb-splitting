package org.knime.knip.clump.graph;

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.region.BresenhamLine;
import net.imglib2.labeling.LabelingType;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class PrintValidPaths<T extends RealType<T> & NativeType<T>, L extends Comparable<L>> 
implements UnaryOperation<Graph<T>, RandomAccess<LabelingType<L>>> {
	
	
	private final L m_value;
	
	public PrintValidPaths(L value){
		m_value = value;
	}
	
	@Override
	public RandomAccess<LabelingType<L>> compute(Graph<T> input, RandomAccess<LabelingType<L>> output) {
		for(Edge e: input.getValidEdges()){
			draw(output, e, m_value);
		}
		return output;
	}

	@Override
	public UnaryOperation<Graph<T>, RandomAccess<LabelingType<L>>> copy() {
		return new PrintValidPaths<T, L>( m_value );
	}
	
	private void draw(RandomAccess<LabelingType<L>> ra, Point p1, Point p2, L value) {
		final Cursor<LabelingType<L>> cursor = 
				new BresenhamLine<LabelingType<L>>(ra, p1, p2);
		long[] res = new long[ ra.numDimensions() ];
		while( cursor.hasNext() ){
			cursor.fwd();
			cursor.localize( res );
			ra.setPosition(res);
			ra.get().setLabel( m_value );
		}
	}
	
	private void draw(RandomAccess<LabelingType<L>> ra, Edge edge, L value) {
		draw(ra, 
				new Point(edge.getSource().getPosition()),
				new Point(edge.getDestination().getPosition()), 
				value);
	}

}
