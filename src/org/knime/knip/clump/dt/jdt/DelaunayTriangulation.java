package org.knime.knip.clump.dt.jdt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;

/**
 * 
 * This class represents a Delaunay Triangulation. The class was written for a
 * large scale triangulation (1000 - 200,000 vertices). The application main use
 * is 3D surface (terrain) presentation. <br>
 * The class main properties are the following:<br>
 * - fast JDTPoint location. (O(n^0.5)), practical runtime is often very fast. <br>
 * - handles degenerate cases and none general position input (ignores duplicate
 * JDTPoints). <br>
 * - save & load from\to text file in TSIN format. <br>
 * - 3D support: including z value approximation. <br>
 * - standard java (1.5 generic) iterators for the vertices and triangles. <br>
 * - smart iterator to only the updated triangles - for terrain simplification <br>
 * <br>
 * 
 * Testing (done in early 2005): Platform java 1.5.02 windows XP (SP2), AMD
 * laptop 1.6G sempron CPU 512MB RAM. Constructing a triangulation of 100,000
 * vertices takes ~ 10 seconds. JDTPoint location of 100,000 JDTPoints on a
 * triangulation of 100,000 vertices takes ~ 5 seconds.
 * 
 * Note: constructing a triangulation with 200,000 vertices and more requires
 * extending java heap size (otherwise an exception will be thrown).<br>
 * 
 * Bugs: if U find a bug or U have an idea as for how to improve the code,
 * please send me an email to: benmo@ariel.ac.il
 * 
 * @author Boaz Ben Moshe 5/11/05 <br>
 *         The project uses some ideas presented in the VoroGuide project,
 *         written by Klasse f?r Kreise (1996-1997), For the original applet
 *         see: http://www.pi6.fernuni-hagen.de/GeomLab/VoroGlide/ . <br>
 */

public class DelaunayTriangulation {

	// the first and last JDTPoints (used only for first step construction)
	private JDTPoint firstP;
	private JDTPoint lastP;

	// for degenerate case!
	private boolean allCollinear;

	// the first and last triangles (used only for first step construction)
	private Triangle firstT, lastT, currT;

	// the triangle the fond (search start from
	private Triangle startTriangle;

	// the triangle the convex hull starts from
	private Triangle startTriangleHull;

	// additional data 4/8/05 used by the iterators
	private Set<JDTPoint> vertices;
	private Vector<Triangle> triangles;

	// The triangles that were deleted in the last deleteJDTPoint iteration.
	private Vector<Triangle> deletedTriangles;
	// The triangles that were added in the last deleteJDTPoint iteration.
	private Vector<Triangle> addedTriangles;

	private int modCount = 0, modCount2 = 0;

	// the Bounding Box, {{x0,y0,z0} , {x1,y1,z1}}
	private JDTPoint bbMin, bbMax;

	/**
	 * Index for faster JDTPoint location searches
	 */
	private GridIndex gridIndex = null;

	/**
	 * creates an empty Delaunay Triangulation.
	 */
	public DelaunayTriangulation() {
		this(new JDTPoint[] {});
	}

	/**
	 * creates a Delaunay Triangulation from all the JDTPoints. Note: duplicated
	 * JDTPoints are ignored.
	 */
	public DelaunayTriangulation(JDTPoint[] ps) {
		this(Arrays.asList(ps));
	}

	public DelaunayTriangulation(Collection<JDTPoint> JDTPoints) {
		modCount = 0;
		modCount2 = 0;
		bbMin = null;
		bbMax = null;
		this.vertices = new TreeSet<JDTPoint>();
		triangles = new Vector<Triangle>();
		deletedTriangles = null;
		addedTriangles = new Vector<Triangle>();
		allCollinear = true;
		insertJDTPoints(JDTPoints);
	}

	/**
	 * the number of (different) vertices in this triangulation.
	 * 
	 * @return the number of vertices in the triangulation (duplicates are
	 *         ignore - set size).
	 */
	public int size() {
		if (vertices == null) {
			return 0;
		}
		return vertices.size();
	}

	/**
	 * @return the number of triangles in the triangulation. <br />
	 *         Note: includes infinife faces!!.
	 */
	public int trianglesSize() {
		this.initTriangles();
		return triangles.size();
	}

	/**
	 * returns the changes counter for this triangulation
	 */
	public int getModeCounter() {
		return this.modCount;
	}

	public void insertJDTPoints(Collection<JDTPoint> JDTPoints) {
		for (JDTPoint p : JDTPoints) {
			insertJDTPoint(p);
		}
	}

	/**
	 * insert the JDTPoint to this Delaunay Triangulation. Note: if p is null or
	 * already exist in this triangulation p is ignored.
	 * 
	 * @param p
	 *            new vertex to be inserted the triangulation.
	 */
	public void insertJDTPoint(JDTPoint p) {
		if (vertices.contains(p))
			return;
		modCount++;
		updateBoundingBox(p);
		vertices.add(p);
		Triangle t = insertJDTPointSimple(p);
		if (t == null) //
			return;
		Triangle tt = t;
		currT = t; // recall the last JDTPoint for - fast (last) update iterator.
		do {
			flip(tt, modCount);
			tt = tt.getCaTriangle();
		} while (tt != t && !tt.isHalfplane());

		// Update index with changed triangles
		if (gridIndex != null)
			gridIndex.updateIndex(getLastUpdatedTriangles());
	}

	/**
	 * Deletes the given JDTPoint from this.
	 * 
	 * @param JDTPointToDelete
	 *            The given JDTPoint to delete.
	 * 
	 *            Implementation of the Mostafavia, Gold & Dakowicz algorithm
	 *            (2002).
	 * 
	 *            By Eyal Roth & Doron Ganel (2009).
	 */
	public void deleteJDTPoint(JDTPoint JDTPointToDelete) {

		// Finding the triangles to delete.
		Vector<JDTPoint> JDTPointsVec = findConnectedVertices(JDTPointToDelete, true);
		if (JDTPointsVec == null) {
			return;
		}

		while (JDTPointsVec.size() >= 3) {
			// Getting a triangle to add, and saving it.
			Triangle triangle = findTriangle(JDTPointsVec, JDTPointToDelete);
			addedTriangles.add(triangle);

			// Finding the JDTPoint on the diagonal (JDTPointToDelete,p)
			JDTPoint p = findDiagonal(triangle, JDTPointToDelete);

			for (JDTPoint tmpP : JDTPointsVec) {
				if (tmpP.equals(p)) {
					JDTPointsVec.removeElement(tmpP);
					break;
				}
			}
		}
		// updating the trangulation
		deleteUpdate(JDTPointToDelete);
		for (Triangle t : deletedTriangles) {
			if (t == startTriangle) {
				startTriangle = addedTriangles.elementAt(0);
				break;
			}
		}
		triangles.removeAll(deletedTriangles);
		triangles.addAll(addedTriangles);
		vertices.remove(JDTPointToDelete);
		addedTriangles.removeAllElements();
		deletedTriangles.removeAllElements();
	}

