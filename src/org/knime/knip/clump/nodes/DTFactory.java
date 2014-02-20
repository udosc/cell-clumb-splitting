package org.knime.knip.clump.nodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.region.BresenhamLine;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.ops.operation.iterable.unary.Mean;
import net.imglib2.ops.operation.labeling.unary.LabelingToImg;
import net.imglib2.ops.operation.randomaccessibleinterval.unary.regiongrowing.AbstractRegionGrowing;
import net.imglib2.ops.operation.randomaccessibleinterval.unary.regiongrowing.CCA;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
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
import org.knime.knip.clump.graph.Node;
import org.knime.knip.clump.graph.PrintValidPaths;
import org.knime.knip.clump.ops.FindStartingPoint;
import org.knime.knip.clump.ops.StandardDeviation;
import org.knime.knip.clump.split.CurvatureSplittingPoints;
import org.knime.knip.core.data.algebra.Complex;
import org.knime.knip.core.util.ImgUtils;

public class DTFactory<L extends Comparable<L>, T extends RealType<T> & NativeType<T>> 
	extends ValueToCellNodeFactory<LabelingValue<L>> {
	
    private final SettingsModelDouble m_sigma = createSigmaModel();
    
    private final SettingsModelDouble m_t = createTModel();
    
    private final SettingsModelDouble m_k = createKModel();
    
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
    
    protected static SettingsModelDouble createKModel(){
    	return new SettingsModelDouble("Curvature: ", 0.1d);
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
	public ValueToCellNodeModel<LabelingValue<L>, LabelingCell<Integer>> createNodeModel() {
		return new ValueToCellNodeModel<LabelingValue<L>, LabelingCell<Integer>>(){
			
			private LabelingCellFactory m_labCellFactory;

			@Override
			protected void addSettingsModels(List<SettingsModel> settingsModels) {
				settingsModels.add( m_sigma );
				settingsModels.add( m_beta );
				settingsModels.add( m_t );
				settingsModels.add( m_k );
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
					for(long[] point: c){
						ra.setPosition(point);
//						ra.get().getMapping().intern( Arrays.asList( e.getKey() ));
						ra.get().setLabel(new Integer(1000));
					}
					
					Curvature<DoubleType> curv = new Curvature<DoubleType>(c, 5, new DoubleType());
					
											
					final List<long[]> splittingPoints = new CurvatureSplittingPoints<DoubleType>(5, 
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
					
					Graph<T> graph = new Graph<T>(splittingPoints, img);
					graph.validate(img, 1);
					
					//Pruning
					for( Edge e: graph.getValidEdges()){
						Complex tangentS = c.getUnitVector(  e.getSource().getPosition() , 5);
						printTangent(tangentS, e.getSource().getPosition(), ra);
						Complex tangentD = c.getUnitVector(  e.getDestination().getPosition() , 5);
						printTangent(tangentD, e.getDestination().getPosition(), ra);
						System.out.println( e.getSource().getIndex() + "," + e.getDestination().getIndex() + ": " +  (tangentS.re() * tangentD.re() + tangentS.im() * tangentD.im() ));
						if( tangentS.re() * tangentD.re() + tangentS.im() * tangentD.im() > m_t.getDoubleValue() )
							graph.deleteEdge(e);
						Complex vector = e.getVector();
						final double tmp0 = Math.abs((vector.re() * tangentS.re() + vector.im() * tangentS.im()) / vector.getMagnitude());
						vector = e.getReverseVector();
						final double tmp1 = Math.abs((vector.re() * tangentD.re() + vector.im() * tangentD.im()) / vector.getMagnitude());
						if(  Math.max(tmp0, tmp1) > m_beta.getDoubleValue() )
							graph.deleteEdge(e);
					}
					List<Edge> list =  removeDuplicates( graph.getValidEdges() );
					Set<Node> inNodes = new HashSet<Node>( list.size() );
					Map<Node, Integer> degrees = graph.getDegrees( list );
					List<Edge> outList = new LinkedList<Edge>();
					List<Edge> complex = new LinkedList<Edge>();
//					Selection by Inference
					if ( list.size() == 1 )
						outList.addAll( list );
					else if ( list.size() > 1 ){
						for( Edge e: list){
							inNodes.add( e.getSource() );
							inNodes.add( e.getDestination() );
						}
						while( !list.isEmpty() ){
							Edge e = list.remove(0); //Polling the top element
							if( degrees.get( e.getSource() ) == 1){
								outList.add( e );
								inNodes.remove( e.getSource() );
								inNodes.remove( e.getDestination() );
							} else {
								for( Edge outE: graph.getOutgoingEdges( e.getSource() )){
									
								}
								
							}
							graph.disconnect( e.getSource() );
							graph.disconnect( e.getDestination() );
						}
						
						//Draw a split line for the remaining single nodes
//						for( Node node : inNodes){
//							long[] tmp = node.getPosition();
//							Complex tangent = c.getUnitVector(  tmp , 3);
//							double angle = tangent.phase() + Math.PI / 4.0d;
//							long sign = (long) Math.signum( Math.sin( angle ));
//							for(int j = 0; j < 100; j++){
//								ra.setPosition(tmp[0]+ sign * j, 0);
//								ra.setPosition(tmp[1]+ Math.round( j*Math.tan(angle)), 1);
//								if( !ra.get().getLabeling().isEmpty() )
//									ra.localize( tmp );
//							}
//							Cursor<LabelingType<L>> cursor = 
//									new BresenhamLine<LabelingType<L>>(ra, new Point(node.getPosition()), new Point(tmp));
//							while( cursor.hasNext() ){
//								cursor.fwd();
//								cursor.get().setLabel((L)number++);
//							}
//						}
					}
					
					for( Edge e: outList){
						draw(img.randomAccess(), e, new BitType(false));
					}
					
//					new PrintValidPaths< L>( (L)number++ ).compute(outList, ra);

					
				}
				
				new CCA<BitType>(AbstractRegionGrowing.get4ConStructuringElement(2), 
                        new BitType(false) ).compute(img, lab);
				
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
