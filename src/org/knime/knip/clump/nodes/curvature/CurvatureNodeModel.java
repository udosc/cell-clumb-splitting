package org.knime.knip.clump.nodes.curvature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.labeling.Labeling;
import net.imglib2.meta.ImgPlus;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.ImgUtil;
import net.imglib2.view.Views;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.MissingCell;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.knip.base.KNIPConstants;
import org.knime.knip.base.ThreadPoolExecutorService;
import org.knime.knip.base.data.img.ImgPlusCell;
import org.knime.knip.base.data.labeling.LabelingCell;
import org.knime.knip.clump.boundary.Contour;
import org.knime.knip.clump.boundary.Curvature;
import org.knime.knip.clump.boundary.WatershedFactory;
import org.knime.knip.clump.ops.FourierShapeDescription;
import org.knime.knip.clump.util.MyUtils;
import org.knime.knip.core.algorithm.InplaceFFT;
import org.knime.knip.core.data.algebra.Complex;
import org.knime.knip.core.ops.filters.GaussNativeTypeOp;
import org.knime.knip.core.util.ImgUtils;


/**
 * This is the model implementation of Curvature.
 * 
 *
 * @author Schlegel
 */
public class CurvatureNodeModel<T extends RealType<T> & NativeType<T>, L extends Comparable<L>> 
	extends NodeModel {
    
    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(CurvatureNodeModel.class);
        
    /** the settings key which is used to retrieve and 
        store the settings (from the dialog or from a settings file)    
       (package visibility to be usable from the dialog). */
	static final String CFGKEY_COUNT = "Count";

    /** initial default count value. */
    static final int DEFAULT_COUNT = 5;

    static final String CFGKEY_IMAGE 	= "image";
    
    
    private int m_imageId;
    
    private final SettingsModelDouble m_sigma = createSigmaModel();
    
    protected static SettingsModelDouble createSigmaModel(){
    	return new SettingsModelDouble("Sigma: ", 2.0d);
    }
    
    private final SettingsModelString m_imageColumn = 
			new SettingsModelString(CFGKEY_IMAGE, "Image");
    
    private final SettingsModelIntegerBounded m_order =
        new SettingsModelIntegerBounded(CurvatureNodeModel.CFGKEY_COUNT,
                    CurvatureNodeModel.DEFAULT_COUNT,
                    0, Integer.MAX_VALUE);
    

    private List<List<Double>> m_data;
    
    /**
     * Constructor for the node model.
     */
    protected CurvatureNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

    	final int numberOfImgs = inData[0].getRowCount();

    	m_data = new ArrayList<List<Double>>( numberOfImgs );
    	
    	int max = 0;
    	
    	
    	for (DataRow row: inData[0]) {
    		final Labeling<L> labeling = ((LabelingCell<L>) row.getCell( m_imageId )).getLabeling();
    		
    		for(L label: labeling.getLabels()){
    			
    			
    			
    			Contour contour = 
    					new WatershedFactory<DoubleType, L>(
    							labeling.getIterableRegionOfInterest(label), 
    							labeling.getArea(label), 
    							new DoubleType()).createContour();
    			
        		Curvature<DoubleType> curvature = new Curvature<DoubleType>(
        						contour, 
        						m_order.getIntValue(), 
        						new DoubleType());
        		
        		curvature.gaussian(m_sigma.getDoubleValue(),
        						new ThreadPoolExecutorService(
        					            KNIMEConstants.GLOBAL_THREAD_POOL.createSubPool(KNIPConstants.THREADS_PER_NODE)));
    			
        						
        		
        		List<Double> values = new ArrayList<Double>( (int) curvature.getImg().dimension(0) );
        		
        		
        		
        		Cursor<DoubleType> c = Views.iterable( curvature.getImg() ).cursor();
        		
        		while( c.hasNext()){
        			values.add( c.next().getRealDouble() );
        		}
        		m_data.add( values );
//       
//        		
//        		Complex[] complex = new Complex[ 256 ];
//        		Complex[] descriptor = new Complex[256];
//        		int n = 0;
//        		while( c.hasNext() ){
//        			final double res = c.next().getRealDouble();
//        			values.add( res );
//        			complex[n++] = new Complex( res, 0);
//        		}
//        		
//        		for(int i = n; i < 256; i++)
//        			complex[i] = new Complex(0,0);
//        		
//	            Complex[] transformed = InplaceFFT.fft( complex );
//	            double dcMagnitude = transformed[0].getMagnitude();
////	            dcMagnitude = 1.0d;
//	            
//	            for (int t = 1; t < (transformed.length / 2); t++) {
//	                descriptor[t - 1] = 
//	                		new Complex(transformed[t].re() / dcMagnitude, transformed[t].im() / dcMagnitude);
//	                System.out.println(t-1 + ": " + descriptor[t - 1]);
//	            }
        		
//	            Complex[] nDescriptor = new FourierShapeDescription<DoubleType>(32).compute(curvature,
//	            		new Complex[16]);
//
//	            for(int i = 0; i < nDescriptor.length; i++){
//	            	if( nDescriptor[i] == null )
//	            		nDescriptor[i] = new Complex(0.0d, 0.0d);
//	            }
//	            
//	            Complex[] transformed = InplaceFFT.ifft(nDescriptor);
//	            
//	            List<Double> fftvalues = new ArrayList<Double>( (int) curvature.dimension(0) );
//	            for(int i = 0; i < (int) curvature.dimension(0); i++){
//	            	fftvalues.add(i, transformed[i].re() );
//	            }
//	           
//        		m_data.add( values );
//        		m_data.add( fftvalues );
////        		Complex[] re = InplaceFFT.ifft(x);
//        		
        		if( values.size() > max )
        			max = values.size();
    		}
        }
    	
    	DataColumnSpec[] allColSpecs = new DataColumnSpec[  m_data.size() ];
        for(int i = 0; i < allColSpecs.length; i++)
        	allColSpecs[i] = 
        		new DataColumnSpecCreator("Cell " + i, DoubleCell.TYPE).createSpec();
    	
    	
        BufferedDataContainer container = exec.createDataContainer( 
    			new DataTableSpec( allColSpecs ));
        
    	for(int i = 0; i < max; i++){
    		DataCell[] cells = new DataCell[ m_data.size() ];
    		for(int j = 0; j < m_data.size(); j++){
    			try {
    				cells[j] = new DoubleCell( m_data.get(j).get(i) );
    			} catch(IndexOutOfBoundsException e){
    				cells[j] = new DoubleCell(-0.01d);
    			}
    		}
    		DataRow row = new DefaultRow( new RowKey("Row_ " + i), cells);
            container.addRowToTable(row);
    	}
    	
    	container.close();
 
        return new BufferedDataTable[]{ container.getTable() };
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
    	
        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {       
        m_order.saveSettingsTo(settings);
        m_imageColumn.saveSettingsTo(settings);
        m_sigma.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        
        m_order.loadSettingsFrom(settings);
        m_imageColumn.loadSettingsFrom(settings);
        m_sigma.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {

        m_order.validateSettings(settings);
        m_imageColumn.validateSettings(settings);
        m_sigma.validateSettings(settings);

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

