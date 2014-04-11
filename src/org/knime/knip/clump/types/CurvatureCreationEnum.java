package org.knime.knip.clump.types;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.clump.curvature.factory.CurvatureFactory;
import org.knime.knip.clump.curvature.factory.GaussianCurvature;
import org.knime.knip.clump.curvature.factory.KCosineCurvature;

/**
 * 
 * @author Udo Schlegel
 *
 */
public enum CurvatureCreationEnum{
	K_COSINE,
	GAUSSIAN;
		
	public static<T extends RealType<T> & NativeType<T>> CurvatureFactory<T> getFactory(CurvatureCreationEnum arg, int supportRadius, T type){
		switch (arg) {
			case K_COSINE:
				return new KCosineCurvature<T>(type, supportRadius);
			case GAUSSIAN:
				return new GaussianCurvature<T>(type, supportRadius);
	        default:
	            throw new IllegalArgumentException(CurvatureCreationEnum.class + ": Unknown enum");
		}
	}
	
	public static String[] getNames(){
		String[] out = new String[ CurvatureCreationEnum.values().length ];
		int index = 0;
		for(CurvatureCreationEnum e: CurvatureCreationEnum.values())
			out[index++] = e.name();
		return out;
	}

}
