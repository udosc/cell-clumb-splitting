package org.knime.knip.clump.nodes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.util.Pair;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.data.labeling.LabelingCell;
import org.knime.knip.base.data.labeling.LabelingCellFactory;
import org.knime.knip.base.data.labeling.LabelingValue;
import org.knime.knip.base.exceptions.KNIPException;
import org.knime.knip.base.node.NodeTools;
import org.knime.knip.base.node.TwoValuesToCellNodeDialog;
import org.knime.knip.base.node.TwoValuesToCellNodeFactory;
import org.knime.knip.base.node.TwoValuesToCellNodeModel;
import org.knime.knip.clump.types.WarpingErrorEnums;
import org.knime.knip.clump.warp.ClusterWarpingErrors;
import org.knime.knip.clump.warp.ImgLib2WarpingError;
import org.knime.knip.core.awt.labelingcolortable.DefaultLabelingColorTable;
import org.knime.knip.core.data.img.DefaultLabelingMetadata;
import org.knime.knip.core.util.ImgUtils;

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
