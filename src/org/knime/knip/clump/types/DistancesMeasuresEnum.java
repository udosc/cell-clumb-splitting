package org.knime.knip.clump.types;

import org.apache.commons.math3.ml.distance.CanberraDistance;
import org.apache.commons.math3.ml.distance.ChebyshevDistance;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.ml.distance.ManhattanDistance;

/**
 * 
 * @author Udo Schlegel
 *
 */
public enum DistancesMeasuresEnum {
	CANBERRA,
	MANHATTAN,
	EUDLIDEAN,
	CHEBYSHEV;
	
	public static DistanceMeasure getDistanceMeasure(DistancesMeasuresEnum arg){
		switch (arg) {
			case CANBERRA:
				return new CanberraDistance();
			case MANHATTAN:
				return new ManhattanDistance();
			case EUDLIDEAN:
				return new EuclideanDistance();
			case CHEBYSHEV:
				return new ChebyshevDistance();
	        default:
	            throw new IllegalArgumentException(DistancesMeasuresEnum.class + ": Unknown Apache Distance enum");
		}
	}
	
	public static String[] getNames(){
		String[] out = new String[ DistancesMeasuresEnum.values().length ];
		int index = 0;
		for(DistancesMeasuresEnum e: DistancesMeasuresEnum.values())
			out[index++] = e.name();
		return out;
	}

}
