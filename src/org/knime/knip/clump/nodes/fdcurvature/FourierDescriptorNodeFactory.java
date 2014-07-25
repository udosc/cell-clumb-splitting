package org.knime.knip.clump.nodes.fdcurvature;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "Curvature" Node.
 * 
 *
 * @author Schlegel
 */
public class FourierDescriptorNodeFactory 
        extends NodeFactory<FourierDescriptorNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public FourierDescriptorNodeModel createNodeModel() {
        return new FourierDescriptorNodeModel();
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
    public NodeView<FourierDescriptorNodeModel> createNodeView(final int viewIndex,
            final FourierDescriptorNodeModel nodeModel) {
        return new FourierDescriptorNodeView(nodeModel);
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
        return new FourierDescriptorNodeDialog();
    }

}

