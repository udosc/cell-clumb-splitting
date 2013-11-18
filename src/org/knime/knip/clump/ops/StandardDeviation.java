package org.knime.knip.clump.ops;

import java.util.Iterator;

import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.ops.operation.iterable.unary.Mean;
import net.imglib2.type.numeric.RealType;

public class StandardDeviation<T extends RealType<T>, V extends RealType<V>>
	implements UnaryOperation<Iterator<T>, V>{

	@Override
	public V compute(Iterator<T> op, V res) {
		double mean = new Mean<T, V>().compute(op, res.createVariable()).getRealDouble();
		double variance = 0.0d;
		int i = 0;
        while( op.hasNext() ){
        	double tmp = op.next().getRealDouble();
            variance += (mean - tmp) * (mean - tmp);
            i++;
        }
        res.setReal( Math.sqrt( variance / i));
        return res;
	}

	@Override
	public UnaryOperation<Iterator<T>, V> copy() {
		return new StandardDeviation<T, V>();
	}

}
