package org.knime.knip.clump.nodes.curvature;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.base.data.labeling.LabelingValue;

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
public class CurvatureNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring Curvature node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected CurvatureNodeDialog() {
        super();
        
        addDialogComponent(new DialogComponentColumnNameSelection( 
            	new SettingsModelString(CurvatureNodeModel.CFGKEY_IMAGE, "Image"),
            	"Image:", 
            	0, 
            	LabelingValue.class)); 
        
        addDialogComponent(new DialogComponentNumber(
                new SettingsModelIntegerBounded(
                    CurvatureNodeModel.CFGKEY_COUNT,
                    CurvatureNodeModel.DEFAULT_COUNT,
                    0, Integer.MAX_VALUE),
                    "Counter:", /*step*/ 1, /*componentwidth*/ 5));
        
        addDialogComponent(new DialogComponentNumber(
        		CurvatureNodeModel.createSigmaModel(), "sigma ", 0.01d));
                    
    }
}

