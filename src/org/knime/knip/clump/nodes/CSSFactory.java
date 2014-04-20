package org.knime.knip.clump.nodes;

import java.util.List;

import net.imglib2.img.Img;
import net.imglib2.ops.types.ConnectedType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.knip.base.data.img.ImgPlusCell;
import org.knime.knip.base.data.img.ImgPlusCellFactory;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.node.ValueToCellNodeDialog;
import org.knime.knip.base.node.ValueToCellNodeFactory;
import org.knime.knip.base.node.ValueToCellNodeModel;
import org.knime.knip.clump.contour.AbstractBinaryFactory;
import org.knime.knip.clump.curvature.Curvature;
import org.knime.knip.clump.curvature.CurvatureScaleSpace;

public class CSSFactory
	extends ValueToCellNodeFactory<ImgPlusValue<BitType>> {

	
	 private static SettingsModelString createTypeModel() {
	        return new SettingsModelString("connection_type", ConnectedType.values()[0].toString());
	 }
	
	@Override
	protected ValueToCellNodeDialog<ImgPlusValue<BitType>> createNodeDialog() {
		return new ValueToCellNodeDialog<ImgPlusValue<BitType>>() {

			@Override
			public void addDialogComponents() {
				// TODO Auto-generated method stub
				
			}
		};
	}

	@Override
	public ValueToCellNodeModel<ImgPlusValue<BitType>, ImgPlusCell<BitType>> createNodeModel() {
		return new ValueToCellNodeModel<ImgPlusValue<BitType>, ImgPlusCell<BitType>>(){

			private ImgPlusCellFactory m_imgCellFactory;
			
			private final SettingsModelString m_type = createTypeModel();
			
			@Override
			protected void addSettingsModels(List<SettingsModel> settingsModels) {
				settingsModels.add(m_type);
				
			}

			@Override
			protected ImgPlusCell<BitType> compute(
					ImgPlusValue<BitType> cellValue) throws Exception {
				
				Img<BitType> img = new CurvatureScaleSpace<DoubleType>( 120 ).compute( 
						new Curvature<DoubleType>( AbstractBinaryFactory.factory(cellValue.getImgPlus(), m_type.getStringValue()).createContour(), 5 , new DoubleType()), 
						getExecutorService());
				
				return m_imgCellFactory.createCell(
						img, 
						cellValue.getMetadata());
			}
			
			
		    /**
		     * {@inheritDoc}
		     */
		    @Override
		    protected void prepareExecute(final ExecutionContext exec) {
		    	m_imgCellFactory = new ImgPlusCellFactory(exec);
		    }
		};
	}


}
