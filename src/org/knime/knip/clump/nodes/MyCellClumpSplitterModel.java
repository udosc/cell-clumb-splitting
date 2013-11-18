package org.knime.knip.clump.nodes;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.util.Pair;
import org.knime.knip.base.data.labeling.LabelingCell;
import org.knime.knip.base.data.labeling.LabelingCellFactory;
import org.knime.knip.base.data.labeling.LabelingValue;
import org.knime.knip.base.node.NodeTools;
import org.knime.knip.base.node.ValueToCellNodeModel;
import org.knime.knip.base.nodes.filter.convolver.ConvolverNodeModel;
import org.knime.knip.clump.boundary.BinaryFactory;
import org.knime.knip.clump.boundary.Contour;
import org.knime.knip.clump.boundary.Curvature;
import org.knime.knip.clump.boundary.ShapeDescription;
import org.knime.knip.clump.dist.CrossCorrelationSimilarity;
import org.knime.knip.clump.dist.DFTDistance;
import org.knime.knip.clump.graph.Edge;
import org.knime.knip.clump.graph.Floyd;
import org.knime.knip.clump.graph.Graph;
import org.knime.knip.clump.ops.FindStartingPoint;
import org.knime.knip.clump.split.CurvatureSplit;
import org.knime.knip.clump.types.DistancesMeasuresEnum;
import org.knime.knip.core.types.ImgFactoryTypes;
import org.knime.knip.core.util.ImgUtils;

/**
 * 
 * @author Schlegel
 *
 * @param <L>
 * @param <T>
 */
