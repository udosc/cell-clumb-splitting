package org.knime.knip.clump.dt;

import java.util.Collection;

import net.imglib2.Point;
import net.imglib2.ops.operation.UnaryOperation;

import org.knime.core.util.Pair;
import org.knime.knip.clump.contour.Contour;
import org.knime.knip.core.data.algebra.Complex;

/**
 * 
 * @author Schlegel
 *
 */
public class EdgePruning 
implements UnaryOperation<Collection<Pair<Point, Point>>, Collection<Pair<Point, Point>>>{

	private final Contour m_contour;
	
	private final double m_beta;
	
	private final double m_t;
	
	public EdgePruning(Contour c, double beta, double t){
		m_contour = c;
		m_beta = beta;
		m_t = t;
	}
	
	@Override
	public Collection<Pair<Point, Point>> compute(
			Collection<Pair<Point, Point>> arg0,
			Collection<Pair<Point, Point>> arg1) {
		for( Pair<Point, Point> e: arg0){
//			Source = i
//			Destination = j
			Complex tangentS = m_contour.getUnitVector(  e.getFirst() , 5);
//			printTangent(tangentS, e.getSource().getPosition(), ra);
			Complex tangentD = m_contour.getUnitVector(  e.getSecond() , 5);
//			printTangent(tangentD, e.getDestination().getPosition(), ra);
//			System.out.println( (tangentS.re() * tangentD.re() + tangentS.im() * tangentD.im() ));

			Complex vectorIJ = new Complex(e.getSecond().getLongPosition(0) - e.getFirst().getLongPosition(0),
					e.getSecond().getLongPosition(1) - e.getFirst().getLongPosition(1));
			final double tmp0 = Math.abs((vectorIJ.re() * tangentS.re() + vectorIJ.im() * tangentS.im()) / vectorIJ.getMagnitude());
			Complex vectorJI = new Complex(e.getFirst().getLongPosition(0) - e.getSecond().getLongPosition(0),
					e.getFirst().getLongPosition(1) - e.getSecond().getLongPosition(1));
			final double tmp1 = Math.abs((vectorJI.re() * tangentD.re() + vectorJI.im() * tangentD.im()) / vectorJI.getMagnitude());
		
			if(  tangentS.re() * tangentD.re() + tangentS.im() * tangentD.im() <= m_t && 
					Math.max(tmp0, tmp1) <= m_beta )
				arg1.add( e );
		}
		return arg1;
	}

	@Override
	public UnaryOperation<Collection<Pair<Point, Point>>, Collection<Pair<Point, Point>>> copy() {
		return new EdgePruning(m_contour, m_beta, m_t);
	}

}
