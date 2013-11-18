package org.knime.knip.clump.nodes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.data.labeling.LabelingCell;
import org.knime.knip.base.data.labeling.LabelingCellFactory;
import org.knime.knip.base.exceptions.KNIPException;
import org.knime.knip.base.node.TwoValuesToCellNodeDialog;
import org.knime.knip.base.node.TwoValuesToCellNodeFactory;
import org.knime.knip.base.node.TwoValuesToCellNodeModel;
import org.knime.knip.clump.types.WarpingErrorEnums;
import org.knime.knip.clump.warp.ImgLib2WarpingError;
import org.knime.knip.core.awt.labelingcolortable.DefaultLabelingColorTable;
import org.knime.knip.core.data.img.DefaultLabelingMetadata;
import org.knime.knip.core.util.ImgUtils;

public class WarpingErrorFactory 
extends TwoValuesToCellNodeFactory<ImgPlusValue<BitType>, ImgPlusValue<BitType>>{

	@Override
	protected TwoValuesToCellNodeDialog<ImgPlusValue<BitType>, ImgPlusValue<BitType>> createNodeDialog() {
		return new TwoValuesToCellNodeDialog<ImgPlusValue<BitType>, ImgPlusValue<BitType>>() {

			@Override
			public void addDialogComponents() {
				// TODO Auto-generated method stub
				
			}
		};
	}

	@Override
	public TwoValuesToCellNodeModel<ImgPlusValue<BitType>, ImgPlusValue<BitType>, LabelingCell<String>> createNodeModel() {
		return new TwoValuesToCellNodeModel<ImgPlusValue<BitType>, ImgPlusValue<BitType>, LabelingCell<String>>() {

			private LabelingCellFactory m_imgCellFactory;
			
			@Override
			protected void addSettingsModels(List<SettingsModel> settingsModels) {
				// TODO Auto-generated method stub
				
			}

			@Override
			protected LabelingCell<String> compute(ImgPlusValue<BitType> cellValue1,
					ImgPlusValue<BitType> cellValue2) throws Exception {
				
				Img<BitType> img1 = cellValue1.getImgPlus();
				Img<BitType> img2 = cellValue2.getImgPlus();
				
				if ( !img1.iterationOrder().equals( img2.iterationOrder()))
					throw new KNIPException(
							WarpingErrorFactory.class.getCanonicalName() + 
							": Dimenssion of the images have to be equals");
				
				ImgLib2WarpingError we = new ImgLib2WarpingError( 
						WarpingErrorEnums.MERGE,
						WarpingErrorEnums.SPLIT);
				
				Img<UnsignedByteType> res = we.compute(
						img1, 
						img2, 
						ImgUtils.createEmptyCopy(img1, new UnsignedByteType()));
				
				for(WarpingErrorEnums e: we.getErrors()){
					System.out.println( e.name() + ": " + e.getNumberOfErrors());
				}
				
				
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
//						System.out.println( cImg.get().get() );
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
						
				
				
				return m_imgCellFactory.createCell(out, 
						new DefaultLabelingMetadata(cellValue1.getMetadata(), cellValue1
                                .getMetadata(), cellValue1.getMetadata(),
                                new DefaultLabelingColorTable()));
				
			}
			
            @Override
            protected void prepareExecute(final ExecutionContext exec) {
                m_imgCellFactory = new LabelingCellFactory(exec);
            }
		};
	}

}
