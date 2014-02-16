import java.awt.*;
import java.util.*;

/**
 * Represents a polygon composed of points and edges.
 * @author Andrew Goldin
 */
public class Poly {

	public static final float ROBOT_DIAMETER = 0.35f;

	private Point[] points;
	private Edge[] edges, goalEdges;
	private boolean isGoal;

	/**
	 * Constructs a new polygon object.
	 * @param xpoints the list of x coordinates of points
	 * @param ypoints the list of y coordinates of points
	 * @param npoints the number of points in the polygon
	 * @param isGoal whether or not the polygon is a goal object
	 */
	public Poly(float[] xpoints, float[] ypoints, int npoints, boolean isGoal) {
		points = new Point[npoints];
		edges = new Edge[npoints];
		goalEdges = new Edge[npoints * npoints];
		for (int i = 0; i < npoints; i++) {
			points[i] = new Point(xpoints[i], ypoints[i]);
			if (i > 0) edges[i] = new Edge(points[i-1], points[i]);
		}
		edges[0] = new Edge(points[npoints - 1], points[0]);
		this.isGoal = isGoal;
		if (isGoal) {
			for (int i = 0; i < npoints; i++) {
				for (int j = 0; j < npoints; j++) {
					goalEdges[npoints * i + j] = new Edge(points[i], points[j]);
				}
			}
		}
		
	}
	
	/**
	 * Copy constructor.
	 * @param p the Poly to copy
	 */
	public Poly(Poly p) {
		this.points = p.points.clone();
		this.edges = p.edges.clone();
		this.goalEdges = p.goalEdges.clone();
		this.isGoal = p.isGoal;
	}

	/**
	 * Returns the number of points in the polygon.
	 * @return the number of points
	 */
	public int numPoints() {
		return points.length;
	}

	/**
	 * Returns the list of points in the polygon.
	 * @return the list of points in the order provided to the constructor
	 */
	public Point[] getPoints() {
		return points;
	}

	/**
	 * Returns the smallest x value contained by the polygon.
	 * @return the minimum x value
	 */
	public float getMinX() {
		float minX = points[0].x;
		for (int i = 1; i < points.length; i++) {
			if (points[i].x < minX) minX = points[i].x;
		}
		return minX;
	}

	/**
	 * Returns the smallest y value contained by the polygon.
	 * @return the minimum y value
	 */
	public float getMinY() {
		float minY = points[0].y;
		for (int i = 1; i < points.length; i++) {
			if (points[i].y < minY) minY = points[i].y;
		}
		return minY;
	}

	/**
	 * Returns the largest x value contained by the polygon.
	 * @return the maximum x value
	 */
	public float getMaxX() {
		float maxX = points[0].x;
		for (int i = 1; i < points.length; i++) {
			if (points[i].x > maxX) maxX = points[i].x;
		}
		return maxX;
	}

	/**
	 * Returns the largest y value contained by the polygon.
	 * @return the maximum y value
	 */
	public float getMaxY() {
		float maxY = points[0].y;
		for (int i = 1; i < points.length; i++) {
			if (points[i].y > maxY) maxY = points[i].y;
		}
		return maxY;
	}
	
	/**
	 * Returns the x coordinate of the approximate center of the polygon.
	 * @return the center x value
	 */
	public float getCenterX() {
		return (getMinX() + getMaxX()) / 2.0f;
	}
	
	/**
	 * Returns the y coordinate of the approximate center of the polygon.
	 * @return the center y value
	 */
	public float getCenterY() {
		return (getMinY() + getMaxY()) / 2.0f;
	}


