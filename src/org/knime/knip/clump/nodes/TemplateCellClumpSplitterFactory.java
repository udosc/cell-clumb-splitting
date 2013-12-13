package org.knime.knip.clump.nodes;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.knip.base.data.labeling.LabelingCell;
import org.knime.knip.base.data.labeling.LabelingValue;
import org.knime.knip.base.node.ValueToCellNodeDialog;
import org.knime.knip.base.node.ValueToCellNodeFactory;
import org.knime.knip.base.node.ValueToCellNodeModel;
import org.knime.knip.clump.types.DistancesMeasuresEnum;
import org.knime.knip.core.util.EnumUtils;

/**
 * 
 * @author Schlegel
 *
 * @param <L>
 */
public class TemplateCellClumpSplitterFactory<L extends Comparable<L>, T extends RealType<T> & NativeType<T>> extends
		ValueToCellNodeFactory<LabelingValue<L>> {
	

	public TemplateCellClumpSplitterFactory(){
		super();
	}

	@Override
	protected ValueToCellNodeDialog<LabelingValue<L>>createNodeDialog() {
		return new ValueToCellNodeDialog<LabelingValue<L>>() {

			@SuppressWarnings("unchecked")
			@Override
			public void addDialogComponents() {
				
		        addDialogComponent("Options", "Template",
                        new DialogComponentColumnNameSelection(TemplateCellClumpSplitterModel.createTemplateColumnModel(),
                                "Template Column ", 1, false, LabelingValue.class));
		        
		        
		        addDialogComponent("Options", "Template",
		        		new DialogComponentNumber(TemplateCellClumpSplitterModel.createOrderColumnModel(),
                                "Order ", 1));
		        
		        
		        addDialogComponent("Options", "Template", 
		        		new DialogComponentNumber(TemplateCellClumpSplitterModel.createSigmaModel(), "Sigma", 0.1d));
		        
//		        addDialogComponent("Options", "Template", 
//		        		new DialogComponentNumber(MyCellClumpSplitterModel.createThresholdModel(), "Threshold", 0.2d));
		        
		        addDialogComponent("Options", "Factor", 
		        		new DialogComponentNumber(TemplateCellClumpSplitterModel.createFactorModel(), "Factor: ", 0.1d));
		        
		        addDialogComponent("Options", "Distance",  
		        		new DialogComponentStringSelection(TemplateCellClumpSplitterModel.createDistancesModel(), 
		        				"Distace", EnumUtils.getStringListFromToString(DistancesMeasuresEnum.values())));
				
			}
		};
	}

	@Override
	public ValueToCellNodeModel<LabelingValue<L>, LabelingCell<Integer>> createNodeModel() {
		return new TemplateCellClumpSplitterModel<L, T>();
	}

}