	/**
	 * return a JDTPoint from the trangulation that is close to JDTPointToDelete
	 * 
	 * @param JDTPointToDelete
	 *            the JDTPoint that the user wants to delete
	 * @return a JDTPoint from the trangulation that is close to JDTPointToDelete By
	 *         Eyal Roth & Doron Ganel (2009).
	 */
	public JDTPoint findCloseJDTPoint(JDTPoint JDTPointToDelete) {
		Triangle triangle = find(JDTPointToDelete);
		JDTPoint p1 = triangle.getA();
		JDTPoint p2 = triangle.getB();
		double d1 = p1.distance(JDTPointToDelete);
		double d2 = p2.distance(JDTPointToDelete);
		if (triangle.isHalfplane()) {
			if (d1 <= d2) {
				return p1;
			} else {
				return p2;
			}
		} else {
			JDTPoint p3 = triangle.getC();

			double d3 = p3.distance(JDTPointToDelete);
			if (d1 <= d2 && d1 <= d3) {
				return p1;
			} else if (d2 <= d1 && d2 <= d3) {
				return p2;
			} else {
				return p3;
			}
		}
	}

	// updates the trangulation after the triangles to be deleted and
	// the triangles to be added were found
	// by Doron Ganel & Eyal Roth(2009)
	private void deleteUpdate(JDTPoint JDTPointToDelete) {
		for (Triangle addedTriangle1 : addedTriangles) {
			// update between addedd triangles and deleted triangles
			for (Triangle deletedTriangle : deletedTriangles) {
				if (shareSegment(addedTriangle1, deletedTriangle)) {
					updateNeighbor(addedTriangle1, deletedTriangle, JDTPointToDelete);
				}
			}
		}
		for (Triangle addedTriangle1 : addedTriangles) {
			// update between added triangles
			for (Triangle addedTriangle2 : addedTriangles) {
				if ((addedTriangle1 != addedTriangle2) && (shareSegment(addedTriangle1, addedTriangle2))) {
					updateNeighbor(addedTriangle1, addedTriangle2);
				}
			}
		}

		// Update index with changed triangles
		if (gridIndex != null)
			gridIndex.updateIndex(addedTriangles.iterator());

	}

	// checks if the 2 triangles shares a segment
	// by Doron Ganel & Eyal Roth(2009)
	private boolean shareSegment(Triangle t1, Triangle t2) {
		int counter = 0;
		JDTPoint t1P1 = t1.getA();
		JDTPoint t1P2 = t1.getB();
		JDTPoint t1P3 = t1.getC();
		JDTPoint t2P1 = t2.getA();
		JDTPoint t2P2 = t2.getB();
		JDTPoint t2P3 = t2.getC();

		if (t1P1.equals(t2P1)) {
			counter++;
		}
		if (t1P1.equals(t2P2)) {
			counter++;
		}
		if (t1P1.equals(t2P3)) {
			counter++;
		}
		if (t1P2.equals(t2P1)) {
			counter++;
		}
		if (t1P2.equals(t2P2)) {
			counter++;
		}
		if (t1P2.equals(t2P3)) {
			counter++;
		}
		if (t1P3.equals(t2P1)) {
			counter++;
		}
		if (t1P3.equals(t2P2)) {
			counter++;
		}
		if (t1P3.equals(t2P3)) {
			counter++;
		}
		if (counter >= 2)
			return true;
		else
			return false;
	}

	// update the neighbors of the addedTriangle and deletedTriangle
	// we assume the 2 triangles share a segment
	// by Doron Ganel & Eyal Roth(2009)
	private void updateNeighbor(Triangle addedTriangle, Triangle deletedTriangle, JDTPoint JDTPointToDelete) {
		JDTPoint delA = deletedTriangle.getA();
		JDTPoint delB = deletedTriangle.getB();
		JDTPoint delC = deletedTriangle.getC();
		JDTPoint addA = addedTriangle.getA();
		JDTPoint addB = addedTriangle.getB();
		JDTPoint addC = addedTriangle.getC();

		// updates the neighbor of the deleted triangle to JDTPoint to the added
		// triangle
		// setting the neighbor of the added triangle
		if (JDTPointToDelete.equals(delA)) {
			deletedTriangle.getBcTriangle().switchneighbors(deletedTriangle, addedTriangle);
			// AB-BC || BA-BC
			if ((addA.equals(delB) && addB.equals(delC)) || (addB.equals(delB) && addA.equals(delC))) {
				addedTriangle.setAbTriangle(deletedTriangle.getBcTriangle());
			}
			// AC-BC || CA-BC
			else if ((addA.equals(delB) && addC.equals(delC)) || (addC.equals(delB) && addA.equals(delC))) {
				addedTriangle.setCanext(deletedTriangle.getBcTriangle());
			}
			// BC-BC || CB-BC
			else {
				addedTriangle.setBcTriangle(deletedTriangle.getBcTriangle());
			}
		} else if (JDTPointToDelete.equals(delB)) {
			deletedTriangle.getCaTriangle().switchneighbors(deletedTriangle, addedTriangle);
			// AB-AC || BA-AC
			if ((addA.equals(delA) && addB.equals(delC)) || (addB.equals(delA) && addA.equals(delC))) {
				addedTriangle.setAbTriangle(deletedTriangle.getCaTriangle());
			}
			// AC-AC || CA-AC
			else if ((addA.equals(delA) && addC.equals(delC)) || (addC.equals(delA) && addA.equals(delC))) {
				addedTriangle.setCanext(deletedTriangle.getCaTriangle());
			}
			// BC-AC || CB-AC
			else {
				addedTriangle.setBcTriangle(deletedTriangle.getCaTriangle());
			}
		}
		// equals c
		else {
			deletedTriangle.getAbTriangle().switchneighbors(deletedTriangle, addedTriangle);
			// AB-AB || BA-AB
			if ((addA.equals(delA) && addB.equals(delB)) || (addB.equals(delA) && addA.equals(delB))) {
				addedTriangle.setAbTriangle(deletedTriangle.getAbTriangle());
			}
			// AC-AB || CA-AB
			else if ((addA.equals(delA) && addC.equals(delB)) || (addC.equals(delA) && addA.equals(delB))) {
				addedTriangle.setCanext(deletedTriangle.getAbTriangle());
			}
			// BC-AB || CB-AB
			else {
				addedTriangle.setBcTriangle(deletedTriangle.getAbTriangle());
			}
		}
	}

