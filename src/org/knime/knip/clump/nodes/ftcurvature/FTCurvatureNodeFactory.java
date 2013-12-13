package org.knime.knip.clump.nodes.ftcurvature;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "Curvature" Node.
 * 
 *
 * @author Schlegel
 */
public class FTCurvatureNodeFactory 
        extends NodeFactory<FTCurvatureNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public FTCurvatureNodeModel createNodeModel() {
        return new FTCurvatureNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<FTCurvatureNodeModel> createNodeView(final int viewIndex,
            final FTCurvatureNodeModel nodeModel) {
        return new FTCurvatureNodeView(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new FTCurvatureNodeDialog();
    }

}