	/**
	 * Uses a Graham scan to grow the polygon by the size of a provided Polygon
	 * @param r the Poly to reference for growth
	 * @return the grown polygon object
	 */
	public Poly grow(Poly r) {
		
		Poly robot = r.reflect(r.getCenterX(), r.getCenterY());
		float rad = Math.abs(robot.getCenterX() - robot.getMinX());
		
		ArrayList<Point> grownPoints = new ArrayList<Point>();
		for (int i = 0; i < points.length; i++) {
			grownPoints.add(new Point(points[i].x - rad, points[i].y + rad));
			grownPoints.add(new Point(points[i].x + rad, points[i].y + rad));
			grownPoints.add(new Point(points[i].x + rad, points[i].y - rad));
			grownPoints.add(new Point(points[i].x - rad, points[i].y - rad));
		}

		//find p0 (highest x with the lowest y)
		Point p0 = grownPoints.get(0);
		for(int i = 0; i < grownPoints.size(); i++) {
			// update p0 if current y value is lower, or equal with larger x value
			if(grownPoints.get(i).y < p0.y || (grownPoints.get(i).y == p0.y && grownPoints.get(i).x > p0.x)) {
				p0 = grownPoints.get(i);
			}
		}
		p0.setAngle(0.0f);
		p0.setDistance(0.0f);

		//get all angles in relation to p0
		for (int i = 0; i < grownPoints.size(); i++) {
			grownPoints.get(i).setAngleAndDistance(p0);
		}
		Collections.sort(grownPoints);
		
		LinkedList<Point> stack = new LinkedList<Point>();
		stack.push(grownPoints.get(grownPoints.size() - 1));
		stack.push(grownPoints.get(0));
		int i = 1;
		while(i < grownPoints.size()) {
			// check for clockwise turns since x-axis is flipped
			if(isClockWise(stack.get(1), stack.get(0), grownPoints.get(i))) {
				stack.push(grownPoints.get(i)); 
				i++;
			}
			else {
				stack.pop();
			}
		}

		// public Poly(float[] xpoints, float[] ypoints, int npoints, boolean isGoal)
		float[] xPoints = new float[stack.size()];
		float[] yPoints = new float[stack.size()];
		for(int j = 0; j < xPoints.length; j++) {
			xPoints[j] = stack.get(j).x;
			yPoints[j] = stack.get(j).y;
		}

		return new Poly(xPoints, yPoints, xPoints.length, false);
	}

	// uses 2D cross product to test if node v is left of the line formed by base and u, base u v
	private boolean isClockWise(Point p1, Point p2, Point p3) {
		double ux = -(p2.x - p1.x);
		double uy = p2.y - p1.y;
		double vx = -(p3.x - p1.x);
		double vy = p3.y - p1.y;
		return (ux * vy - uy * vx < 0);
	}

	// private method for growth
	private Poly reflect(float ox, float oy) {
		float[] newx = new float[points.length];
		float[] newy = new float[points.length];
		for (int i = 0; i < points.length; i++) {
			newx[i] = 2.0f * ox - points[i].x;
			newy[i] = 2.0f * oy - points[i].y;
		}
		return new Poly(newx, newy, points.length, isGoal);
	}
	
	/**
	 * Determines whether a point is inside the polygon.
	 * @param p the point to check
	 * @return true if the Poly contains the point, false otherwise
	 */
	public boolean containsPoint(Point p) {
		
		// this SHOULD work for any arbitrary convex poly, but doesn't :(
//		Poly test = new Poly(this);
//		test.points[0].setAngle(0.0f);
//		test.points[0].setDistance(0.0f);
//		for (int i = 0; i < test.points.length; i++) {
//			test.points[i].setAngleAndDistance(test.points[0]);
//		}
//		Arrays.sort(test.points);
//		for (int i = 0; i < test.points.length - 1; i++) {
//			if (!isClockWise(test.points[i], test.points[i+1], p)) {
//				return false;
//			}
//		}
//		if (!isClockWise(test.points[test.points.length - 1], test.points[0], p)) {
//			return false;
//		}
//		return true;
		
		// shortcut since we know obstacles are rectangular
		float eps = 0.001f; // due to floating point error
		if (p.x < getMinX() + eps || p.x > getMaxX() - eps) return false;
		if (p.y < getMinY() + eps || p.y > getMaxY() - eps) return false;
		return true;
	}
	
	/**
	 * Determines whether a given edge intersects the Poly.
	 * @param e the edge to check
	 * @return true if the provided edge intersects one of the edges of
	 * the Poly or contains one of the points in the Poly, false otherwise
	 */
	public boolean intersects(Edge e) {
		for (int i = 0; i < edges.length; i++) {
			if (e.intersects(edges[i])) return true;
		}
		for (int i = 0; i < points.length; i++) {
			if (e.containsPoint(points[i])) return true;
		}
		return false;
	}

	/**
	 * Draws the polygon.
	 * @param g the graphics context
	 * @param scale the scale factor
	 * @param offsetX the pixel x offset
	 * @param offsetY the pixel y offset
	 */
	public void draw(Graphics g, float scale, float offsetX, float offsetY) {
		if (isGoal) {
			for (int i = 0; i < goalEdges.length; i++) {
				goalEdges[i].draw(g, scale, offsetX, offsetY);
			}
		}
		else {
			for (int i = 0; i < edges.length; i++) {
				edges[i].draw(g, scale, offsetX, offsetY);
			}
		}
	}

}