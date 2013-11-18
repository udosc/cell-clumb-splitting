package org.knime.knip.clump.nodes.curvature;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "Curvature" Node.
 * 
 *
 * @author Schlegel
 */
public class CurvatureNodeFactory 
        extends NodeFactory<CurvatureNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public CurvatureNodeModel createNodeModel() {
        return new CurvatureNodeModel();
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
    public NodeView<CurvatureNodeModel> createNodeView(final int viewIndex,
            final CurvatureNodeModel nodeModel) {
        return new CurvatureNodeView(nodeModel);
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
        return new CurvatureNodeDialog();
    }

}

