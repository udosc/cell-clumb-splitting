package org.knime.knip.clump.nodes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSetFactory;
import org.knime.core.node.config.ConfigRO;


/**
 * 
 * @author Schlegel
 */
public class ClumpSplittingNodeSetFactory implements NodeSetFactory {

	private Map<String, String> m_nodeFactories = new HashMap<String, String>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<String> getNodeFactoryIds() {

		m_nodeFactories.put(SplittPointNodeFactory.class.getCanonicalName(),
				"/master");
		m_nodeFactories.put(MyContourExtracter.class.getCanonicalName(), 
				"/master");
		m_nodeFactories.put(TemplateCellClumpSplitterFactory.class.getCanonicalName(),
				"/master");
		m_nodeFactories.put(CurvatureNodeFactory.class.getCanonicalName(),
				"/master");
		m_nodeFactories.put(WarpingErrorFactory.class.getCanonicalName(), 
				"/master");
		m_nodeFactories.put(CSSFactory.class.getCanonicalName(),
				"/master");



		return m_nodeFactories.keySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends NodeFactory<? extends NodeModel>> getNodeFactory(
			String id) {
		try {
			return (Class<? extends NodeFactory<? extends NodeModel>>) Class
					.forName(id);
		} catch (ClassNotFoundException e) {
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCategoryPath(String id) {
		return m_nodeFactories.get(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAfterID(String id) {
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConfigRO getAdditionalSettings(String id) {
		return null;
	}

}