public class MyCellClumpSplitterModel<L extends Comparable<L>, T extends RealType<T> & NativeType<T>>
	extends ValueToCellNodeModel<LabelingValue<L>, LabelingCell<Integer>>{

	private int m_templateIndex;
		
	private List<ShapeDescription<DoubleType>> m_templates;
	
	private final SettingsModelString m_smTemplateColumn = createTemplateColumnModel();
	
    private final SettingsModelIntegerBounded m_smOrder = createOrderColumnModel();
    
    private final SettingsModelDouble m_sigma = createSigmaModel();
    
    private final SettingsModelDouble m_threshold = createThresholdModel();
    
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
    
    protected static SettingsModelDouble createFactorModel(){
    	return new SettingsModelDouble("Factor:", 0.1d);
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
	
	public MyCellClumpSplitterModel(){
		super(new PortType[]{BufferedDataTable.TYPE});
	}
	
	@Override
	protected void addSettingsModels(List<SettingsModel> settingsModels) {
		settingsModels.add(m_smTemplateColumn);
		settingsModels.add(m_smOrder);
		settingsModels.add(m_sigma);
		settingsModels.add(m_smDistance);
		settingsModels.add(m_smFactor);
		settingsModels.add(m_threshold);
	}
	
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException{
		if (inSpecs[1] != null) {
            m_templateIndex = ((DataTableSpec)inSpecs[1]).findColumnIndex(m_smTemplateColumn.getStringValue());
        }
        if ((m_templateIndex == -1) && (inSpecs[1] != null)) {
            if ((m_templateIndex =
                    NodeTools.autoOptionalColumnSelection((DataTableSpec)inSpecs[1], m_smTemplateColumn,
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
		
		Labeling<L> out = ImgUtils.createEmptyCopy(labeling);
		RandomAccess<LabelingType<L>> ra = out.randomAccess();
		
		
		final Labeling<Integer> lab =
                new NativeImgLabeling<Integer, IntType>(
                		new ArrayImgFactory<IntType>().create(labeling, new IntType()));

		Collection<Pair<L, long[]>> map = new FindStartingPoint<L>().compute(
				labeling, 
				new LinkedList<Pair<L, long[]>>());
		
		Integer i = 0;
		for(Pair<L, long[]> start: map){
			Contour c = new BinaryFactory(binaryImg, start.getSecond()).createContour();
			
			
			if( c.length() < 20)
				continue;
			
			Curvature<DoubleType> curv = 
					new Curvature<DoubleType>(c, 5, new DoubleType(), 2.0d, this.getExecutorService());
			
			System.out.println("Processing Label: " + start.getFirst());
			for(long[] point: c){
				ra.setPosition(point);
//				ra.get().getMapping().intern( Arrays.asList( e.getKey() ));
				ra.get().setLabel((L) i);
			}
			
			final double threshold = new Mean<DoubleType, DoubleType>().
					compute(curv.iterator(), new DoubleType(0.0d)).getRealDouble();
			
			System.out.println( threshold );
			
			//Finding the possible splitting points
			Collection<long[]> splittingPoints  = new CurvatureSplit<T, L>(m_smOrder.getIntValue(),
//					m_threshold.getDoubleValue(),
					threshold,
					m_sigma.getDoubleValue()).
				compute(c, new LinkedList<long[]>());
			
			if ( !splittingPoints.isEmpty() ){
				Graph<DoubleType> graph = new Graph<DoubleType>(splittingPoints);
				graph.calc(curv, 
//						new DFTDistance<DoubleType>(DistancesMeasuresEnum.getDistanceMeasure( 
//								Enum.valueOf(DistancesMeasuresEnum.class, m_smDistance.getStringValue()) ),
//								256, 
//								32), 
						new CrossCorrelationSimilarity<DoubleType>(),
						m_templates, m_smFactor.getDoubleValue());
				graph.validate(binaryImg);
				
				//Drawing the path
				for(Edge edge: new Floyd<L, DoubleType>( graph ).getMinPath()){
//					draw(ra, edge, (L)new Integer(1338));
					draw(raBinaryImg, edge, new BitType(false));
				}
			}
			
		}
		
		new CCA<BitType>(AbstractRegionGrowing
                        .get4ConStructuringElement(2), 
                        new BitType(false) ).compute(binaryImg, lab);

		return 
    		m_labCellFactory.createCell(lab, cellValue.getLabelingMetadata()) ;
		
	}

	/**
	 * @param ra
	 * @param edge
	 */
	private void draw(RandomAccess<LabelingType<L>> ra, Edge edge, L value) {
		Point r1 = new Point(edge.getSource().getPosition());
		Point r2 = new Point(edge.getDestination().getPosition());
		Cursor<LabelingType<L>> cursor = 
				new BresenhamLine<LabelingType<L>>(ra, r1, r2);
		while( cursor.hasNext() ){
			cursor.next().setLabel( value );
		}
	}
	
	private void draw(RandomAccess<BitType> ra, Edge edge, BitType value) {
		Point r1 = new Point(edge.getSource().getPosition());
		Point r2 = new Point(edge.getDestination().getPosition());
		Cursor<BitType> cursor = 
				new BresenhamLine<BitType>(ra, r1, r2);
		while( cursor.hasNext() ){
			cursor.next().set( value.get() );
		}
	}
	
	@Override
	protected PortObject[] execute(final PortObject[] inObjects, ExecutionContext exec){
		
        final Iterator<DataRow> it = ((BufferedDataTable)inObjects[1]).iterator();
        
        m_templates = new LinkedList<ShapeDescription<DoubleType>>();
        
        while (it.hasNext()) {
            final DataRow row = it.next();
            
            List<Contour> cc = new LinkedList<Contour>();
            Labeling<L> labeling = ((LabelingValue<L>)row.getCell( m_templateIndex )).getLabeling();
            
//            final long[] dim = new long[ labeling.numDimensions() ];
//            labeling.dimensions(dim);
            
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
            	m_templates.add(
              		new Curvature<DoubleType>(
              				contour, 
              				m_smOrder.getIntValue(),new DoubleType(), 
              				m_sigma.getDoubleValue(),
              				this.getExecutorService()));
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
