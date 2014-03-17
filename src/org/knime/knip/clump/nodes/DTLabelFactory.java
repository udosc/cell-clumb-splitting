package org.knime.knip.clump.nodes;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.region.BresenhamLine;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.ops.operation.labeling.unary.LabelingToImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.util.Pair;
import org.knime.knip.base.data.labeling.LabelingCell;
import org.knime.knip.base.data.labeling.LabelingCellFactory;
import org.knime.knip.base.data.labeling.LabelingValue;
import org.knime.knip.base.node.ValueToCellNodeDialog;
import org.knime.knip.base.node.ValueToCellNodeFactory;
import org.knime.knip.base.node.ValueToCellNodeModel;
import org.knime.knip.clump.contour.BinaryFactory;
import org.knime.knip.clump.contour.Contour;
import org.knime.knip.clump.dt.EdgeInference;
import org.knime.knip.clump.dt.EdgePruning;
import org.knime.knip.clump.dt.MyDelaunayTriangulation;
import org.knime.knip.clump.graph.Edge;
import org.knime.knip.clump.ops.FindStartingPoint;
import org.knime.knip.clump.ops.ValidateSplitLines;
import org.knime.knip.clump.split.CurvatureSplittingPoints;
import org.knime.knip.core.data.algebra.Complex;

