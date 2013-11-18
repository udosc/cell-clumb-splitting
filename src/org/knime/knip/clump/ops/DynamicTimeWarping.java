package org.knime.knip.clump.ops;

import java.util.List;

import org.apache.commons.math3.ml.distance.DistanceMeasure;

import net.imglib2.ops.operation.BinaryOperation;

public class DynamicTimeWarping
	implements BinaryOperation<List<Double>, List<Double>, Double> {
	
	private final DistanceMeasure m_dist;
	
	public DynamicTimeWarping(DistanceMeasure distance){
		m_dist = distance;
	}

	@Override
	public Double compute(List<Double> l1, List<Double> l2,
			Double output) {
	

        /** Transform the examples to vectors */
        double[] ts1 = new double[l1.size()];
        double[] ts2 = new double[l2.size()];

        for (int i = 0; i < ts1.length; i++) {
            ts1[i] = l1.get(i);
        }

        for (int i = 0; i < ts2.length; i++) {
            ts2[i] = l2.get(i);
        }

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
	
	
    private double pointDistance(int i, int j, double[] ts1, double[] ts2) {
        final double diff = ts1[i] - ts2[j];
        return (diff * diff);
    }

	@Override
	public BinaryOperation<List<Double>, List<Double>, Double> copy() {
		return new DynamicTimeWarping(m_dist);
	}

}