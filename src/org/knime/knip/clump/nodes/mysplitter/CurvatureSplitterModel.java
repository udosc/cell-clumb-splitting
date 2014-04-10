package org.knime.knip.clump.nodes.mysplitter;

import java.util.List;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.knip.clump.curvature.CurvatureDistance;
import org.knime.knip.clump.dist.contour.ContourDistance;

public class CurvatureSplitterModel<L extends Comparable<L>, T extends RealType<T> & NativeType<T>>  extends TemplateCellClumpSplitterModel<L, T>{
    
    @Override
    protected void addSettingsModels(List<SettingsModel> settingsModels) {
    	super.addSettingsModels(settingsModels);
    }
	
	@Override
	protected ContourDistance<DoubleType> createContourDistance() {
		return new CurvatureDistance<DoubleType>(getTemplates(), createCurvatureFactory(), 2, getExecutorService(), getSigma());
	}

}