	// update the neighbors of the 2 added Triangle s
	// we assume the 2 triangles share a segment
	// by Doron Ganel & Eyal Roth(2009)
	private void updateNeighbor(Triangle addedTriangle1, Triangle addedTriangle2) {
		JDTPoint A1 = addedTriangle1.getA();
		JDTPoint B1 = addedTriangle1.getB();
		JDTPoint C1 = addedTriangle1.getC();
		JDTPoint A2 = addedTriangle2.getA();
		JDTPoint B2 = addedTriangle2.getB();
		JDTPoint C2 = addedTriangle2.getC();

		// A1-A2
		if (A1.equals(A2)) {
			// A1B1-A2B2
			if (B1.equals(B2)) {
				addedTriangle1.setAbTriangle(addedTriangle2);
				addedTriangle2.setAbTriangle(addedTriangle1);
			}
			// A1B1-A2C2
			else if (B1.equals(C2)) {
				addedTriangle1.setAbTriangle(addedTriangle2);
				addedTriangle2.setCanext(addedTriangle1);
			}
			// A1C1-A2B2
			else if (C1.equals(B2)) {
				addedTriangle1.setCanext(addedTriangle2);
				addedTriangle2.setAbTriangle(addedTriangle1);
			}
			// A1C1-A2C2
			else {
				addedTriangle1.setCanext(addedTriangle2);
				addedTriangle2.setCanext(addedTriangle1);
			}
		}
		// A1-B2
		else if (A1.equals(B2)) {
			// A1B1-B2A2
			if (B1.equals(A2)) {
				addedTriangle1.setAbTriangle(addedTriangle2);
				addedTriangle2.setAbTriangle(addedTriangle1);
			}
			// A1B1-B2C2
			else if (B1.equals(C2)) {
				addedTriangle1.setAbTriangle(addedTriangle2);
				addedTriangle2.setBcTriangle(addedTriangle1);

			}
			// A1C1-B2A2
			else if (C1.equals(A2)) {
				addedTriangle1.setCanext(addedTriangle2);
				addedTriangle2.setAbTriangle(addedTriangle1);
			}
			// A1C1-B2C2
			else {
				addedTriangle1.setCanext(addedTriangle2);
				addedTriangle2.setBcTriangle(addedTriangle1);
			}
		}
		// A1-C2
		else if (A1.equals(C2)) {
			// A1B1-C2A2
			if (B1.equals(A2)) {
				addedTriangle1.setAbTriangle(addedTriangle2);
				addedTriangle2.setCanext(addedTriangle1);
			}
			// A1B1-C2B2
			if (B1.equals(B2)) {
				addedTriangle1.setAbTriangle(addedTriangle2);
				addedTriangle2.setBcTriangle(addedTriangle1);
			}
			// A1C1-C2A2
			if (C1.equals(A2)) {
				addedTriangle1.setCanext(addedTriangle2);
				addedTriangle2.setCanext(addedTriangle1);
			}
			// A1C1-C2B2
			else {
				addedTriangle1.setCanext(addedTriangle2);
				addedTriangle2.setBcTriangle(addedTriangle1);
			}
		}
		// B1-A2
		else if (B1.equals(A2)) {
			// B1A1-A2B2
			if (A1.equals(B2)) {
				addedTriangle1.setAbTriangle(addedTriangle2);
				addedTriangle2.setAbTriangle(addedTriangle1);
			}
			// B1A1-A2C2
			else if (A1.equals(C2)) {
				addedTriangle1.setAbTriangle(addedTriangle2);
				addedTriangle2.setCanext(addedTriangle1);
			}
			// B1C1-A2B2
			else if (C1.equals(B2)) {
				addedTriangle1.setBcTriangle(addedTriangle2);
				addedTriangle2.setAbTriangle(addedTriangle1);
			}
			// B1C1-A2C2
			else {
				addedTriangle1.setBcTriangle(addedTriangle2);
				addedTriangle2.setCanext(addedTriangle1);
			}
		}
		// B1-B2
		else if (B1.equals(B2)) {
			// B1A1-B2A2
			if (A1.equals(A2)) {
				addedTriangle1.setAbTriangle(addedTriangle2);
				addedTriangle2.setAbTriangle(addedTriangle1);
			}
			// B1A1-B2C2
			else if (A1.equals(C2)) {
				addedTriangle1.setAbTriangle(addedTriangle2);
				addedTriangle2.setBcTriangle(addedTriangle1);
			}
			// B1C1-B2A2
			else if (C1.equals(A2)) {
				addedTriangle1.setBcTriangle(addedTriangle2);
				addedTriangle2.setAbTriangle(addedTriangle1);
			}
			// B1C1-B2C2
			else {
				addedTriangle1.setBcTriangle(addedTriangle2);
				addedTriangle2.setBcTriangle(addedTriangle1);
			}
		}
		// B1-C2
		else if (B1.equals(C2)) {
			// B1A1-C2A2
			if (A1.equals(A2)) {
				addedTriangle1.setAbTriangle(addedTriangle2);
				addedTriangle2.setCanext(addedTriangle1);
			}
			// B1A1-C2B2
			if (A1.equals(B2)) {
				addedTriangle1.setAbTriangle(addedTriangle2);
				addedTriangle2.setBcTriangle(addedTriangle1);
			}
			// B1C1-C2A2
			if (C1.equals(A2)) {
				addedTriangle1.setBcTriangle(addedTriangle2);
				addedTriangle2.setCanext(addedTriangle1);
			}
			// B1C1-C2B2
			else {
				addedTriangle1.setBcTriangle(addedTriangle2);
				addedTriangle2.setBcTriangle(addedTriangle1);
			}
		}
		// C1-A2
		else if (C1.equals(A2)) {
			// C1A1-A2B2
			if (A1.equals(B2)) {
				addedTriangle1.setCanext(addedTriangle2);
				addedTriangle2.setBcTriangle(addedTriangle1);
			}
			// C1A1-A2C2
			else if (A1.equals(C2)) {
				addedTriangle1.setCanext(addedTriangle2);
				addedTriangle2.setCanext(addedTriangle1);
			}
			// C1B1-A2B2
			else if (B1.equals(B2)) {
				addedTriangle1.setBcTriangle(addedTriangle2);
				addedTriangle2.setAbTriangle(addedTriangle1);
			}
			// C1B1-A2C2
			else {
				addedTriangle1.setBcTriangle(addedTriangle2);
				addedTriangle2.setCanext(addedTriangle1);
			}
		}
		// C1-B2
		else if (C1.equals(B2)) {
			// C1A1-B2A2
			if (A1.equals(A2)) {
				addedTriangle1.setCanext(addedTriangle2);
				addedTriangle2.setAbTriangle(addedTriangle1);
			}
			// C1A1-B2C2
			else if (A1.equals(C2)) {
				addedTriangle1.setCanext(addedTriangle2);
				addedTriangle2.setBcTriangle(addedTriangle1);
			}
			// C1B1-B2A2
			else if (B1.equals(A2)) {
				addedTriangle1.setCanext(addedTriangle2);
				addedTriangle2.setAbTriangle(addedTriangle1);
			}
			// C1B1-B2C2
			else {
				addedTriangle1.setBcTriangle(addedTriangle2);
				addedTriangle2.setBcTriangle(addedTriangle1);
			}
		}
		// C1-C2
		else if (C1.equals(C2)) {
			// C1A1-C2A2
			if (A1.equals(A2)) {
				addedTriangle1.setCanext(addedTriangle2);
				addedTriangle2.setCanext(addedTriangle1);
			}
			// C1A1-C2B2
			if (A1.equals(B2)) {
				addedTriangle1.setCanext(addedTriangle2);
				addedTriangle2.setBcTriangle(addedTriangle1);
			}
			// C1B1-C2A2
			if (B1.equals(A2)) {
				addedTriangle1.setBcTriangle(addedTriangle2);
				addedTriangle2.setCanext(addedTriangle1);
			}
			// C1B1-C2B2
			else {
				addedTriangle1.setBcTriangle(addedTriangle2);
				addedTriangle2.setBcTriangle(addedTriangle1);
			}
		}
	}

