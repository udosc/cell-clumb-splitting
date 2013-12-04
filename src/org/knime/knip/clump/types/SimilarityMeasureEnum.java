package org.knime.knip.clump.types;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.knime.knip.clump.dist.DFTDistance;
import org.knime.knip.clump.dist.DynamicTimeWarping;
import org.knime.knip.clump.dist.MinDistance;
import org.knime.knip.clump.dist.ShapeDistance;

/**
 * 
 * @author Udo
 *
 */
public enum SimilarityMeasureEnum {

	FOURIER,
	DTW,
	EUDLIDEAN;
		
	public static <T extends RealType<T> & NativeType<T>> ShapeDistance<T> getDistanceMeasure(SimilarityMeasureEnum arg){
		switch (arg) {
			case FOURIER:
				return new DFTDistance<T>(new EuclideanDistance(), 16);
			case DTW:
				return new DynamicTimeWarping<T>(new EuclideanDistance() );
			case EUDLIDEAN:
				return new MinDistance<T>(new EuclideanDistance());
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
