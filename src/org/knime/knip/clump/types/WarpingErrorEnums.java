package org.knime.knip.clump.types;

public enum WarpingErrorEnums {
	
	MERGE( 1 ),
	SPLIT( 2 ),
	HOLE_ADDITION( 4 ),
	OBJECT_DELETION( 8 ),
	OBJECT_ADDITION( 16 ),
	HOLE_DELETION( 32 ),
	DEFAULT_FLAGS( 63 );
	
	private int m_value;
	
	private int m_errors;
	
	WarpingErrorEnums(int value){
		m_value = value;
	}
	
	public int getValue(){
		return m_value;
	}
	
	
	public void setNumberOfErrors(int errors){
		m_errors = errors;
	}

	public int getNumberOfErrors(){
		return m_errors;
	}
	
	public static WarpingErrorEnums getWarpingErrorEnum(int flag){
		for(WarpingErrorEnums e: WarpingErrorEnums.values()){
			if( e.getValue() == flag )
				return e;
		}
		return DEFAULT_FLAGS;
	}
	
	public static int calculateValue(WarpingErrorEnums... enums){
		int out = 0x0;
		for(WarpingErrorEnums e: enums)
			out |= e.getValue(); 
		return out;
	}
}