	// finds the a JDTPoint on the triangle that if connect it to "JDTPoint" (creating
	// a segment)
	// the other two JDTPoints of the triangle will be to the left and to the right
	// of the segment
	// by Doron Ganel & Eyal Roth(2009)
	private JDTPoint findDiagonal(Triangle triangle, JDTPoint JDTPoint) {
		JDTPoint p1 = triangle.getA();
		JDTPoint p2 = triangle.getB();
		JDTPoint p3 = triangle.getC();

		if ((p1.pointLineTest(JDTPoint, p3) == Geometry.LEFT) && (p2.pointLineTest(JDTPoint, p3) == Geometry.RIGHT))
			return p3;
		if ((p3.pointLineTest(JDTPoint, p2) == Geometry.LEFT) && (p1.pointLineTest(JDTPoint, p2) == Geometry.RIGHT))
			return p2;
		if ((p2.pointLineTest(JDTPoint, p1) == Geometry.LEFT) && (p3.pointLineTest(JDTPoint, p1) == Geometry.RIGHT))
			return p1;
		return null;
	}

	/**
	 * Calculates a Voronoi cell for a given neighborhood in this triangulation.
	 * A neighborhood is defined by a triangle and one of its corner JDTPoints.
	 * 
	 * By Udi Schneider
	 * 
	 * @param triangle
	 *            a triangle in the neighborhood
	 * @param p
	 *            corner JDTPoint whose surrounding neighbors will be checked
	 * @return set of JDTPoints representing the cell polygon
	 */
	public JDTPoint[] calcVoronoiCell(Triangle triangle, JDTPoint p) {
		// handle any full triangle
		if (!triangle.isHalfplane()) {

			// get all neighbors of given corner JDTPoint
			Vector<Triangle> neighbors = findTriangleNeighborhood(triangle, p);

			Iterator<Triangle> itn = neighbors.iterator();
			JDTPoint[] vertices = new JDTPoint[neighbors.size()];

			// for each neighbor, including the given triangle, add
			// center of circumscribed circle to cell polygon
			int index = 0;
			while (itn.hasNext()) {
				Triangle tmp = itn.next();
				vertices[index++] = tmp.circumcircle().center();
			}

			return vertices;
		}

		// handle half plane
		// in this case, the cell is a single line
		// which is the perpendicular bisector of the half plane line
		else {
			// local friendly alias
			Triangle halfplane = triangle;
			// third JDTPoint of triangle adjacent to this half plane
			// (the JDTPoint not shared with the half plane)
			JDTPoint third = null;
			// triangle adjacent to the half plane
			Triangle neighbor = null;

			// find the neighbor triangle
			if (!halfplane.getAbTriangle().isHalfplane()) {
				neighbor = halfplane.getAbTriangle();
			} else if (!halfplane.getBcTriangle().isHalfplane()) {
				neighbor = halfplane.getBcTriangle();
			} else if (!halfplane.getBcTriangle().isHalfplane()) {
				neighbor = halfplane.getCaTriangle();
			}

			// find third JDTPoint of neighbor triangle
			// (the one which is not shared with current half plane)
			// this is used in determining half plane orientation
			if (!neighbor.getA().equals(halfplane.getA()) && !neighbor.getA().equals(halfplane.getB()))
				third = neighbor.getA();
			if (!neighbor.getB().equals(halfplane.getA()) && !neighbor.getB().equals(halfplane.getB()))
				third = neighbor.getB();
			if (!neighbor.getC().equals(halfplane.getA()) && !neighbor.getC().equals(halfplane.getB()))
				third = neighbor.getC();

			// delta (slope) of half plane edge
			double halfplaneDelta = (halfplane.getA().getY() - halfplane.getB().getY())
					/ (halfplane.getA().getX() - halfplane.getB().getX());

			// delta of line perpendicular to current half plane edge
			double perpDelta = (1.0 / halfplaneDelta) * (-1.0);

			// determine orientation: find if the third JDTPoint of the triangle
			// lies above or below the half plane
			// works by finding the matching y value on the half plane line
			// equation
			// for the same x value as the third JDTPoint
			double yOrient = halfplaneDelta * (third.getX() - halfplane.getA().getX()) + halfplane.getA().getY();
			boolean above = true;
			if (yOrient > third.getY())
				above = false;

			// based on orientation, determine cell line direction
			// (towards right or left side of window)
			double sign = 1.0;
			if ((perpDelta < 0 && !above) || (perpDelta > 0 && above))
				sign = -1.0;

			// the cell line is a line originating from the circumcircle to
			// infinity
			// x = 500.0 is used as a large enough value
			JDTPoint circumcircle = neighbor.circumcircle().center();
			double xCellLine = (circumcircle.getX() + (500.0 * sign));
			double yCellLine = perpDelta * (xCellLine - circumcircle.getX()) + circumcircle.getY();

			JDTPoint[] result = new JDTPoint[2];
			result[0] = circumcircle;
			result[1] = new JDTPoint(xCellLine, yCellLine);

			return result;
		}
	}