public class DTLabelFactory<L extends Comparable<L>, T extends RealType<T> & NativeType<T>> 
	extends ValueToCellNodeFactory<LabelingValue<L>> {
	
    private final SettingsModelDouble m_sigma = createSigmaModel();
    
    private final SettingsModelDouble m_t = createTModel();
    
    private final SettingsModelDouble m_k = createKModel();
    
    private final SettingsModelDouble m_beta = createBetaModel();
    
    private final SettingsModelBoolean m_prune = createPruneModel();
    
    private final SettingsModelBoolean m_inference = createInferenceModel();
    
    protected static SettingsModelDouble createSigmaModel(){
    	return new SettingsModelDouble("Sigma: ", 2.0d);
    }

    protected static SettingsModelDouble createBetaModel(){
    	return new SettingsModelDouble("Beta: ", 0.9d);
    }
    
    protected static SettingsModelDouble createTModel(){
    	return new SettingsModelDouble("T: ", -0.15d);
    }
    
    protected static SettingsModelDouble createKModel(){
    	return new SettingsModelDouble("Curvature: ", 0.1d);
    }
    
    protected static SettingsModelBoolean createPruneModel(){
    	return new SettingsModelBoolean("Prune edges: ", true);
    }
    
    protected static SettingsModelBoolean createInferenceModel(){
    	return new SettingsModelBoolean("Inference edges: ", true);
    }

	@Override
	protected ValueToCellNodeDialog<LabelingValue<L>> createNodeDialog() {
		return new ValueToCellNodeDialog<LabelingValue<L>>() {

			@Override
			public void addDialogComponents() {
				addDialogComponent(new DialogComponentNumber(
						createSigmaModel(), 
						"Sigma: ", 
						0.1d));
				
				addDialogComponent(new DialogComponentNumber(
						createBetaModel(),
						"Beta: ", 
						0.9d));
				
				addDialogComponent(new DialogComponentNumber(
						createTModel(),
						"T: ", 
						-0.15d));
				
				addDialogComponent(new DialogComponentBoolean(
						createPruneModel(), 
						"Prune Edges"));
				
				
				addDialogComponent(new DialogComponentBoolean(
						createInferenceModel(), 
						"Inference Edges"));
				
			}
		};
	}

	@Override
	public ValueToCellNodeModel<LabelingValue<L>, LabelingCell<Integer>> createNodeModel() {
		return new ValueToCellNodeModel<LabelingValue<L>, LabelingCell<Integer>>(){
			
			private LabelingCellFactory m_labCellFactory;

			@Override
			protected void addSettingsModels(List<SettingsModel> settingsModels) {
				settingsModels.add( m_sigma );
				settingsModels.add( m_beta );
				settingsModels.add( m_t );
				settingsModels.add( m_k );
				settingsModels.add( m_beta );
				settingsModels.add( m_t );
			}

			@Override
			protected LabelingCell<Integer> compute(LabelingValue<L> cellValue) throws IOException{
				
				System.out.println("Processing row: " +  cellValue.getLabelingMetadata().getName());
				
				Labeling<L> labeling = cellValue.getLabelingCopy();
				long[] dim = new long[ labeling.numDimensions() ]; 
				labeling.dimensions(dim);
				
				Img<BitType> img = new ArrayImgFactory<BitType>().create(dim, new BitType());
				new LabelingToImg<L, BitType>().compute(
						labeling, 
						img);
				
//				Labeling<Integer> out = ImgUtils.createEmptyCopy(labeling);
//				RandomAccess<LabelingType<L>> ra = out.randomAccess();
				
				final Labeling<Integer> lab =
		                new NativeImgLabeling<Integer, IntType>(
		                		new ArrayImgFactory<IntType>().create(labeling, new IntType()));
				
				RandomAccess<LabelingType<Integer>> ra = lab.randomAccess();

				Collection<Pair<L, long[]>> map = new FindStartingPoint<L>().compute(
						labeling, 
						new LinkedList<Pair<L, long[]>>());
				
				Integer i = 0;
				for(Pair<L, long[]> start: map){
					Contour c = new BinaryFactory(img, start.getSecond()).createContour();
					
					
					if( c.length() < 20 )
						continue;
					
					System.out.println("Tracking label: " + i++);
//					for(long[] point: c){
//						ra.setPosition(point);
////						ra.get().getMapping().intern( Arrays.asList( e.getKey() ));
//						ra.get().setLabel(new Integer(1000));
//					}
					
//					Curvature<DoubleType> curv = new Curvature<DoubleType>(c, 5, new DoubleType());
					
											
					final List<long[]> splittingPoints = new CurvatureSplittingPoints<DoubleType>(5, 
							10, 
							new DoubleType(),
							m_sigma.getDoubleValue()).compute(c);
					
//					for(long[] p: splittingPoints){
//						Complex tangent = c.getUnitVector( c.indefOf(p), 5);
//						System.out.println( p[0] +", " + p[1] + ": " + tangent.re() + "-" + tangent.im());
//						System.out.println( tangent.getMagnitude() );
//					}
					

//					for(long[] point: splittingPoints){
//						ra.setPosition(point);
//						ra.get().setLabel( (L) number++);
//					}
					
//					new ImglibDelaunayTriangulation().compute(splittingPoints, new LinkedList<Pair<Point, Point>>());
					Collection<Pair<Point, Point>> points = new ValidateSplitLines(img).compute(
							new MyDelaunayTriangulation().compute(splittingPoints, new LinkedList<Pair<Point, Point>>()), 
							new LinkedList<Pair<Point, Point>>());
					
					int label = 0;
					
					if( m_prune.getBooleanValue() ){
						points = 
							new EdgePruning(c, m_beta.getDoubleValue(), m_t.getDoubleValue()).compute(
									points,
									new LinkedList<Pair<Point, Point>>());
					}
					
					if( m_inference.getBooleanValue() )
						points = 
						new EdgeInference(c).compute(
								points,
								new LinkedList<Pair<Point, Point>>());

					for(Pair<Point, Point> line: points){
						Cursor<LabelingType<Integer>> cursor = 
								new BresenhamLine<LabelingType<Integer>>(lab, line.getFirst(), line.getSecond());
						while( cursor.hasNext() ){
//							List res = getLabeling()
							cursor.next().setLabel(label);
						}
						label++;
					}	
				}

				
				return m_labCellFactory.createCell(lab, cellValue.getLabelingMetadata());
			}
			
		    @Override
		    protected void prepareExecute(final ExecutionContext exec) {
		        m_labCellFactory = new LabelingCellFactory(exec);
		    }
		    
		    private List<Edge> removeDuplicates(List<Edge> list){
		    	final List<Edge> out = new LinkedList<Edge>();
		    	for(Edge e: list){
		    		if( e.getSource().getIndex() < e.getDestination().getIndex() )
		    			out.add( e );
		    	}
		    	return out;
		    }

			private void draw(RandomAccess<BitType> ra, Point p1, Point p2, BitType value) {
				final Cursor<BitType> cursor = 
						new BresenhamLine<BitType>(ra, p1, p2);
				long[] res = new long[ ra.numDimensions() ];
				while( cursor.hasNext() ){
					cursor.fwd();
					cursor.localize( res );
					ra.setPosition(res);
					ra.get().set( value );
				}
			}
			
			private void draw(RandomAccess<BitType> ra, Edge edge, BitType value) {
				draw(ra, 
						new Point(edge.getSource().getPosition()),
						new Point(edge.getDestination().getPosition()), 
						value);
			}
			

		    
		    private RandomAccess<LabelingType<Integer>> printTangent(Complex tangent, long[] pos, RandomAccess<LabelingType<Integer>> ra){
		    	long r0 = (long) (pos[0] + tangent.re() * 15);
		    	long r1 = (long) (pos[1] + tangent.im() * 15);
		    	Cursor<LabelingType<Integer>> cursor = new BresenhamLine<LabelingType<Integer>>(ra, new Point(pos), new Point(r0, r1));
		    	while( cursor.hasNext() ){
		    		cursor.fwd();
		    		try {
		    			cursor.get().setLabel(new Integer( 1111 ));
					} catch (ArrayIndexOutOfBoundsException e) {
						// TODO: handle exception
						break;
					}
		    		
		    	}
		    	return ra;
		    }
		};
	}

}
