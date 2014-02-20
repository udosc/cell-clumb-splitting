package org.knime.knip.clump.nodes.test;

import java.util.List;

import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;

import org.knime.core.data.DataCell;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.node.ValueToCellNodeDialog;
import org.knime.knip.base.node.ValueToCellNodeFactory;
import org.knime.knip.base.node.ValueToCellNodeModel;

public class ClumpTesting extends ValueToCellNodeFactory<ImgPlusValue<BitType>> {

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
	public ValueToCellNodeModel<ImgPlusValue<BitType>, ? extends DataCell> createNodeModel() {
		return new ValueToCellNodeModel<ImgPlusValue<BitType>, DataCell>() {

			@Override
			protected void addSettingsModels(List<SettingsModel> settingsModels) {
				// TODO Auto-generated method stub
				
			}

			@Override
			protected DataCell compute(ImgPlusValue<BitType> cellValue)
					throws Exception {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}



}
