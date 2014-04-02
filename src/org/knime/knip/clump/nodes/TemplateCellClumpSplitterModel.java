package org.knime.knip.clump.nodes;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.BresenhamLine;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.ops.operation.labeling.unary.LabelingToImg;
import net.imglib2.ops.operation.randomaccessibleinterval.unary.regiongrowing.AbstractRegionGrowing;
import net.imglib2.ops.operation.randomaccessibleinterval.unary.regiongrowing.CCA;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.util.Pair;
import org.knime.knip.base.data.labeling.LabelingCell;
import org.knime.knip.base.data.labeling.LabelingCellFactory;
import org.knime.knip.base.data.labeling.LabelingValue;
import org.knime.knip.base.node.NodeUtils;
import org.knime.knip.base.node.ValueToCellNodeModel;
import org.knime.knip.base.nodes.filter.convolver.ConvolverNodeModel;
import org.knime.knip.clump.contour.BinaryFactory;
import org.knime.knip.clump.contour.Contour;
import org.knime.knip.clump.curvature.KCosineCurvature;
import org.knime.knip.clump.dist.contour.CurvatureDistance;
import org.knime.knip.clump.dist.contour.CurvatureFourier;
import org.knime.knip.clump.graph.Edge;
import org.knime.knip.clump.graph.GraphSplitting;
import org.knime.knip.clump.ops.FindStartingPoint;
import org.knime.knip.clump.split.CurvatureSplittingPoints;
import org.knime.knip.clump.types.DistancesMeasuresEnum;

/**
 * 
 * @author Schlegel
 *
 * @param <L>
 * @param <T>
 */
