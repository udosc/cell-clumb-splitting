package org.knime.knip.clump.graph;

public class SplitLine {
	
	private long[] m_p1;
	
	private long[] m_p2;
	
	private double m_weight;
	
	public SplitLine(long[] p1, long[] p2, double weight){
		m_p1 = p1;
		m_p2 = p2;
		m_weight = weight;
	}
	
	public long[] getP1() {
		return m_p1;
	}

	public void setP1(long[] p1) {
		this.m_p1 = p1;
	}

	public long[] getP2() {
		return m_p2;
	}

	public void setP2(long[] p2) {
		this.m_p2 = p2;
	}

	public double getWeight() {
		return m_weight;
	}

	public void setWeight(double weight) {
		this.m_weight = weight;
	}
	
	@Override
	public boolean equals(Object obj){
		if ( obj == null )
			return false;
		if ( !(obj instanceof SplitLine) ) 
			return false;
		SplitLine res = (SplitLine) obj;
		if( res.getP1().equals( m_p1 ) && res.getP2().equals( m_p2 ))
			return true;
		return false;
		
	}



}
