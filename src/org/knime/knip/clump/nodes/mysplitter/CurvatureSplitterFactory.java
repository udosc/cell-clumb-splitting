package org.knime.knip.clump.nodes.mysplitter;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class CurvatureSplitterFactory<L extends Comparable<L>, T extends RealType<T> & NativeType<T>> extends TemplateCellClumpSplitterFactory<L, T>{
	
	@Override
	public CurvatureSplitterModel<L, T> createNodeModel() {
		return new CurvatureSplitterModel<L, T>();
	}

}
