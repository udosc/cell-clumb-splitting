package org.knime.knip.clump.types;


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
