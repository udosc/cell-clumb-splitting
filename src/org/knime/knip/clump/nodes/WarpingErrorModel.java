package org.knime.knip.clump.nodes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.util.Pair;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.data.labeling.LabelingCell;
import org.knime.knip.base.data.labeling.LabelingCellFactory;
import org.knime.knip.base.exceptions.KNIPException;
import org.knime.knip.base.node.TwoValuesToCellNodeModel;
import org.knime.knip.clump.types.WarpingErrorEnums;
import org.knime.knip.clump.warping.ClusterWarpingErrors;
import org.knime.knip.clump.warping.ConvertWarpingMismatches;
import org.knime.knip.clump.warping.MyWarpingError;
import org.knime.knip.core.awt.labelingcolortable.DefaultLabelingColorTable;
import org.knime.knip.core.data.img.DefaultLabelingMetadata;
import org.knime.knip.core.util.ImgUtils;

import trainableSegmentation.metrics.ClusteredWarpingMismatches;

/**
 * 
 * @author Udo
 *
 */
public class WarpingErrorModel 
	extends TwoValuesToCellNodeModel<ImgPlusValue<BitType>, ImgPlusValue<BitType>, LabelingCell<String>> {

	private SettingsModelInteger m_smSize = WarpingErrorFactory.createSizeSettingsModel();
	
	private LabelingCellFactory m_imgCellFactory;

	private List<Pair<String, ClusteredWarpingMismatches>> m_missMatches;
	
	private List<Double> m_warpingError;
	
	private List<Double> m_randError;
	
	public WarpingErrorModel(){
		super( null, new PortType[]{BufferedDataTable.TYPE} );
	}
	
	@Override
	protected void addSettingsModels(List<SettingsModel> settingsModels) {
		settingsModels.add( m_smSize );
//		settingsModels.add( m_smGTIndex );
//		settingsModels.add( m_smRefIndex );
		
	}
	

	@Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		final PortObjectSpec firstSpec = super.configure(inSpecs)[0];
		return new PortObjectSpec[]{firstSpec, null};
    }
	

	@Override
	protected LabelingCell<String> compute(ImgPlusValue<BitType> cellValue1,
			ImgPlusValue<BitType> cellValue2) throws Exception {
		
		ImgPlus<BitType> groundTruth 	= cellValue1.getImgPlus();
		ImgPlus<BitType> refImg 		= cellValue2.getImgPlus();
		
		
		
		if ( !refImg.iterationOrder().equals( groundTruth.iterationOrder()))
			throw new KNIPException(
					WarpingErrorFactory.class.getCanonicalName() + 
					": Dimenssion of the images have to be equals");
		
		MyWarpingError<UnsignedByteType> we = new MyWarpingError<UnsignedByteType>(
				new UnsignedByteType(), 
				100, 
				WarpingErrorEnums.MERGE,
				WarpingErrorEnums.SPLIT);
		
		
		
		Img<UnsignedByteType> res = we.compute( 
				groundTruth, 
				refImg,
				ImgUtils.createEmptyCopy(refImg, new UnsignedByteType()));
		m_warpingError.add( we.getWarpingError() * 100000);
		m_randError.add( we.getRandError() * 100000 );
		
		
		Labeling<String> out = new NativeImgLabeling<String, UnsignedByteType>(
				ImgUtils.createEmptyCopy(res));
		
		Cursor<UnsignedByteType> cImg = res.cursor();
		Cursor<LabelingType<String>> cLabel = out.cursor();
		final Map<Integer, List<String>> labels = new HashMap<Integer, List<String>>(16);
		List<String> name;
		while( cImg.hasNext() ){
			cImg.fwd();
			cLabel.fwd();
			if( cImg.get().get() == 0){
				continue;
			}
            if ((name = labels.get(cImg.get().get())) == null) {

                final List<String> tmp = Arrays.asList( 
                		WarpingErrorEnums.getWarpingErrorEnum( cImg.get().getInteger()).name());
                labels.put(cImg.get().get(), tmp);
                name = cLabel.get().getMapping().intern(tmp);

            }
            cLabel.get().setLabeling(name);
		}
			
//		System.out.println( cellValue1.getMetadata().getName() );
//		new ClusterWarpingErrors<String>( m_smSize.getIntValue() ).compute( out, we.getErrors());
		
		m_missMatches.add( new Pair<String, ClusteredWarpingMismatches>(
				cellValue1.getMetadata().getName(), we.getMismatches()) );
		
		return m_imgCellFactory.createCell(out, 
				new DefaultLabelingMetadata(cellValue1.getMetadata(), cellValue1
                        .getMetadata(), cellValue1.getMetadata(),
                        new DefaultLabelingColorTable()));
		
	}
	
    @Override
    protected void prepareExecute(final ExecutionContext exec) {
        m_imgCellFactory 	= new LabelingCellFactory(exec);
        m_missMatches 		= new LinkedList<Pair<String, ClusteredWarpingMismatches>>();
        m_randError 		= new LinkedList<Double>();
        m_warpingError 		= new LinkedList<Double>();
    }
    
    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
    	PortObject[] res = super.execute(inObjects, exec);
    	DataColumnSpec[] dataSpec = new DataColumnSpec[  WarpingErrorEnums.values().length + 2];
    	dataSpec[0] = new DataColumnSpecCreator("Warping Error", DoubleCell.TYPE).createSpec();
        for(int i = 0; i < WarpingErrorEnums.values().length; i++)
        	dataSpec[i+1] = 
        		new DataColumnSpecCreator(WarpingErrorEnums.values()[i].name(), DoubleCell.TYPE).createSpec();
        
        dataSpec[ dataSpec.length - 1 ] =
        		new DataColumnSpecCreator("Rand Error", DoubleCell.TYPE).createSpec();
        
        
        BufferedDataContainer container = exec.createDataContainer( 
    			new DataTableSpec( dataSpec ));
       
       
        
        int n = 0;
        for(Pair<String, ClusteredWarpingMismatches> pair: m_missMatches){
        	final DataCell[] cells = new DataCell[ dataSpec.length ];
        	cells[0] = new DoubleCell( m_warpingError.get( n ) );
        	
        	 WarpingErrorEnums[] tmp = new ConvertWarpingMismatches().compute(
        			 pair.getSecond(), 
        			 WarpingErrorEnums.values());
        	
        	for(int i = 0; i <  tmp.length; i++)
        		cells[i + 1] = new DoubleCell( tmp[i].getNumberOfErrors() );
        	cells[ dataSpec.length -1 ] = new DoubleCell( m_randError.get( n ) );
        	container.addRowToTable( new DefaultRow( new RowKey(pair.getFirst()), cells) );
        	n++;
        }
        
        container.close();
        
        PortObject[] output = new PortObject[ res.length + 1 ];          
        for(int i = 0; i < res.length; i++)
        	output[i] = res[i];
        output[ res.length ] = container.getTable();
        return output;
    	
    }
    
}