	/**
	 * returns an iterator object involved in the last update.
	 * 
	 * @return iterator to all triangles involved in the last update of the
	 *         triangulation NOTE: works ONLY if the are triangles (it there is
	 *         only a half plane - returns an empty iterator
	 */
	public Iterator<Triangle> getLastUpdatedTriangles() {
		Vector<Triangle> tmp = new Vector<Triangle>();
		if (this.trianglesSize() > 1) {
			Triangle t = currT;
			allTriangles(t, tmp, this.modCount);
		}
		return tmp.iterator();
	}

	private void allTriangles(Triangle curr, Vector<Triangle> front, int mc) {
		if (curr != null && curr.getMc() == mc && !front.contains(curr)) {
			front.add(curr);
			allTriangles(curr.getAbTriangle(), front, mc);
			allTriangles(curr.getBcTriangle(), front, mc);
			allTriangles(curr.getCaTriangle(), front, mc);
		}
	}

	private Triangle insertJDTPointSimple(JDTPoint p) {
		// nJDTPoints++;
		if (!allCollinear) {
			Triangle t = find(startTriangle, p);
			if (t.isHalfplane())
				startTriangle = extendOutside(t, p);
			else
				startTriangle = extendInside(t, p);
			return startTriangle;
		}

		if (vertices.size() == 1) {
			firstP = p;
			return null;
		}

		if (vertices.size() == 2) {
			startTriangulation(firstP, p);
			return null;
		}

		switch (p.pointLineTest(firstP, lastP)) {
		case LEFT:
			startTriangle = extendOutside(firstT.getAbTriangle(), p);
			allCollinear = false;
			break;
		case RIGHT:
			startTriangle = extendOutside(firstT, p);
			allCollinear = false;
			break;
		case ONSEGMENT:
			insertCollinear(p, Geometry.ONSEGMENT);
			break;
		case INFRONTOFA:
			insertCollinear(p, Geometry.INFRONTOFA);
			break;
		case BEHINDB:
			insertCollinear(p, Geometry.BEHINDB);
			break;
		}
		return null;
	}

	private void insertCollinear(JDTPoint p, Geometry res) {
		Triangle t, tp, u;

		switch (res) {
		case INFRONTOFA:
			t = new Triangle(firstP, p);
			tp = new Triangle(p, firstP);
			t.setAbTriangle(tp);
			tp.setAbTriangle(t);
			t.setBcTriangle(tp);
			tp.setCanext(t);
			t.setCanext(firstT);
			firstT.setBcTriangle(t);
			tp.setBcTriangle(firstT.getAbTriangle());
			firstT.getAbTriangle().setCanext(tp);
			firstT = t;
			firstP = p;
			break;
		case BEHINDB:
			t = new Triangle(p, lastP);
			tp = new Triangle(lastP, p);
			t.setAbTriangle(tp);
			tp.setAbTriangle(t);
			t.setBcTriangle(lastT);
			lastT.setCanext(t);
			t.setCanext(tp);
			tp.setBcTriangle(t);
			tp.setCanext(lastT.getAbTriangle());
			lastT.getAbTriangle().setBcTriangle(tp);
			lastT = t;
			lastP = p;
			break;
		case ONSEGMENT:
			u = firstT;
			while (p.isGreater(u.getA()))
				u = u.getCaTriangle();
			t = new Triangle(p, u.getB());
			tp = new Triangle(u.getB(), p);
			u.setB(p);
			u.getAbTriangle().setA(p);
			t.setAbTriangle(tp);
			tp.setAbTriangle(t);
			t.setBcTriangle(u.getBcTriangle());
			u.getBcTriangle().setCanext(t);
			t.setCanext(u);
			u.setBcTriangle(t);
			tp.setCanext(u.getAbTriangle().getCaTriangle());
			u.getAbTriangle().getCaTriangle().setBcTriangle(tp);
			tp.setBcTriangle(u.getAbTriangle());
			u.getAbTriangle().setCanext(tp);
			if (firstT == u) {
				firstT = t;
			}
			break;
		}
	}

	private void startTriangulation(JDTPoint p1, JDTPoint p2) {
		JDTPoint ps, pb;
		if (p1.isLess(p2)) {
			ps = p1;
			pb = p2;
		} else {
			ps = p2;
			pb = p1;
		}
		firstT = new Triangle(pb, ps);
		lastT = firstT;
		Triangle t = new Triangle(ps, pb);
		firstT.setAbTriangle(t);
		t.setAbTriangle(firstT);
		firstT.setBcTriangle(t);
		t.setCanext(firstT);
		firstT.setCanext(t);
		t.setBcTriangle(firstT);
		firstP = firstT.getB();
		lastP = lastT.getA();
		startTriangleHull = firstT;
	}

	private Triangle extendInside(Triangle t, JDTPoint p) {

		Triangle h1, h2;
		h1 = treatDegeneracyInside(t, p);
		if (h1 != null)
			return h1;

		h1 = new Triangle(t.getC(), t.getA(), p);
		h2 = new Triangle(t.getB(), t.getC(), p);
		t.setC(p);
		t.circumcircle();
		h1.setAbTriangle(t.getCaTriangle());
		h1.setBcTriangle(t);
		h1.setCanext(h2);
		h2.setAbTriangle(t.getBcTriangle());
		h2.setBcTriangle(h1);
		h2.setCanext(t);
		h1.getAbTriangle().switchneighbors(t, h1);
		h2.getAbTriangle().switchneighbors(t, h2);
		t.setBcTriangle(h2);
		t.setCanext(h1);
		return t;
	}

