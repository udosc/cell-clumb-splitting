package org.knime.knip.clump.dist;

import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.knime.knip.clump.boundary.ShapeDescription;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class EarthMoversDistance<T extends RealType<T> & NativeType<T>> 
	implements ShapeDistance<T>{

	@Override
	public T compute(ShapeDescription<T> inputA, Img<T> inputB, T output) {
		double lastDistance = 0;
        double totalDistance = 0;
        Cursor<T> a = inputA.getImg().cursor();
        Cursor<T> b  = inputB.cursor();
        while ( a.hasNext() ) {
        	a.fwd();
        	b.fwd();
            final double currentDistance = (a.get().getRealDouble() + lastDistance) - b.get().getRealDouble();
            totalDistance += Math.abs(currentDistance);
            lastDistance = currentDistance;
        }
        output.setReal( totalDistance );
        return output;
	}

	@Override
	public BinaryOperation<ShapeDescription<T>, Img<T>, T> copy() {
		return new EarthMoversDistance<T>();
	}

	@Override
	public DistanceMeasure getDistanceMeasure() {
		return null;
	}

}
