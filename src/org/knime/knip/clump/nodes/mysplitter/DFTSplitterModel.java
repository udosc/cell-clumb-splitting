package org.knime.knip.clump.nodes.mysplitter;

import java.util.List;

import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.knip.clump.dist.contour.ContourDistance;
import org.knime.knip.clump.dist.contour.CurvatureFourier;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

public class DFTSplitterModel<L extends Comparable<L>, T extends RealType<T> & NativeType<T>>  extends TemplateCellClumpSplitterModel<L, T>{

    private final SettingsModelInteger m_smDescriptor = createDiscriptorModel();
    
    protected static SettingsModelInteger createDiscriptorModel(){
    	return new SettingsModelInteger("Used descriptors: ", 32);
    }
    
    protected void addSettingsModels(List<SettingsModel> settingsModels) {
    	super.addSettingsModels(settingsModels);
    	settingsModels.add( m_smDescriptor );
    }
	
	@Override
	protected ContourDistance<DoubleType> createContourDistance() {
		return new CurvatureFourier<DoubleType>(getTemplates(), createCurvatureFactory(), m_smDescriptor.getIntValue());
	}

}
