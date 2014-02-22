package org.knime.knip.clump.types;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.knime.knip.clump.dist.DynamicTimeWarping;
import org.knime.knip.clump.dist.MinDistance;
import org.knime.knip.clump.dist.ShapeDistance;
import org.knime.knip.clump.dist.contour.ContourDistance;
import org.knime.knip.clump.dist.contour.CurvatureDistance;
import org.knime.knip.clump.dist.contour.DFTDistance;

/**
 * 
 * @author Udo
 *
 */
public enum ContourMeasureEnums {


	CURVATURE,
	DFT_OF_CONTOUR,
	DFT_OF_CURVATURE;
		
//	public static <T extends RealType<T>> ContourDistance<T> getDistanceMeasure(ContourMeasureEnums arg){
//		switch (arg) {
//			case CURVATURE:
//				return new CurvatureDistance<T>(templates, factory, step, exec, sigma);
//			case EUDLIDEAN:
//				return new MinDistance<T>(2);
//	        default:
//	            throw new IllegalArgumentException(DistancesMeasuresEnum.class + ": Unknown Apache Distance enum");
//		}
//	}
	
	public static String[] getNames(){
		String[] out = new String[ ContourMeasureEnums.values().length ];
		int index = 0;
		for(ContourMeasureEnums e: ContourMeasureEnums.values())
			out[index++] = e.name();
		return out;
	}


}
