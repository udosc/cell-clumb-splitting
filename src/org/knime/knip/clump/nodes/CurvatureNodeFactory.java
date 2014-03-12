package org.knime.knip.clump.nodes;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.collection.PointSampleList;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.meta.ImgPlus;
import net.imglib2.ops.operation.labeling.unary.LabelingToImg;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.util.Pair;
import org.knime.knip.base.data.img.ImgPlusCell;
import org.knime.knip.base.data.img.ImgPlusCellFactory;
import org.knime.knip.base.data.labeling.LabelingValue;
import org.knime.knip.base.node.ValueToCellNodeDialog;
import org.knime.knip.base.node.ValueToCellNodeFactory;
import org.knime.knip.base.node.ValueToCellNodeModel;
import org.knime.knip.clump.contour.BinaryFactory;
import org.knime.knip.clump.contour.Contour;
import org.knime.knip.clump.curvature.KCosineCurvature;
import org.knime.knip.clump.ops.FindStartingPoint;
import org.knime.knip.core.util.ImgUtils;

/**
 * 
 * @author Udo
 *
 * @param <L>
 */
public class CurvatureNodeFactory<L extends Comparable<L>> 
	extends ValueToCellNodeFactory<LabelingValue<L>> {

	@Override
	protected ValueToCellNodeDialog<LabelingValue<L>> createNodeDialog() {
		return new ValueToCellNodeDialog<LabelingValue<L>>() {

			@Override
			public void addDialogComponents() {
				// TODO Auto-generated method stub
				
			}
		};
	}

	@Override
	public ValueToCellNodeModel<LabelingValue<L>, ImgPlusCell<DoubleType>> createNodeModel() {
		return new ValueToCellNodeModel<LabelingValue<L>, ImgPlusCell<DoubleType>>() {

			private ImgPlusCellFactory m_imgCellFactory;
			
			@Override
			protected void addSettingsModels(List<SettingsModel> settingsModels) {
				// TODO Auto-generated method stub
				
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

				Collection<Pair<L, long[]>> map = new FindStartingPoint<L>().compute(
						labeling, 
						new LinkedList<Pair<L, long[]>>());
				
				Integer i = 0;
				for(Pair<L, long[]> start: map){
					Contour contour = new BinaryFactory(binaryImg, start.getSecond()).createContour();
					i++;
					if( contour.length() < 20 )
						continue;
						
					PointSampleList<DoubleType> curvature = new KCosineCurvature<DoubleType>(new DoubleType(), 5).getPointSampleList( contour );
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
