package org.knime.knip.clump.nodes.ftcurvature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.ops.operation.labeling.unary.LabelingToImg;
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
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.Pair;
import org.knime.knip.base.data.labeling.LabelingCell;
import org.knime.knip.clump.contour.BinaryFactory;
import org.knime.knip.clump.contour.Contour;
import org.knime.knip.clump.curvature.Curvature;
import org.knime.knip.clump.curvature.factory.KCosineCurvature;
import org.knime.knip.clump.dist.contour.CurvatureFourier;
import org.knime.knip.clump.ops.FindStartingPoint;
import org.knime.knip.clump.ops.FourierOfCurvature;
import org.knime.knip.core.algorithm.InplaceFFT;
import org.knime.knip.core.data.algebra.Complex;


/**
 * This is the model implementation of Curvature.
 * 
 *
 * @author Schlegel
 */
public class FTCurvatureNodeModel<T extends RealType<T> & NativeType<T>, L extends Comparable<L>> 
	extends NodeModel {
    
    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(FTCurvatureNodeModel.class);
        
    
    private final SettingsModelInteger m_order = 
    		createOrderModel();
   
    private final SettingsModelString m_imageColumn = 
			createImageModel();
    
    private final SettingsModelIntegerBounded m_numberOfFD =
    		createNumberModel();
    
    
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
    

    private List<List<Double>> m_curvature;
    
    private List<List<Double>> m_ft;
    
    private List<String> m_header;
    
    /**
     * Constructor for the node model.
     */
    protected FTCurvatureNodeModel() {
        super(1, 2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

//    	final int numberOfImgs = inData[0].getRowCount();

    	m_curvature = new LinkedList<List<Double>>(  );
    	m_ft = new LinkedList<List<Double>>(  );
    	m_header = new LinkedList<String>();
    	
    	int max = 0;

    	for (DataRow row: inData[0]) {
    		
    		final LabelingCell<L> cell = ((LabelingCell<L>) row.getCell( m_imageId ));
			final Labeling<L> labeling = 
    				cell.getLabeling();
			
			RandomAccessibleInterval<LabelingType<L>> rai =
					Views.zeroMin( Views.rotate(labeling, 0, 1));
			
			
			
			final Img<BitType> binaryImg = new ArrayImgFactory<BitType>().create(labeling, new BitType());
			new LabelingToImg<L, BitType>().compute(
					labeling, 
						binaryImg);
			
			
			
			Collection<Pair<L, long[]>> map = new FindStartingPoint<L>().compute(
					labeling, 
					new LinkedList<Pair<L, long[]>>());
			
    		for(Pair<L, long[]> start: map){

    			Contour contour = 
    					new BinaryFactory(binaryImg, start.getSecond()).createContour();
    		        		
        		
    			Img<DoubleType> curvature = new KCosineCurvature<DoubleType>(new DoubleType(), m_order.getIntValue()).createCurvatureImg(contour);
    			
        		List<Double> values = 
        				new ArrayList<Double>( (int) curvature.dimension(0) );
        		
        		
        		
        		Cursor<DoubleType> c = Views.iterable( curvature ).cursor();
        		
        		while( c.hasNext()){
        			values.add( c.next().getRealDouble() );
        		}
        		m_curvature.add( values );
        		m_header.add( cell.getStringValue() + ": " + start.getFirst() );
        		
        		int nDesc = (int)Math.pow(2, 
        				Math.ceil( Math.log( curvature.dimension(0) ) / Math.log(2)    )); 
        		
        		FourierOfCurvature<DoubleType> fd = new FourierOfCurvature<DoubleType>( curvature );
        		
//        		Complex[] nDescriptor = new FourierShapeDescription<DoubleType>().
//        			compute(curvature, new Complex[ m_numberOfFD.getIntValue() ]);
//        		
//        		Complex[] descriptors = new Complex[ nDesc ];
//        		for(int i = 0; i < descriptors.length; i++){
//        			descriptors[i] = i < nDescriptor.length ? nDescriptor[i] : 
//        				new Complex(0.0d, 0.0d);
//        		}
//        		
// 
//        		
//        		List<Double> ftValues = 
//        				new ArrayList<Double>( nDesc );
//        		
//        		for(Complex complex: InplaceFFT.ifft(descriptors)){
//        			ftValues.add( complex.re() );
//        		}
        		
        		List<Double> list = new LinkedList<Double>();
        		for(double d: fd.lowPass(m_numberOfFD.getIntValue()))
        			list.add( d );
        		m_ft.add( list );
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
    	
    	DataColumnSpec[] allColSpecs = new DataColumnSpec[  m_curvature.size() ];
        for(int i = 0; i < allColSpecs.length; i++)
        	allColSpecs[i] = 
        		new DataColumnSpecCreator( m_header.get(i), DoubleCell.TYPE).createSpec();
    	
    	
        BufferedDataContainer container1 = exec.createDataContainer( 
    			new DataTableSpec( allColSpecs ));
        
        BufferedDataContainer container2 = exec.createDataContainer( 
    			new DataTableSpec( allColSpecs ));
        
    	for(int i = 0; i < max; i++){
    		DataCell[] cells = new DataCell[ m_curvature.size() ];
    		DataCell[] ftCells = new DataCell[ m_ft.size() ];
    		for(int j = 0; j < m_curvature.size(); j++){
    			try {
    				cells[j] = new DoubleCell( m_curvature.get(j).get(i) );
    			} catch(IndexOutOfBoundsException e){
    				cells[j] = new MissingCell( "unknown value" );
    			}
    			try {
    				ftCells[j] = new DoubleCell( m_ft.get(j).get(i) );
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

