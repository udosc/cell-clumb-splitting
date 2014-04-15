package org.knime.knip.clump.nodes.mysplitter;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.knip.base.data.labeling.LabelingValue;
import org.knime.knip.base.node.ValueToCellNodeDialog;

public class DFTSplitterFactory<L extends Comparable<L>, T extends RealType<T> & NativeType<T>> extends TemplateCellClumpSplitterFactory<L, T>{
	
	@Override
	protected ValueToCellNodeDialog<LabelingValue<L>>createNodeDialog() {
		
		final ValueToCellNodeDialog<LabelingValue<L>> res = super.createNodeDialog();
	
		res.addDialogComponent("Options", "Template",
			    		new DialogComponentNumber(DFTSplitterModel.createDiscriptorModel(),
			                    "Used discriptors: ", 5));
		
		return res;
	}

	@Override
	public DFTSplitterModel<L, T> createNodeModel() {
		return new DFTSplitterModel<L, T>();
	}

}
