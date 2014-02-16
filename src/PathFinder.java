import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Finds the shortest path between two points given a visibility graph.
 * @author Andrew Goldin
 */
public class PathFinder {

	private ArrayList<PointNode> verts;
	private int startIndex, goalIndex;
	
	/**
	 * Constructs a new PathFinder object.
	 * @param start the start point
	 * @param goal the end point
	 * @param edges the edges defining the visibility graph
	 */
	public PathFinder(Point start, Point goal, Edge[] edges) {
		
		verts = new ArrayList<PointNode>();
		for (int i = 0; i < edges.length; i++) {
			verts.add(new PointNode(edges[i].p));
		}
		for (int i = 0; i < verts.size(); i++) {
			for (int j = 0; j < verts.size(); j++) {
				if (hasEdge(verts.get(i).point, verts.get(j).point, edges)) {
					verts.get(i).addConnection(verts.get(j));
					verts.get(j).addConnection(verts.get(i));
				}
			}
		}
		
		startIndex = verts.indexOf(new PointNode(start));
		goalIndex = verts.indexOf(new PointNode(goal));
	}
	
	/**
	 * Determines whether any edge in the vgraph contains two points.
	 * @param a the first point
	 * @param b the second point
	 * @param edges the edges of the graph
	 * @return true if the graph has an edge with the two points, false otherwise
	 */
	public boolean hasEdge(Point a, Point b, Edge[] edges) {
		for (int i = 0; i < edges.length; i++) {
			if (edges[i].p.equals(a) && edges[i].q.equals(b)
					|| edges[i].p.equals(b) && edges[i].q.equals(a)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the shortest path from start to goal using dijkstra's algorithm.
	 * @return an ordered list of Points representing the shortest path
	 */
	public Point[] getShortestPath() {
		return dijkstra(verts, startIndex, goalIndex);
	}
	
	// performs dijkstra's alg to compute the shortest path
	private Point[] dijkstra(ArrayList<PointNode> graph, int sourceIndex, int endIndex) {
		
		// initializations
		ArrayList<PointNode> Q = new ArrayList<PointNode>();
		for (int i = 0; i < verts.size(); i++) {
			verts.get(i).distance = Float.MAX_VALUE;
			verts.get(i).visited = false;
			verts.get(i).previous = null;
		}
		
		verts.get(sourceIndex).distance = 0.0f;
		Q.add(verts.get(sourceIndex));
		
		while (!Q.isEmpty()) {
			PointNode u = closestUnvisited(Q);
			if (u.equals(verts.get(endIndex))) {
				LinkedList<PointNode> stack = new LinkedList<PointNode>();
				PointNode t = verts.get(endIndex);
				while (t.previous != null) {
					stack.push(t);
					t = t.previous;
				}
				stack.push(verts.get(sourceIndex));
				Point[] finalList = new Point[stack.size()];
				for (int i = 0; i < finalList.length; i++) {
					finalList[i] = stack.get(i).point;
				}
				return finalList;
			}
			Q.remove(u);
			u.visited = true;
			
			for (int i = 0; i < u.visiblePoints.size(); i++) {
				PointNode v = u.visiblePoints.get(i);
				float alt = u.distance + Point.computeDistance(u.point, v.point);
				if (alt < v.distance && !v.visited) {
					v.distance = alt;
					v.previous = u;
					Q.add(v);
				}
			}
		}
		
		return null;
	}
	
	// internal method for dijkstra
	private PointNode closestUnvisited(ArrayList<PointNode> pointSet) {
		PointNode closest = pointSet.get(0);
		for (int i = 1; i < pointSet.size(); i++) {
			if (pointSet.get(i).distance < closest.distance && !pointSet.get(i).visited) {
				closest = pointSet.get(i);
			}
		}
		return closest;
	}
	
	// internal class for dijkstra
	private class PointNode {
		public Point point;
		public ArrayList<PointNode> visiblePoints;
		public float distance;
		public boolean visited;
		public PointNode previous;
		public PointNode(Point p) {
			point = new Point(p);
			visiblePoints = new ArrayList<PointNode>();
		}
		public void addConnection(PointNode p) {
			visiblePoints.add(p);
		}
		public boolean equals(Object other) {
			PointNode o = (PointNode) other;
			return point.equals(o.point);
		}
	}
	
}