	private Triangle treatDegeneracyInside(Triangle t, JDTPoint p) {

		if (t.getAbTriangle().isHalfplane() && p.pointLineTest(t.getB(), t.getA()) == Geometry.ONSEGMENT)
			return extendOutside(t.getAbTriangle(), p);
		if (t.getBcTriangle().isHalfplane() && p.pointLineTest(t.getC(), t.getB()) == Geometry.ONSEGMENT)
			return extendOutside(t.getBcTriangle(), p);
		if (t.getCaTriangle().isHalfplane() && p.pointLineTest(t.getA(), t.getC()) == Geometry.ONSEGMENT)
			return extendOutside(t.getCaTriangle(), p);
		return null;
	}

	private Triangle extendOutside(Triangle t, JDTPoint p) {

		if (p.pointLineTest(t.getA(), t.getB()) == Geometry.ONSEGMENT) {
			Triangle dg = new Triangle(t.getA(), t.getB(), p);
			Triangle hp = new Triangle(p, t.getB());
			t.setB(p);
			dg.setAbTriangle(t.getAbTriangle());
			dg.getAbTriangle().switchneighbors(t, dg);
			dg.setBcTriangle(hp);
			hp.setAbTriangle(dg);
			dg.setCanext(t);
			t.setAbTriangle(dg);
			hp.setBcTriangle(t.getBcTriangle());
			hp.getBcTriangle().setCanext(hp);
			hp.setCanext(t);
			t.setBcTriangle(hp);
			return dg;
		}
		Triangle ccT = extendcounterclock(t, p);
		Triangle cT = extendclock(t, p);
		ccT.setBcTriangle(cT);
		cT.setCanext(ccT);
		startTriangleHull = cT;
		return cT.getAbTriangle();
	}

	private Triangle extendcounterclock(Triangle t, JDTPoint p) {

		t.setHalfplane(false);
		t.setC(p);
		t.circumcircle();

		Triangle tca = t.getCaTriangle();
		Geometry res = p.pointLineTest(tca.getA(), tca.getB());
		//if (p.pointLineTest(tca.getA(), tca.getB()) >= Geometry.RIGHT) {
		if ( res != Geometry.LEFT || res != Geometry.LEFT) {
			Triangle nT = new Triangle(t.getA(), p);
			nT.setAbTriangle(t);
			t.setCanext(nT);
			nT.setCanext(tca);
			tca.setBcTriangle(nT);
			return nT;
		}
		return extendcounterclock(tca, p);
	}

	private Triangle extendclock(Triangle t, JDTPoint p) {

		t.setHalfplane(false);
		t.setC(p);
		t.circumcircle();

		Triangle tbc = t.getBcTriangle();
		Geometry res = p.pointLineTest(tbc.getA(), tbc.getB());
		if ( res != Geometry.LEFT || res != Geometry.LEFT)  {
			Triangle nT = new Triangle(p, t.getB());
			nT.setAbTriangle(t);
			t.setBcTriangle(nT);
			nT.setBcTriangle(tbc);
			tbc.setCanext(nT);
			return nT;
		}
		return extendclock(tbc, p);
	}

	private void flip(Triangle t, int mc) {
		Triangle u = t.getAbTriangle();
		Triangle v;
		t.setMc(mc);
		if (u.isHalfplane() || !u.circumcircleContains(t.getC()))
			return;

		if (t.getA() == u.getA()) {
			v = new Triangle(u.getB(), t.getB(), t.getC());
			v.setAbTriangle(u.getBcTriangle());
			t.setAbTriangle(u.getAbTriangle());
		} else if (t.getA() == u.getB()) {
			v = new Triangle(u.getC(), t.getB(), t.getC());
			v.setAbTriangle(u.getCaTriangle());
			t.setAbTriangle(u.getBcTriangle());
		} else if (t.getA() == u.getC()) {
			v = new Triangle(u.getA(), t.getB(), t.getC());
			v.setAbTriangle(u.getAbTriangle());
			t.setAbTriangle(u.getCaTriangle());
		} else {
			throw new RuntimeException("Error in flip.");
		}

		v.setMc(mc);
		v.setBcTriangle(t.getBcTriangle());
		v.getAbTriangle().switchneighbors(u, v);
		v.getBcTriangle().switchneighbors(t, v);
		t.setBcTriangle(v);
		v.setCanext(t);
		t.setB(v.getA());
		t.getAbTriangle().switchneighbors(u, t);
		t.circumcircle();

		currT = v;
		flip(t, mc);
		flip(v, mc);
	}

	/**
	 * compute the number of vertices in the convex hull. <br />
	 * NOTE: has a 'bug-like' behavor: <br />
	 * in cases of colinear - not on a asix parallel rectangle, colinear JDTPoints
	 * are reported
	 * 
	 * @return the number of vertices in the convex hull.
	 */
	public int getConvexHullSize() {
		int ans = 0;
		Iterator<JDTPoint> it = this.getConvexHullVerticesIterator();
		while (it.hasNext()) {
			ans++;
			it.next();
		}
		return ans;
	}

	/**
	 * finds the triangle the query JDTPoint falls in, note if out-side of this
	 * triangulation a half plane triangle will be returned (see contains), the
	 * search has expected time of O(n^0.5), and it starts form a fixed triangle
	 * (this.startTriangle),
	 * 
	 * @param p
	 *            query JDTPoint
	 * @return the triangle that JDTPoint p is in.
	 */
	public Triangle find(JDTPoint p) {

		// If triangulation has a spatial index try to use it as the starting
		// triangle
		Triangle searchTriangle = startTriangle;
		if (gridIndex != null) {
			Triangle indexTriangle = gridIndex.findCellTriangleOf(p);
			if (indexTriangle != null)
				searchTriangle = indexTriangle;
		}

		// Search for the JDTPoint's triangle starting from searchTriangle
		return find(searchTriangle, p);
	}

	/**
	 * finds the triangle the query JDTPoint falls in, note if out-side of this
	 * triangulation a half plane triangle will be returned (see contains). the
	 * search starts from the the start triangle
	 * 
	 * @param p
	 *            query JDTPoint
	 * @param start
	 *            the triangle the search starts at.
	 * @return the triangle that JDTPoint p is in..
	 */
	public Triangle find(JDTPoint p, Triangle start) {
		if (start == null)
			start = this.startTriangle;
		Triangle T = find(start, p);
		return T;
	}

	private static Triangle find(Triangle curr, JDTPoint p) {
		if (p == null)
			return null;
		Triangle nextT;
		if (curr.isHalfplane()) {
			nextT = findnext2(p, curr);
			if (nextT == null || nextT.isHalfplane())
				return curr;
			curr = nextT;
		}
		while (true) {
			nextT = findnext1(p, curr);
			if (nextT == null)
				return curr;
			if (nextT.isHalfplane())
				return nextT;
			curr = nextT;
		}
	}

