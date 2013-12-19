package org.knime.knip.clump.nodes;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.ops.operation.iterable.unary.Mean;
import net.imglib2.ops.operation.labeling.unary.LabelingToImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.util.Pair;
import org.knime.knip.base.data.labeling.LabelingCell;
import org.knime.knip.base.data.labeling.LabelingCellFactory;
import org.knime.knip.base.data.labeling.LabelingValue;
import org.knime.knip.base.node.ValueToCellNodeDialog;
import org.knime.knip.base.node.ValueToCellNodeFactory;
import org.knime.knip.base.node.ValueToCellNodeModel;
import org.knime.knip.clump.boundary.Curvature;
import org.knime.knip.clump.contour.BinaryFactory;
import org.knime.knip.clump.contour.Contour;
import org.knime.knip.clump.graph.Edge;
import org.knime.knip.clump.graph.Graph;
import org.knime.knip.clump.graph.PrintValidPaths;
import org.knime.knip.clump.ops.FindStartingPoint;
import org.knime.knip.clump.ops.StandardDeviation;
import org.knime.knip.clump.split.CurvatureBasedSplitting;
import org.knime.knip.core.data.algebra.Complex;
import org.knime.knip.core.util.ImgUtils;

public class DTFactory<L extends Comparable<L>, T extends RealType<T> & NativeType<T>> 
	extends ValueToCellNodeFactory<LabelingValue<L>> {
	
    private final SettingsModelDouble m_sigma = createSigmaModel();
    
    private final SettingsModelDouble m_t = createTModel();
    
    private final SettingsModelDouble m_beta = createBetaModel();
    
    protected static SettingsModelDouble createSigmaModel(){
    	return new SettingsModelDouble("Sigma: ", 2.0d);
    }

    protected static SettingsModelDouble createBetaModel(){
    	return new SettingsModelDouble("Beta: ", 0.9d);
    }
    
    protected static SettingsModelDouble createTModel(){
    	return new SettingsModelDouble("T: ", -0.15d);
    }

	@Override
	protected ValueToCellNodeDialog<LabelingValue<L>> createNodeDialog() {
		return new ValueToCellNodeDialog<LabelingValue<L>>() {

			@Override
			public void addDialogComponents() {
				addDialogComponent(new DialogComponentNumber(
						createSigmaModel(), 
						"Sigma", 
						0.1d));
				
			}
		};
	}

	@Override
	public ValueToCellNodeModel<LabelingValue<L>, LabelingCell<L>> createNodeModel() {
		return new ValueToCellNodeModel<LabelingValue<L>, LabelingCell<L>>(){
			
			private LabelingCellFactory m_labCellFactory;

			@Override
			protected void addSettingsModels(List<SettingsModel> settingsModels) {
				settingsModels.add( m_sigma );
				settingsModels.add( m_beta );
				settingsModels.add( m_t );
			}

			@Override
			protected LabelingCell<L> compute(LabelingValue<L> cellValue) throws IOException{
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
						ra.get().setLabel((L) new Integer(1000));
					}
					
					Curvature<DoubleType> curv = new Curvature<DoubleType>(c, 5, new DoubleType());
					
					final double mean = new Mean<DoubleType, DoubleType>().
							compute(curv.getImg().iterator(), new DoubleType(0.0d)).getRealDouble();
					
					final double std = new StandardDeviation<DoubleType, DoubleType>(mean).
							compute(curv.getImg().iterator(), new DoubleType(0.0d)).getRealDouble();
											
					final List<long[]> splittingPoints = new CurvatureBasedSplitting<DoubleType>(5, 
							mean +std, 
							10, 
							new DoubleType(),
							m_sigma.getDoubleValue()).compute(c, new LinkedList<long[]>());
					
//					for(long[] p: splittingPoints){
//						Complex tangent = c.getUnitVector( c.indefOf(p), 5);
//						System.out.println( p[0] +", " + p[1] + ": " + tangent.re() + "-" + tangent.im());
//						System.out.println( tangent.getMagnitude() );
//					}
					
					Integer number = 0;
//					for(long[] point: splittingPoints){
//						ra.setPosition(point);
//						ra.get().setLabel( (L) number++);
//					}
					
					Graph<T> graph = new Graph<T>(splittingPoints);
					graph.validate(img, 1);
					for( Edge e: graph.getValidEdges()){
						Complex tangentS = c.getUnitVector(  e.getSource().getPosition() , 1);
						Complex tangentD = c.getUnitVector(  e.getDestination().getPosition() , 1);
						System.out.println( tangentS.re() * tangentD.re() + tangentS.im() + tangentD.im() );
//						if( tangentS.re() * tangentD.re() + tangentS.im() + tangentD.im() > m_t.getDoubleValue() )
//							graph.deleteEdge(e);
						Complex vector = e.getVector();
						final double tmp0 = Math.abs((vector.re() * tangentS.re() + vector.im() + tangentS.im()) / vector.getMagnitude());
						vector = e.getReverseVector();
						final double tmp1 = Math.abs((vector.re() * tangentD.re() + vector.im() + tangentD.im()) / vector.getMagnitude());
//						if(  Math.max(tmp0, tmp1) > m_beta.getDoubleValue() )
//							graph.deleteEdge(e);
					}
					new PrintValidPaths<T, L>( (L)number++ ).compute(graph, ra);

					
				}
				
				return m_labCellFactory.createCell(out, cellValue.getLabelingMetadata());
			}
			
		    @Override
		    protected void prepareExecute(final ExecutionContext exec) {
		        m_labCellFactory = new LabelingCellFactory(exec);
		    }
		};
	}

}
