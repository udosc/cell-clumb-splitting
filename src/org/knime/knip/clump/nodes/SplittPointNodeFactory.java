package org.knime.knip.clump.nodes;

import java.awt.Graphics;
import java.awt.Polygon;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.meta.ImgPlus;
import net.imglib2.ops.operation.labeling.unary.LabelingToImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.util.Pair;
import org.knime.knip.base.data.img.ImgPlusCell;
import org.knime.knip.base.data.img.ImgPlusCellFactory;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.data.labeling.LabelingCell;
import org.knime.knip.base.data.labeling.LabelingCellFactory;
import org.knime.knip.base.data.labeling.LabelingValue;
import org.knime.knip.base.node.ValueToCellNodeDialog;
import org.knime.knip.base.node.ValueToCellNodeFactory;
import org.knime.knip.base.node.ValueToCellNodeModel;
import org.knime.knip.base.node.ValueToCellsNodeDialog;
import org.knime.knip.base.node.ValueToCellsNodeFactory;
import org.knime.knip.base.node.ValueToCellsNodeModel;
import org.knime.knip.clump.boundary.BinaryFactory;
import org.knime.knip.clump.boundary.Contour;
import org.knime.knip.clump.boundary.Curvature;
import org.knime.knip.clump.boundary.WatershedFactory;
import org.knime.knip.clump.ops.FindStartingPoint;
import org.knime.knip.clump.split.CurvatureSplit;
import org.knime.knip.core.awt.labelingcolortable.DefaultLabelingColorTable;
import org.knime.knip.core.data.img.DefaultLabelingMetadata;
import org.knime.knip.core.util.ImgUtils;
import org.knime.knip.core.util.PolygonTools;
/**
 * 
 * @author Udo Schlegel
 *
 * @param <T>
 * @param <L>
 */
public class SplittPointNodeFactory<T extends RealType<T> & NativeType<T>, L extends Comparable<L>> 
	extends ValueToCellsNodeFactory<LabelingValue<L>>{
	
	protected SettingsModelDouble m_sigma = 
			new SettingsModelDouble("Sigma: ", 1.0d);
	
	protected SettingsModelDouble m_threshold = 
			new SettingsModelDouble("Threshold: ", 0.1d);

	@Override
	protected ValueToCellsNodeDialog<LabelingValue<L>> createNodeDialog() {
		return new ValueToCellsNodeDialog<LabelingValue<L>>() {

			@Override
			public void addDialogComponents() {
				
				addDialogComponent("Options", "Preprocessing", 
						new DialogComponentNumber(m_sigma, "Sigma", 0.1d));
				
				addDialogComponent("Options", "Preprocessing", 
						new DialogComponentNumber(m_threshold, "Threshold", 0.1d));
				
			}
		};
	}

	@Override
	public ValueToCellsNodeModel<LabelingValue<L>> createNodeModel() {
		
		
		return new ValueToCellsNodeModel<LabelingValue<L>> () {
			
			
			private LabelingCellFactory m_labCellFactory;
			
			private ImgPlusCellFactory m_imgCellFactory;

			@Override
			protected void addSettingsModels(List<SettingsModel> settingsModels) {
				settingsModels.add(m_sigma);
				settingsModels.add(m_threshold);
				
			}

			@Override
			protected DataCell[] compute(LabelingValue<L> cellValue)
					throws Exception {
				
//				if( !new BitType().getClass().equals( cellValue.getPixelType() ))
//						throw new KNIPRuntimeException("Image must be a BitType");
				
				
//				final ImgPlus<BitType> img = (ImgPlus<BitType>) cellValue.getImgPlus();
				
//				drawPolygon(img);
				
//				final Img<BitType> container = img.factory().create(img, img.firstElement());

								
				Labeling<L> labeling = cellValue.getLabelingCopy();
				long[] dim = new long[ labeling.numDimensions() ]; 
				labeling.dimensions(dim);
				
				Img<BitType> img = new ArrayImgFactory<BitType>().create(dim, new BitType());
				new LabelingToImg<L, BitType>().compute(
						labeling, 
						img);
				
				Labeling<L> out = ImgUtils.createEmptyCopy(labeling);
				RandomAccess<LabelingType<L>> ra = out.randomAccess();

				Collection<Pair<L, long[]>> map = new FindStartingPoint<L>().compute(
						labeling, 
						new LinkedList<Pair<L, long[]>>());
				
				Integer i = 0;
				for(Pair<L, long[]> start: map){
					Contour c = new BinaryFactory(img, start.getSecond()).createContour();
					
					if( c.length() < 20 )
						continue;
					
					System.out.println("Tracking label: " + i++);
					for(long[] point: c){
						ra.setPosition(point);
//						ra.get().getMapping().intern( Arrays.asList( e.getKey() ));
						ra.get().setLabel((L) i);
					}
					
					Collection<long[]> split = new CurvatureSplit<T, L>(7,
							m_threshold.getDoubleValue(),
							m_sigma.getDoubleValue()).
						compute(c, new LinkedList<long[]>());
					
					for(long[] point: split){
						ra.setPosition(point);
						ra.get().setLabel( (L) new Integer(1337));
					}
				}
				
				return new DataCell[]{
						m_labCellFactory.createCell(out, cellValue.getLabelingMetadata())};
				
			}
			
            /**
             * {@inheritDoc}
             */
            @Override
            protected void prepareExecute(final ExecutionContext exec) {
                m_labCellFactory = new LabelingCellFactory(exec);
                m_imgCellFactory = new ImgPlusCellFactory(exec);
            }

			
			private void drawPolygon(Img<BitType> image){
				int[] pos = new int[ image.numDimensions()];
				Cursor<BitType>  c = image.cursor();
				while( c.hasNext() ){
					if ( c.next().get() ){
						pos[0] = c.getIntPosition(0);
						pos[1] = c.getIntPosition(1);
						break;
					}
				}
				
				final Polygon poly = PolygonTools.extractPolygon(image, pos);
				
				final JPanel panel = new JPanel(){
					  
					private static final long serialVersionUID = 4457688846890363038L;

					@Override
					protected void paintComponent( Graphics g ){
						super.paintComponent( g );
					    g.drawPolygon(poly);
					}
				};

		        final JFrame frame = new JFrame();
		        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		        frame.setSize(poly.getBounds().width, poly.getBounds().height);
		        frame.getContentPane().add(panel);
		        frame.setVisible(true);
			}

			@Override
			protected Pair<DataType[], String[]> getDataOutTypeAndName() {				
				return new Pair<DataType[], String[]>(
						new DataType[]{LabelingCell.TYPE}, 
						new String[]{"Labeling"});
			}
		};
	}

}