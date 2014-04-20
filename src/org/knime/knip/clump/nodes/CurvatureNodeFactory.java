package org.knime.knip.clump.nodes;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.meta.ImgPlus;
import net.imglib2.ops.operation.labeling.unary.LabelingToImg;
import net.imglib2.ops.types.ConnectedType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.Pair;
import org.knime.knip.base.data.img.ImgPlusCell;
import org.knime.knip.base.data.img.ImgPlusCellFactory;
import org.knime.knip.base.data.labeling.LabelingValue;
import org.knime.knip.base.node.ValueToCellNodeDialog;
import org.knime.knip.base.node.ValueToCellNodeFactory;
import org.knime.knip.base.node.ValueToCellNodeModel;
import org.knime.knip.clump.contour.AbstractBinaryFactory;
import org.knime.knip.clump.contour.Contour;
import org.knime.knip.clump.contour.FindStartingPoints;
import org.knime.knip.clump.curvature.factory.CurvatureFactory;
import org.knime.knip.clump.types.CurvatureCreationEnum;
import org.knime.knip.core.util.EnumUtils;
import org.knime.knip.core.util.ImgUtils;

/**
 * 
 * @author Udo
 *
 * @param <L>
 */
public class CurvatureNodeFactory<L extends Comparable<L>> 
	extends ValueToCellNodeFactory<LabelingValue<L>> {
	
    private static SettingsModelString createFacotoryModel(){
    	return new SettingsModelString("Factory", CurvatureCreationEnum.K_COSINE.name());
    }
    
	private static SettingsModelString createTypeModel() {
		return new SettingsModelString("connection_type", ConnectedType.values()[0].toString());
	}

	@Override
	protected ValueToCellNodeDialog<LabelingValue<L>> createNodeDialog() {
		return new ValueToCellNodeDialog<LabelingValue<L>>() {

			@Override
			public void addDialogComponents() {
		        addDialogComponent("Options", "Factory: ",  
		        		new DialogComponentStringSelection(createFacotoryModel(), 
		        				"Factory", EnumUtils.getStringListFromToString(CurvatureCreationEnum.values())));
				
		        addDialogComponent("Options", "Settings", new DialogComponentStringSelection(createTypeModel(),
                        "Connection Type", EnumUtils.getStringCollectionFromToString(ConnectedType.values())));
			}
		};
	}

	@Override
	public ValueToCellNodeModel<LabelingValue<L>, ImgPlusCell<DoubleType>> createNodeModel() {
		return new ValueToCellNodeModel<LabelingValue<L>, ImgPlusCell<DoubleType>>() {

			private ImgPlusCellFactory m_imgCellFactory;
			
			private SettingsModelString m_smModel = createFacotoryModel();
			
			private final SettingsModelString m_type = createTypeModel();
			
			@Override
			protected void addSettingsModels(List<SettingsModel> settingsModels) {
				settingsModels.add( m_smModel );
				
			}

			@Override
			protected ImgPlusCell<DoubleType> compute(LabelingValue<L> cellValue)
					throws Exception {
				Labeling<L> labeling = cellValue.getLabelingCopy();
				long[] dim = new long[ labeling.numDimensions() ]; 
				labeling.dimensions(dim);
				
				Img<BitType> binaryImg = new ArrayImgFactory<BitType>().create(dim, new BitType());
				new LabelingToImg<L, BitType>().compute(
						labeling, 
						binaryImg);
				
				Img<DoubleType> out = ImgUtils.createEmptyCopy(
						dim, new ArrayImgFactory<DoubleType>(), new DoubleType());
				
				RandomAccess<DoubleType> ra = out.randomAccess();

				Collection<Pair<L, long[]>> map = new FindStartingPoints<L>().compute(
						labeling, 
						new LinkedList<Pair<L, long[]>>());
				
				Integer i = 0;
				for(Pair<L, long[]> start: map){
					Contour contour = AbstractBinaryFactory.factory(binaryImg, start.getSecond(), m_type.getStringValue()).createContour();
					i++;
					if( contour.length() < 20 )
						continue;
					
					CurvatureFactory<DoubleType> factory = CurvatureCreationEnum.getFactory( 
							EnumUtils.valueForName(m_smModel.getStringValue(), CurvatureCreationEnum.values()),
							5,
							new DoubleType());
					
					IterableInterval<DoubleType> curvature = factory.getPointSampleList( contour );
					
					
					
//					PointSampleList<DoubleType> curvature = new KCosineCurvature<DoubleType>(new DoubleType(), 5).getPointSampleList( contour );
//					PointSampleList<DoubleType> curvature = new GaussianCurvature<DoubleType>(new DoubleType(), 5).getPointSampleList();
//					Curvature<DoubleType> curv = new Curvature<DoubleType>(
//							contour, 
//							5, 
//							new DoubleType());
					System.out.println("Printing label: " + i);
					long[] pos = new long[ curvature.numDimensions() ];
					Cursor<DoubleType> cursor = curvature.localizingCursor();
					while( cursor.hasNext() ){
						cursor.fwd();
						cursor.localize(pos);
						ra.setPosition(pos);
						ra.get().set( cursor.get() );
					}
				}
				
				return m_imgCellFactory.createCell(new ImgPlus<DoubleType>( out ));
			}
			
			@Override
            protected void prepareExecute(final ExecutionContext exec) {
                m_imgCellFactory = new ImgPlusCellFactory(exec);
            }
		};
	}

}
