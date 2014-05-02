package org.knime.knip.clump.graph;

import java.util.LinkedList;
import java.util.List;

import net.imglib2.type.numeric.RealType;

public class Nuclei<T extends RealType<T>>{
	
	private final List<SplitLine<T>> m_splitLines;
	
	private final boolean[] m_constrains;
	
	private final double m_similarity;
	
	private final double m_size;

	public Nuclei(boolean[] constrains, SplitLine<T> splitLine, double similarity, double size){
		m_constrains = constrains;
		m_splitLines = new LinkedList<SplitLine<T>>();
		m_splitLines.add( splitLine );
		m_similarity = similarity;
		m_size = size;
	}
	
	public Nuclei(boolean[] constrains, List<SplitLine<T>> splitLine, double similarity, double size){
		m_constrains = constrains;
		m_splitLines = splitLine;
		m_similarity = similarity;
		m_size = size;
	}
	
	public double getSimilarity(){
		return m_similarity;
	}
	
	public boolean[] getConstrains(){
		return m_constrains;
	}
	
	public List<SplitLine<T>> getSplitLines(){
		return m_splitLines;
	}
	
	public double getSize(){
		return m_size;
	}
}