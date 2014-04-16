package org.knime.knip.clump.post;

import java.util.List;
import java.util.Map;

import org.knime.core.util.Pair;

import net.imglib2.Point;
import net.imglib2.ops.operation.UnaryOperation;

public class RemoveTriangles implements UnaryOperation<List<Pair<Point, Point>>, List<Pair<Point, Point>>> {

	private Map<Point, Integer> m_map;
	
	private List<Pair<Point, Point>> m_list;
	
	@Override
	public List<Pair<Point, Point>> compute(List<Pair<Point, Point>> arg0,
			List<Pair<Point, Point>> arg1) {

		return null;
	}

	@Override
	public UnaryOperation<List<Pair<Point, Point>>, List<Pair<Point, Point>>> copy() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
