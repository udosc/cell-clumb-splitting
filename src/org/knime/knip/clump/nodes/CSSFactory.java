package org.knime.knip.clump.nodes;

import java.util.List;

import net.imglib2.img.Img;
import net.imglib2.meta.ImgPlus;
import net.imglib2.ops.operation.UnaryOutputOperation;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;

import org.knime.core.data.DataCell;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.knip.base.data.img.ImgPlusCell;
import org.knime.knip.base.data.img.ImgPlusCellFactory;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.data.labeling.LabelingCellFactory;
import org.knime.knip.base.node.ImgPlusToImgPlusNodeFactory;
import org.knime.knip.base.node.ImgPlusToImgPlusNodeModel;
import org.knime.knip.base.node.ValueToCellNodeDialog;
import org.knime.knip.base.node.ValueToCellNodeFactory;
import org.knime.knip.base.node.ValueToCellNodeModel;
import org.knime.knip.clump.boundary.Curvature;
import org.knime.knip.clump.contour.BinaryFactory;
import org.knime.knip.clump.curvature.CurvatureScaleSpace;
import org.knime.knip.core.util.ImgUtils;

public class CSSFactory
	extends ValueToCellNodeFactory<ImgPlusValue<BitType>> {

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
			
			@Override
			protected void addSettingsModels(List<SettingsModel> settingsModels) {
				// TODO Auto-generated method stub
				
			}

			@Override
			protected ImgPlusCell<BitType> compute(
					ImgPlusValue<BitType> cellValue) throws Exception {
				
				Img<BitType> img = new CurvatureScaleSpace<DoubleType>( 120 ).compute( 
						new Curvature<DoubleType>( new BinaryFactory( cellValue.getImgPlus() ).createContour(), 5 , new DoubleType()), 
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
