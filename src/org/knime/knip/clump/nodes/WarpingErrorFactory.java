package org.knime.knip.clump.nodes;

import net.imglib2.type.logic.BitType;

import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.data.labeling.LabelingCell;
import org.knime.knip.base.node.TwoValuesToCellNodeDialog;
import org.knime.knip.base.node.TwoValuesToCellNodeFactory;
import org.knime.knip.base.node.TwoValuesToCellNodeModel;

/**
 * 
 * @author Schlegel
 *
 */
public class WarpingErrorFactory 
extends TwoValuesToCellNodeFactory<ImgPlusValue<BitType>, ImgPlusValue<BitType>>{
	
	private final SettingsModelInteger m_smSize = 
			createSizeSettingsModel();
	
//	private final SettingsModelString m_smGTIndex = 
//			new SettingsModelString("Ground truth", "");
//	
//	private final SettingsModelString m_smRefIndex =
//			new SettingsModelString("Reference", "");
	
	protected static SettingsModelInteger createSizeSettingsModel(){
		return new SettingsModelInteger("Sigma: ", 1);
	}
			
	@Override
	protected TwoValuesToCellNodeDialog<ImgPlusValue<BitType>, ImgPlusValue<BitType>> createNodeDialog() {
		return new TwoValuesToCellNodeDialog<ImgPlusValue<BitType>, ImgPlusValue<BitType>>() {

			@SuppressWarnings("unchecked")
			@Override
			public void addDialogComponents() {
		        addDialogComponent("Options", "Labeling",
		        		new DialogComponentNumber( m_smSize,
                                "Minimum size ", 1));
		        
//		        addDialogComponent("Options", "Columns",
//                        new DialogComponentColumnNameSelection(m_smGTIndex,
//                                "Ground truth", 1, true, ImgPlusValue.class));
//		        
//		        addDialogComponent("Options", "Columns",
//                        new DialogComponentColumnNameSelection(m_smRefIndex,
//                                "Reference", 1, true, ImgPlusValue.class));
			}
		};
	}

	@Override
	public TwoValuesToCellNodeModel<ImgPlusValue<BitType>, ImgPlusValue<BitType>, LabelingCell<String>> createNodeModel() {
		return new WarpingErrorModel();
	}

}
