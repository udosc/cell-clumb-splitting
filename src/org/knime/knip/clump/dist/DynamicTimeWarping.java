package org.knime.knip.clump.dist;

import java.util.List;

import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.knime.knip.clump.boundary.ShapeDescription;
import org.knime.knip.clump.util.MyUtils;

import net.imglib2.img.Img;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class DynamicTimeWarping<T extends RealType<T> & NativeType<T>>
	implements ShapeDistance<T> {
	
	private final DistanceMeasure m_dist;
	
	public DynamicTimeWarping(DistanceMeasure distance){
		m_dist = distance;
	}
	
    private double pointDistance(int i, int j, double[] ts1, double[] ts2) {
        final double diff = ts1[i] - ts2[j];
        return (diff * diff);
    }

	@Override
	public DistanceMeasure getDistanceMeasure() {
		return m_dist;
	}

	@Override
	public BinaryOperation<ShapeDescription<T>, Img<T>, T> copy() {
		return new DynamicTimeWarping<T>(m_dist);
	}
	
	private double compute(double[] ts1,  double[] ts2) {

		double output = 0.0d;
		
        /** Build a point-to-point distance matrix */
        double[][] dP2P = new double[ts1.length][ts2.length];
        for (int i = 0; i < ts1.length; i++) {
            for (int j = 0; j < ts2.length; j++) {
//                dP2P[i][j] = pointDistance(i, j, ts1, ts2);
            	dP2P[i][j] = m_dist.compute(new double[]{ ts1[i]}, new double[]{ ts2[j]});
            }
        }

        /** Check for some special cases due to ultra short time series */
        if (ts1.length == 0 || ts2.length == 0) {
        	output = 0.0d;
            return output;
        }


        /**
         * Build the optimal distance matrix using a dynamic programming
         * approach
         */
        double[][] D = new double[ts1.length][ts2.length];

        D[0][0] = dP2P[0][0]; // Starting point

        for (int i = 1; i < ts1.length; i++) { // Fill the first column of our
            // distance matrix with optimal
            // values
            D[i][0] = dP2P[i][0] + D[i - 1][0];
        }

        if (ts2.length == 1) { // TS2 is a point
            double sum = 0;
            for (int i = 0; i < ts1.length; i++) {
                sum += D[i][0];
            }
            output = Math.sqrt(sum) / ts1.length;
            return output;
        }

        for (int j = 1; j < ts2.length; j++) { // Fill the first row of our
            // distance matrix with optimal
            // values
            D[0][j] = dP2P[0][j] + D[0][j - 1];
        }

        if (ts1.length == 1) { // TS1 is a point
            double sum = 0;
            for (int j = 0; j < ts2.length; j++) {
                sum += D[0][j];
            }
            output =Math.sqrt(sum) / ts2.length;
            return output;
        }

        for (int i = 1; i < ts1.length; i++) { // Fill the rest
            for (int j = 1; j < ts2.length; j++) {
                double[] steps = { D[i - 1][j - 1], D[i - 1][j], D[i][j - 1] };
                double min = Math.min(steps[0], Math.min(steps[1], steps[2]));
                D[i][j] = dP2P[i][j] + min;
            }
        }


        int i = ts1.length - 1;
        int j = ts2.length - 1;
        int k = 1;
        double dist = D[i][j];

        while (i + j > 2) {
            if (i == 0) {
                j--;
            } else if (j == 0) {
                i--;
            } else {
                double[] steps = { D[i - 1][j - 1], D[i - 1][j], D[i][j - 1] };
                double min = Math.min(steps[0], Math.min(steps[1], steps[2]));

                if (min == steps[0]) {
                    i--;
                    j--;
                } else if (min == steps[1]) {
                    i--;
                } else if (min == steps[2]) {
                    j--;
                }
            }
            k++;
            dist += D[i][j];
        }
        output =  Math.sqrt(dist) / k ;
        return output;	
	}

	@Override
	public T compute(ShapeDescription<T> inputA, Img<T> inputB, T output) {
		double res = 
				compute(MyUtils.toDoubleArray(inputA.getImg()), MyUtils.toDoubleArray(inputB));
		
		output.setReal( res );
		
		return output;
	}

}