	/*
	 * assumes v is NOT an halfplane! returns the next triangle for find.
	 */
	private static Triangle findnext1(JDTPoint p, Triangle v) {
		if (p.pointLineTest(v.getA(), v.getB()) == Geometry.RIGHT && !v.getAbTriangle().isHalfplane())
			return v.getAbTriangle();
		if (p.pointLineTest(v.getB(), v.getC()) == Geometry.RIGHT && !v.getBcTriangle().isHalfplane())
			return v.getBcTriangle();
		if (p.pointLineTest(v.getC(), v.getA()) == Geometry.RIGHT && !v.getCaTriangle().isHalfplane())
			return v.getCaTriangle();
		if (p.pointLineTest(v.getA(), v.getB()) == Geometry.RIGHT)
			return v.getAbTriangle();
		if (p.pointLineTest(v.getB(), v.getC()) == Geometry.RIGHT)
			return v.getBcTriangle();
		if (p.pointLineTest(v.getC(), v.getA()) == Geometry.RIGHT)
			return v.getCaTriangle();
		return null;
	}

	/** assumes v is an halfplane! - returns another (none halfplane) triangle */
	private static Triangle findnext2(JDTPoint p, Triangle v) {
		if (v.getAbTriangle() != null && !v.getAbTriangle().isHalfplane())
			return v.getAbTriangle();
		if (v.getBcTriangle() != null && !v.getBcTriangle().isHalfplane())
			return v.getBcTriangle();
		if (v.getCaTriangle() != null && !v.getCaTriangle().isHalfplane())
			return v.getCaTriangle();
		return null;
	}

	/*
	 * Receives a JDTPoint and returns all the JDTPoints of the triangles that shares
	 * JDTPoint as a corner (Connected vertices to this JDTPoint).
	 * 
	 * Set saveTriangles to true if you wish to save the triangles that were
	 * found.
	 * 
	 * By Doron Ganel & Eyal Roth
	 */
	private Vector<JDTPoint> findConnectedVertices(JDTPoint JDTPoint, boolean saveTriangles) {
		Set<JDTPoint> JDTPointsSet = new HashSet<JDTPoint>();
		Vector<JDTPoint> JDTPointsVec = new Vector<JDTPoint>();
		Vector<Triangle> triangles = null;
		// Getting one of the neigh
		Triangle triangle = find(JDTPoint);

		// Validating find result.
		if (!triangle.isCorner(JDTPoint)) {
			System.err
					.println("findConnectedVertices: Could not find connected vertices since the first found triangle doesn't"
							+ " share the given JDTPoint.");
			return null;
		}

		triangles = findTriangleNeighborhood(triangle, JDTPoint);
		if (triangles == null) {
			System.err.println("Error: can't delete a JDTPoint on the perimeter");
			return null;
		}
		if (saveTriangles) {
			deletedTriangles = triangles;
		}

		for (Triangle tmpTriangle : triangles) {
			JDTPoint JDTPoint1 = tmpTriangle.getA();
			JDTPoint JDTPoint2 = tmpTriangle.getB();
			JDTPoint JDTPoint3 = tmpTriangle.getC();

			if (JDTPoint1.equals(JDTPoint) && !JDTPointsSet.contains(JDTPoint2)) {
				JDTPointsSet.add(JDTPoint2);
				JDTPointsVec.add(JDTPoint2);
			}

			if (JDTPoint2.equals(JDTPoint) && !JDTPointsSet.contains(JDTPoint3)) {
				JDTPointsSet.add(JDTPoint3);
				JDTPointsVec.add(JDTPoint3);
			}

			if (JDTPoint3.equals(JDTPoint) && !JDTPointsSet.contains(JDTPoint1)) {
				JDTPointsSet.add(JDTPoint1);
				JDTPointsVec.add(JDTPoint1);
			}
		}

		return JDTPointsVec;
	}

	// Walks on a consistent side of triangles until a cycle is achieved.
	// By Doron Ganel & Eyal Roth
	// changed to public by Udi
	public Vector<Triangle> findTriangleNeighborhood(Triangle firstTriangle, JDTPoint JDTPoint) {
		Vector<Triangle> triangles = new Vector<Triangle>(30);
		triangles.add(firstTriangle);

		Triangle prevTriangle = null;
		Triangle currentTriangle = firstTriangle;
		Triangle nextTriangle = currentTriangle.nextNeighbor(JDTPoint, prevTriangle);

		while (nextTriangle != firstTriangle) {
			// the JDTPoint is on the perimeter
			if (nextTriangle.isHalfplane()) {
				return null;
			}
			triangles.add(nextTriangle);
			prevTriangle = currentTriangle;
			currentTriangle = nextTriangle;
			nextTriangle = currentTriangle.nextNeighbor(JDTPoint, prevTriangle);
		}

		return triangles;
	}

	/*
	 * find triangle to be added to the triangulation
	 * 
	 * By: Doron Ganel & Eyal Roth
	 */
	private Triangle findTriangle(Vector<JDTPoint> JDTPointsVec, JDTPoint p) {
		JDTPoint[] arrayJDTPoints = new JDTPoint[JDTPointsVec.size()];
		JDTPointsVec.toArray(arrayJDTPoints);

		int size = arrayJDTPoints.length;
		if (size < 3) {
			return null;
		}
		// if we left with 3 JDTPoints we return the triangle
		else if (size == 3) {
			return new Triangle(arrayJDTPoints[0], arrayJDTPoints[1], arrayJDTPoints[2]);
		} else {
			for (int i = 0; i <= size - 1; i++) {
				JDTPoint p1 = arrayJDTPoints[i];
				int j = i + 1;
				int k = i + 2;
				if (j >= size) {
					j = 0;
					k = 1;
				}
				// check IndexOutOfBound
				else if (k >= size)
					k = 0;
				JDTPoint p2 = arrayJDTPoints[j];
				JDTPoint p3 = arrayJDTPoints[k];
				// check if the triangle is not re-entrant and not encloses p
				Triangle t = new Triangle(p1, p2, p3);
				if ((calcDet(p1, p2, p3) >= 0) && !t.contains(p)) {
					if (!t.fallInsideCircumcircle(arrayJDTPoints))
						return t;
				}
				// if there are only 4 JDTPoints use contains that refers to JDTPoint
				// on boundary as outside
				if (size == 4 && (calcDet(p1, p2, p3) >= 0) && !t.containsBoundaryIsOutside(p)) {
					if (!t.fallInsideCircumcircle(arrayJDTPoints))
						return t;
				}
			}
		}
		return null;
	}