public class TemplateCellClumpSplitterModel<L extends Comparable<L>, T extends RealType<T> & NativeType<T>>
	extends ValueToCellNodeModel<LabelingValue<L>, LabelingCell<Integer>>{

	private int m_templateIndex;
			
	private List<Contour> m_templates;
	
	private final SettingsModelString m_smTemplateColumn = createTemplateColumnModel();
	
    private final SettingsModelIntegerBounded m_smOrder = createOrderColumnModel();
    
    private final SettingsModelDouble m_sigma = createSigmaModel();
    
//    private final SettingsModelDouble m_threshold = createThresholdModel();
    
    private final SettingsModelDouble m_smFactor = createFactorModel();
    
    private final SettingsModelString m_smDistance = createDistancesModel();
	
	private LabelingCellFactory m_labCellFactory;
		
	
    protected static SettingsModelString createTemplateColumnModel() {
        return new SettingsModelString("column_template", "");
    }
    
    protected static SettingsModelDouble createSigmaModel(){
    	return new SettingsModelDouble("Sigma: ", 2.0d);
    }
    
    protected static SettingsModelDouble createThresholdModel(){
    	return new SettingsModelDouble("Threshold: ", 0.2d);
    }
    
    protected static SettingsModelDoubleBounded createFactorModel(){
    	return new SettingsModelDoubleBounded("Factor ", 0.1, 0.0, 100.0);
    }
    
    protected static SettingsModelString createDistancesModel(){
    	return new SettingsModelString("Distance", DistancesMeasuresEnum.CANBERRA.name());
    }
    
    protected static SettingsModelIntegerBounded createOrderColumnModel(){
        return new SettingsModelIntegerBounded("Order ",
                5,
                0, Integer.MAX_VALUE);
    }
    
    private final NodeLogger LOGGER = NodeLogger.getLogger(ConvolverNodeModel.class);
	
	public TemplateCellClumpSplitterModel(){
		super(new PortType[]{BufferedDataTable.TYPE});
	}
	
	@Override
	protected void addSettingsModels(List<SettingsModel> settingsModels) {
		settingsModels.add(m_smTemplateColumn);
		settingsModels.add(m_smOrder);
		settingsModels.add(m_sigma);
		settingsModels.add(m_smDistance);
		settingsModels.add(m_smFactor);
//		settingsModels.add(m_threshold);
	}
	
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException{
		if (inSpecs[1] != null) {
            m_templateIndex = ((DataTableSpec)inSpecs[1]).findColumnIndex(m_smTemplateColumn.getStringValue());
        }
        if ((m_templateIndex == -1) && (inSpecs[1] != null)) {
            if ((m_templateIndex =
            		NodeUtils.autoOptionalColumnSelection((DataTableSpec)inSpecs[1], m_smTemplateColumn,
                                                          LabelingValue.class)) >= 0) {
                setWarningMessage("Auto-configure Column: " + m_smTemplateColumn.getStringValue());
            } else {
                throw new InvalidSettingsException("No column selected!");
            }
        }
        
        return super.configure(inSpecs);
	}

	@Override
	protected LabelingCell<Integer> compute(LabelingValue<L> cellValue) throws Exception {
		
		
		Labeling<L> labeling = cellValue.getLabelingCopy();
		long[] dim = new long[ labeling.numDimensions() ]; 
		labeling.dimensions(dim);
		
		Img<BitType> binaryImg = new ArrayImgFactory<BitType>().create(labeling, new BitType());
		new LabelingToImg<L, BitType>().compute(labeling, 
					binaryImg);
		
		RandomAccess<BitType> raBinaryImg = binaryImg.randomAccess();
		
//		Labeling<L> out = ImgUtils.createEmptyCopy(labeling);
//		RandomAccess<LabelingType<L>> ra = out.randomAccess();
		
		
		final Labeling<Integer> lab =
                new NativeImgLabeling<Integer, IntType>(
                		new ArrayImgFactory<IntType>().create(labeling, new IntType()));

		Collection<Pair<L, long[]>> map = new FindStartingPoint<L>().compute(
				labeling, 
				new LinkedList<Pair<L, long[]>>());

		for(Pair<L, long[]> start: map){
			Contour contour = new BinaryFactory(binaryImg, start.getSecond()).createContour();
			
			
			if( contour.length() < 20)
				continue;
			
//			Curvature<DoubleType> curv = 
//					new Curvature<DoubleType>(
//							contour, 
//							5, 
//							new DoubleType());
			

			
			System.out.println(cellValue.getLabelingMetadata().getName() + " Processing Label: " + start.getFirst());
//			for(long[] point: contour){
//				ra.setPosition(point);
////				ra.get().getMapping().intern( Arrays.asList( e.getKey() ));
//				ra.get().setLabel((L) i);
//			}
			
					
			
//			System.out.println( mean + std );
			
//			curv.gaussian(m_sigma.getDoubleValue(), 
//					this.getExecutorService() );
			
	        
			GraphSplitting<DoubleType, Integer> cs = new GraphSplitting<DoubleType, Integer>(
//					new CurvatureFourier<DoubleType>(m_templates, new KCosineCurvature<DoubleType>(new DoubleType(), m_smOrder.getIntValue()), 32),
	        		new CurvatureDistance<DoubleType>(m_templates, new KCosineCurvature<DoubleType>(new DoubleType(), m_smOrder.getIntValue()), 1, this.getExecutorService(), m_sigma.getDoubleValue()),
//					new DFTDistance<DoubleType>(m_templates, new DoubleType(), 16, false),
	        		binaryImg, 
	        		m_smFactor.getDoubleValue());
			
			
			
			cs.compute(contour, new CurvatureSplittingPoints<DoubleType>(5,
					15, 
					new DoubleType(),
					m_sigma.getDoubleValue()));
			
//			cs.draw(lab.randomAccess());
			
//			System.out.println( cs );
//			if ( points != null ){
//				new PrintMinPath<DoubleType, BitType>( new BitType( false )).
//					compute(points, raBinaryImg);
//			}

			cs.printMatrix( cs.getMatrix() );
			Collection<Pair<Point, Point>> points = cs.printGreedy();
			
			if( points == null ){
				continue;
			}
			
			for(Pair<Point, Point> line: points){
				Cursor<BitType> cursor = 
						new BresenhamLine<BitType>(raBinaryImg, line.getFirst(), line.getSecond());
				while( cursor.hasNext() ){
					cursor.next().set( false );
				}
			}
			
		}
		
		new CCA<BitType>(AbstractRegionGrowing.get4ConStructuringElement(2), 
                        new BitType(false) ).compute(binaryImg, lab);

		return 
    		m_labCellFactory.createCell(lab, cellValue.getLabelingMetadata()) ;
		
	}

	/**
	 * @param ra
	 * @param edge
	 */
	@SuppressWarnings("unused")
	private void draw(RandomAccess<LabelingType<L>> ra, Edge edge, L value) {
		Point r1 = new Point(edge.getSource().getPosition());
		Point r2 = new Point(edge.getDestination().getPosition());
		Cursor<LabelingType<L>> cursor = 
				new BresenhamLine<LabelingType<L>>(ra, r1, r2);
		while( cursor.hasNext() ){
			cursor.next().setLabel( value );
		}
	}
	
	@SuppressWarnings("unused")
	private void draw(RandomAccess<BitType> ra, long[] p1, long[] p2, BitType value) {
		Cursor<BitType> cursor = 
				new BresenhamLine<BitType>(ra, new Point(p1), new Point(p2));
		while( cursor.hasNext() ){
			cursor.next().set( value.get() );
		}
	}

	
	@Override
	protected PortObject[] execute(final PortObject[] inObjects, ExecutionContext exec){
		
        final Iterator<DataRow> it = ((BufferedDataTable)inObjects[1]).iterator();
        
        m_templates = new LinkedList<Contour>();
        
        List<RandomAccessibleInterval<DoubleType>> list = new LinkedList<RandomAccessibleInterval<DoubleType>>( );
        
        while (it.hasNext()) {
            final DataRow row = it.next();
            
            List<Contour> cc = new LinkedList<Contour>();
            Labeling<L> labeling = ((LabelingValue<L>)row.getCell( m_templateIndex )).getLabeling();
            
			Collection<Pair<L, long[]>> startPoints = new FindStartingPoint<L>().compute(
					labeling, 
					new LinkedList<Pair<L, long[]>>());
			
			Img<BitType> binaryImg = new ArrayImgFactory<BitType>().create(labeling, new BitType());
					new LabelingToImg<L, BitType>().compute(labeling, 
					binaryImg);
			
			for(Pair<L, long[]> p: startPoints){
				cc.add( new BinaryFactory(binaryImg, p.getSecond()).createContour() );
			}

			
            for(Contour contour: cc){
            	
//            	list.add( new KCosineCurvature<DoubleType>(new DoubleType(), 5).createCurvatureImg(contour) );
//            	
//            	
//            	
//            	Curvature<DoubleType> curvature = new Curvature<DoubleType>(
//            			contour, 
//          				m_smOrder.getIntValue(),new DoubleType());
//            	curvature.gaussian( m_sigma.getDoubleValue(),
//            			this.getExecutorService());
            	m_templates.add( contour );
            }
        }
                
        PortObject[] res = null;

        try {
            res = super.execute(inObjects, exec);
        } catch (final Exception e) {
        	e.printStackTrace();
            LOGGER.warn("Exception during execution of MyCellClumpSplitter + " + e);
        } 
        return res;
	}
	
	
    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareExecute(final ExecutionContext exec) {
        m_labCellFactory = new LabelingCellFactory(exec);
    }
    
}
