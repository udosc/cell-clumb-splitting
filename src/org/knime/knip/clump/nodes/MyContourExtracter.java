package org.knime.knip.clump.nodes;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.ops.operation.labeling.unary.LabelingToImg;
import net.imglib2.ops.types.ConnectedType;
import net.imglib2.type.logic.BitType;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.Pair;
import org.knime.knip.base.data.labeling.LabelingCell;
import org.knime.knip.base.data.labeling.LabelingCellFactory;
import org.knime.knip.base.data.labeling.LabelingValue;
import org.knime.knip.base.node.ValueToCellNodeDialog;
import org.knime.knip.base.node.ValueToCellNodeFactory;
import org.knime.knip.base.node.ValueToCellNodeModel;
import org.knime.knip.clump.contour.AbstractBinaryFactory;
import org.knime.knip.clump.contour.Contour;
import org.knime.knip.clump.contour.FindStartingPoints;
import org.knime.knip.core.util.EnumUtils;
import org.knime.knip.core.util.ImgUtils;

public class MyContourExtracter<L extends Comparable<L>>
	extends ValueToCellNodeFactory<LabelingValue<L>>{

	 private static SettingsModelString createTypeModel() {
	        return new SettingsModelString("connection_type", ConnectedType.values()[0].toString());
	 }
	
	@Override
	protected ValueToCellNodeDialog<LabelingValue<L>> createNodeDialog() {
		return new ValueToCellNodeDialog<LabelingValue<L>>() {

			@Override
			public void addDialogComponents() {
                addDialogComponent("Options", "Settings", new DialogComponentStringSelection(createTypeModel(),
                        "Connection Type", EnumUtils.getStringCollectionFromToString(ConnectedType.values())));
				
			}
		};
	}

	@Override
	public ValueToCellNodeModel<LabelingValue<L>, LabelingCell<L>> createNodeModel() {
		return new ValueToCellNodeModel<LabelingValue<L>, LabelingCell<L>>() {

			private LabelingCellFactory m_labCellFactory;
					
			private final SettingsModelString m_type = createTypeModel();
			
			@Override
			protected void addSettingsModels(List<SettingsModel> settingsModels) {
				settingsModels.add( m_type );
			}

			@Override
			protected LabelingCell<L> compute(LabelingValue<L> cellValue)
					throws Exception {
				
				Labeling<L> labeling = cellValue.getLabelingCopy();
				long[] dim = new long[ labeling.numDimensions() ]; 
				labeling.dimensions(dim);
				
				Img<BitType> img = new ArrayImgFactory<BitType>().create(dim, new BitType());
				new LabelingToImg<L, BitType>().compute(
						labeling, 
						img);
				
				Labeling<L> out = ImgUtils.createEmptyCopy(labeling);
				RandomAccess<LabelingType<L>> ra = out.randomAccess();

				Collection<Pair<L, long[]>> map = new FindStartingPoints<L>().compute(
						labeling, 
						new LinkedList<Pair<L, long[]>>());
				
				
				
				Integer i = 0;
				for(Pair<L, long[]> start: map){
					Contour c = AbstractBinaryFactory.factory(img, start.getSecond(), m_type.getStringValue()).createContour();
					System.out.println("Tracking label: " + i++);
					for(long[] point: c){
						ra.setPosition(point);
//						ra.get().getMapping().intern( Arrays.asList( e.getKey() ));
						ra.get().setLabel((L) i);
					}
				}
				
				return m_labCellFactory.createCell(out, cellValue.getLabelingMetadata());
			}
			
            protected void prepareExecute(final ExecutionContext exec) {
                m_labCellFactory = new LabelingCellFactory(exec);
            }
		};
	}

}