	// TODO: Move this to triangle.
	// checks if the triangle is not re-entrant
	private double calcDet(JDTPoint A, JDTPoint B, JDTPoint P) {
		return (A.getX() * (B.getY() - P.getY())) - (A.getY() * (B.getX() - P.getX()))
				+ (B.getX() * P.getY() - B.getY() * P.getX());
	}

	/**
	 * 
	 * @param p
	 *            query JDTPoint
	 * @return true iff p is within this triangulation (in its 2D convex hull).
	 */

	public boolean contains(JDTPoint p) {
		Triangle tt = find(p);
		return !tt.isHalfplane();
	}

	/**
	 * 
	 * @param x
	 *            - X cordination of the query JDTPoint
	 * @param y
	 *            - Y cordination of the query JDTPoint
	 * @return true iff (x,y) falls inside this triangulation (in its 2D convex
	 *         hull).
	 */
	public boolean contains(double x, double y) {
		return contains(new JDTPoint(x, y));
	}

	/**
	 * 
	 * @param q
	 *            Query JDTPoint
	 * @return the q JDTPoint with updated Z value (z value is as given the
	 *         triangulation).
	 */
	public JDTPoint z(JDTPoint q) {
		Triangle t = find(q);
		return t.getZ(q);
	}

	/**
	 * 
	 * @param x
	 *            - X cordination of the query JDTPoint
	 * @param y
	 *            - Y cordination of the query JDTPoint
	 * @return the q JDTPoint with updated Z value (z value is as given the
	 *         triangulation).
	 */
	public double z(double x, double y) {
		JDTPoint q = new JDTPoint(x, y);
		Triangle t = find(q);
		return t.zValue(q);
	}

	private void updateBoundingBox(JDTPoint p) {
		double x = p.getX(), y = p.getY(), z = p.getZ();
		if (bbMin == null) {
			bbMin = new JDTPoint(p);
			bbMax = new JDTPoint(p);
		} else {
			if (x < bbMin.getX())
				bbMin.setX(x);
			else if (x > bbMax.getX())
				bbMax.setX(x);
			if (y < bbMin.getY())
				bbMin.setY(y);
			else if (y > bbMax.getY())
				bbMax.setY(y);
			if (z < bbMin.getZ())
				bbMin.setZ(z);
			else if (z > bbMax.getZ())
				bbMax.setZ(z);
		}
	}

	/**
	 * @return The bounding rectange between the minimum and maximum coordinates
	 */
	public BoundingBox getBoundingBox() {
		if (bbMin == null || bbMax == null)
			return null;
		return new BoundingBox(bbMin, bbMax);
	}

	/**
	 * return the min JDTPoint of the bounding box of this triangulation
	 * {{x0,y0,z0}}
	 */
	public JDTPoint minBoundingBox() {
		return bbMin;
	}

	/**
	 * return the max JDTPoint of the bounding box of this triangulation
	 * {{x1,y1,z1}}
	 */
	public JDTPoint maxBoundingBox() {
		return bbMax;
	}

	/**
	 * computes the current set (vector) of all triangles and return an iterator
	 * to them.
	 * 
	 * @return an iterator to the current set of all triangles.
	 */
	public Iterator<Triangle> trianglesIterator() {
		if (this.size() <= 2)
			triangles = new Vector<Triangle>();
		initTriangles();
		return triangles.iterator();
	}

	/**
	 * returns an iterator to the set of all the JDTPoints on the XY-convex hull
	 * 
	 * @return iterator to the set of all the JDTPoints on the XY-convex hull.
	 */
	public Iterator<JDTPoint> getConvexHullVerticesIterator() {
		Vector<JDTPoint> ans = new Vector<JDTPoint>();
		Triangle curr = this.startTriangleHull;
		boolean cont = true;
		double x0 = bbMin.getX(), x1 = bbMax.getX();
		double y0 = bbMin.getY(), y1 = bbMax.getY();
		boolean sx, sy;
		while (cont) {
			sx = curr.getA().getX() == x0 || curr.getA().getX() == x1;
			sy = curr.getA().getY() == y0 || curr.getA().getY() == y1;
			if ((sx && sy) || (!sx && !sy)) {
				ans.add(curr.getA());
			}
			if (curr.getBcTriangle() != null && curr.getBcTriangle().isHalfplane())
				curr = curr.getBcTriangle();
			if (curr == this.startTriangleHull)
				cont = false;
		}
		return ans.iterator();
	}

	/**
	 * returns an iterator to the set of JDTPoints compusing this triangulation.
	 * 
	 * @return iterator to the set of JDTPoints compusing this triangulation.
	 */
	public Iterator<JDTPoint> verticesIterator() {
		return this.vertices.iterator();
	}

	private void initTriangles() {
		if (modCount == modCount2)
			return;
		if (this.size() > 2) {
			modCount2 = modCount;
			Vector<Triangle> front = new Vector<Triangle>();
			triangles = new Vector<Triangle>();
			front.add(this.startTriangle);
			while (front.size() > 0) {
				Triangle t = front.remove(0);
				if (t.isMark() == false) {
					t.setMark(true);
					triangles.add(t);
					if (t.getAbTriangle() != null && !t.getAbTriangle().isMark()) {
						front.add(t.getAbTriangle());
					}
					if (t.getBcTriangle() != null && !t.getBcTriangle().isMark()) {
						front.add(t.getBcTriangle());
					}
					if (t.getCaTriangle() != null && !t.getCaTriangle().isMark()) {
						front.add(t.getCaTriangle());
					}
				}
			}
			for (int i = 0; i < triangles.size(); i++) {
				triangles.elementAt(i).setMark(false);
			}
		}
	}

	/**
	 * Index the triangulation using a grid index
	 * 
	 * @param xCellCount
	 *            number of grid cells in a row
	 * @param yCellCount
	 *            number of grid cells in a column
	 */
	public void indexData(int xCellCount, int yCellCount) {
		gridIndex = new GridIndex(this, xCellCount, yCellCount);
	}

	/**
	 * Remove any existing spatial indexing
	 */
	public void removeIndex() {
		gridIndex = null;
	}

	public List<Triangle> getTriangulation() {
		if (this.size() <= 2)
			triangles = new Vector<Triangle>();
		initTriangles();
		List<Triangle> triangulation = new ArrayList<Triangle>(triangles);
		return triangulation;
	}
}


