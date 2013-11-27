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
import org.knime.core.node.port.PortType;
import org.knime.core.util.Pair;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.data.labeling.LabelingCell;
import org.knime.knip.base.data.labeling.LabelingCellFactory;
import org.knime.knip.base.exceptions.KNIPException;
import org.knime.knip.base.node.TwoValuesToCellNodeModel;
import org.knime.knip.clump.types.WarpingErrorEnums;
import org.knime.knip.clump.warp.ClusterWarpingErrors;
import org.knime.knip.clump.warp.ImgLib2WarpingError;
import org.knime.knip.core.awt.labelingcolortable.DefaultLabelingColorTable;
import org.knime.knip.core.data.img.DefaultLabelingMetadata;
import org.knime.knip.core.util.ImgUtils;

public class WarpingErrorModel 
	extends TwoValuesToCellNodeModel<ImgPlusValue<BitType>, ImgPlusValue<BitType>, LabelingCell<String>> {

	private SettingsModelInteger m_smSize = WarpingErrorFactory.createSizeSettingsModel();
	
	private LabelingCellFactory m_imgCellFactory;

	private List<Pair<String, WarpingErrorEnums[]>> m_missMatches;
	
	private double m_warpingError;
	
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
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
    	DataTableSpec[] res = super.configure(inSpecs);
    	
    	
    	
    	DataTableSpec[] out = new DataTableSpec[ res.length+ 1];
    	for(int i = 0; i < res.length; i++){
    		out[i] = res[i];
    	}
    	
    	out[ res.length ] = null;
    	
        return out;
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
		
		ImgLib2WarpingError we = new ImgLib2WarpingError( 
				WarpingErrorEnums.MERGE,
				WarpingErrorEnums.SPLIT);
		
		
		
		Img<UnsignedByteType> res = we.compute(
				refImg, 
				groundTruth, 
				ImgUtils.createEmptyCopy(refImg, new UnsignedByteType()));
		m_warpingError = we.getWarpingError();
		
		
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
			
		System.out.println( cellValue1.getMetadata().getName() );
		new ClusterWarpingErrors<String>( m_smSize.getIntValue() ).compute( out, we.getErrors());
		
		m_missMatches.add( new Pair<String, WarpingErrorEnums[]>(
				cellValue1.getMetadata().getName(), we.getErrors()) );
		
		return m_imgCellFactory.createCell(out, 
				new DefaultLabelingMetadata(cellValue1.getMetadata(), cellValue1
                        .getMetadata(), cellValue1.getMetadata(),
                        new DefaultLabelingColorTable()));
		
	}
	
    @Override
    protected void prepareExecute(final ExecutionContext exec) {
        m_imgCellFactory = new LabelingCellFactory(exec);
        m_missMatches = new LinkedList<Pair<String, WarpingErrorEnums[]>>();
    }
    
    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
    	PortObject[] res = super.execute(inObjects, exec);
		WarpingErrorEnums[] first =  m_missMatches.size() > 0 ? m_missMatches.get(0).getSecond() :
			WarpingErrorEnums.values();
    	DataColumnSpec[] dataSpec = new DataColumnSpec[  first.length + 1];
    	dataSpec[0] = new DataColumnSpecCreator("Warping Error", DoubleCell.TYPE).createSpec();
        for(int i = 1; i < dataSpec.length; i++)
        	dataSpec[i] = 
        		new DataColumnSpecCreator(first[i-1].name(), DoubleCell.TYPE).createSpec();
        
        
        BufferedDataContainer container = exec.createDataContainer( 
    			new DataTableSpec( dataSpec ));
       
        
        for(Pair<String, WarpingErrorEnums[]> pair: m_missMatches){
        	final DataCell[] cells = new DataCell[ dataSpec.length ];
        	cells[0] = new DoubleCell( m_warpingError * 100000);
        	for(int i = 1; i < dataSpec.length; i++)
        		cells[i] = new DoubleCell( pair.getSecond()[i-1].getNumberOfErrors() );
        	container.addRowToTable( new DefaultRow( new RowKey(pair.getFirst()), cells) );
        }
        
        container.close();
        
        PortObject[] output = new PortObject[ res.length + 1 ];          
        for(int i = 0; i < res.length; i++)
        	output[i] = res[i];
        output[ res.length ] = container.getTable();
        return output;
    	
    }
    
}