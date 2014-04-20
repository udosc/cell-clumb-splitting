package org.knime.knip.clump.nodes.ftcurvature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.imglib2.RealRandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.ops.operation.labeling.unary.LabelingToImg;
import net.imglib2.ops.types.ConnectedType;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.MissingCell;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.Pair;
import org.knime.knip.base.data.labeling.LabelingCell;
import org.knime.knip.clump.contour.AbstractBinaryFactory;
import org.knime.knip.clump.contour.Contour;
import org.knime.knip.clump.contour.FindStartingPoints;
import org.knime.knip.clump.curvature.factory.KCosineCurvature;
import org.knime.knip.clump.fourier.FourierOfCurvature;
import org.knime.knip.clump.util.MyUtils;



/**
 * This is the model implementation of Curvature.
 * 
 *
 * @author Schlegel
 */
public class FTCurvatureNodeModel<T extends RealType<T> & NativeType<T>, L extends Comparable<L>> 
	extends NodeModel {
    
    private final SettingsModelInteger m_order = 
    		createOrderModel();
   
    private final SettingsModelString m_imageColumn = 
			createImageModel();
    
    private final SettingsModelIntegerBounded m_numberOfFD =
    		createNumberModel();
    
    private final SettingsModelString m_type = createTypeModel();
    
	protected static SettingsModelString createTypeModel() {
		return new SettingsModelString("connection_type", ConnectedType.values()[0].toString());
	}
    
    protected static SettingsModelString createImageModel(){
    	return new SettingsModelString("Image ID", "Image");
    }
    
    protected static SettingsModelIntegerBounded createNumberModel(){
    	return new SettingsModelIntegerBounded("Number: ", 
    			8,
    			0,
    			Integer.MAX_VALUE);
    }
    
    protected static SettingsModelIntegerBounded createOrderModel(){
        return new SettingsModelIntegerBounded("Order: ",
                    5,
                    0, 
                    Integer.MAX_VALUE);
    }
    
    private int m_imageId;
    

    private List<List<Double>> m_curvatures;
    
    private List<List<Double>> m_ftCurvatures;
    
    private List<String> m_headers;
    
    /**
     * Constructor for the node model.
     */
    protected FTCurvatureNodeModel() {
        super(1, 2);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
	@Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

//    	final int numberOfImgs = inData[0].getRowCount();

    	m_curvatures = new LinkedList<List<Double>>(  );
    	m_ftCurvatures = new LinkedList<List<Double>>(  );
    	m_headers = new LinkedList<String>();
    	
    	int max = 0;

    	for (DataRow row: inData[0]) {
    		
    		final LabelingCell<L> cell = ((LabelingCell<L>) row.getCell( m_imageId ));
			final Labeling<L> labeling = 
    				cell.getLabeling();	
			
			
			final Img<BitType> binaryImg = new ArrayImgFactory<BitType>().create(labeling, new BitType());
			new LabelingToImg<L, BitType>().compute(
					labeling, 
						binaryImg);
			
			
			
			final Collection<Pair<L, long[]>> map = new FindStartingPoints<L>().compute(
					labeling, 
					new LinkedList<Pair<L, long[]>>());
			
    		for(Pair<L, long[]> start: map){

    			final Contour contour = 
    					AbstractBinaryFactory.factory(binaryImg, start.getSecond(), m_type.getStringValue()).createContour();
    		        		
        		
    			final Img<DoubleType> curvature = new KCosineCurvature<DoubleType>(new DoubleType(), m_order.getIntValue()).createCurvatureImg(contour);
    
    			
    			final FourierOfCurvature<DoubleType> fd = new FourierOfCurvature<DoubleType>( curvature );
    			final int nDesc = fd.getNumberOfDescriptors();
    			
        		List<Double> values = 
        				new ArrayList<Double>( (int) curvature.dimension(0) );
        		
        		//Interpolate the data to be the same length as the ft ones
        		final RealRandomAccess<DoubleType> rra = Views.interpolate( curvature,
        				new NLinearInterpolatorFactory<DoubleType>()).realRandomAccess();
        		        		
        		final double step = (MyUtils.numElements(curvature) - 1.0d)/ (double) nDesc ;
        		
        		for(int i = 0; i < nDesc; i++){
        			rra.setPosition((i*step), 0);
        			values.add( rra.get().getRealDouble() );
        		}

        		m_curvatures.add( values );
        		m_headers.add( cell.getStringValue() + ": " + start.getFirst() );
        		        		
        		
        		
        		
        		List<Double> list = new LinkedList<Double>();
        		for(double d: fd.lowPass(m_numberOfFD.getIntValue()))
        			list.add( d );
        		m_ftCurvatures.add( list );
     		
        		if( values.size() > max )
        			max = values.size();
    		}
        }
    	
    	DataColumnSpec[] allColSpecs = new DataColumnSpec[  m_curvatures.size() ];
        for(int i = 0; i < allColSpecs.length; i++)
        	allColSpecs[i] = 
        		new DataColumnSpecCreator( m_headers.get(i), DoubleCell.TYPE).createSpec();
    	
    	
        BufferedDataContainer container1 = exec.createDataContainer( 
    			new DataTableSpec( allColSpecs ));
        
        BufferedDataContainer container2 = exec.createDataContainer( 
    			new DataTableSpec( allColSpecs ));
        
    	for(int i = 0; i < max; i++){
    		DataCell[] cells = new DataCell[ m_curvatures.size() ];
    		DataCell[] ftCells = new DataCell[ m_ftCurvatures.size() ];
    		for(int j = 0; j < m_curvatures.size(); j++){
    			try {
    				cells[j] = new DoubleCell( m_curvatures.get(j).get(i) );
    			} catch(IndexOutOfBoundsException e){
    				cells[j] = new MissingCell( "unknown value" );
    			}
    			try {
    				ftCells[j] = new DoubleCell( m_ftCurvatures.get(j).get(i) );
    			} catch(IndexOutOfBoundsException e){
    				ftCells[j] = new MissingCell( "unknown value" );
    			}
    		}
    		DataRow row = new DefaultRow( new RowKey("" + i), cells);
            container1.addRowToTable(row);
            container2.addRowToTable( new DefaultRow("" + i, ftCells));
    	}
    	
    	container1.close();
    	container2.close();
 
        return new BufferedDataTable[]{ container1.getTable(), container2.getTable() };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        
    	m_imageId 	= inSpecs[0].findColumnIndex(m_imageColumn.getStringValue());
    	
        return new DataTableSpec[]{null, null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {       
        m_numberOfFD.saveSettingsTo(settings);
        m_imageColumn.saveSettingsTo(settings);
        m_order.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        
        m_numberOfFD.loadSettingsFrom(settings);
        m_imageColumn.loadSettingsFrom(settings);
        m_order.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {

        m_numberOfFD.validateSettings(settings);
        m_imageColumn.validateSettings(settings);
        m_order.validateSettings(settings);

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

}

