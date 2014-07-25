package org.knime.knip.clump.nodes.fdcurvature;

import net.imglib2.ops.types.ConnectedType;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.knip.base.data.labeling.LabelingValue;
import org.knime.knip.core.util.EnumUtils;

/**
 * <code>NodeDialog</code> for the "Curvature" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Schlegel
 */
public class FourierDescriptorNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring Curvature node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    @SuppressWarnings("unchecked")
	protected FourierDescriptorNodeDialog() {
        super();
        
        addDialogComponent(new DialogComponentColumnNameSelection( 
        		FourierDescriptorNodeModel.createImageModel(),
            	"Image: ", 
            	0, 
            	LabelingValue.class)); 
        
        addDialogComponent(new DialogComponentNumber(
                	FourierDescriptorNodeModel.createNumberModel(),
                    "Number :", /*step*/ 1, /*componentwidth*/ 8));
        
        addDialogComponent(new DialogComponentNumber(
        		FourierDescriptorNodeModel.createOrderModel(), 
        		"Order ", 5));
                    
        addDialogComponent(new DialogComponentStringSelection(FourierDescriptorNodeModel.createTypeModel(),
                "Connection Type", EnumUtils.getStringCollectionFromToString(ConnectedType.values())));
    }
}

